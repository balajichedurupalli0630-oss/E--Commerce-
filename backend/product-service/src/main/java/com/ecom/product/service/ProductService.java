package com.ecom.product.service;

import com.ecom.dto.CategoryDTO;
import com.ecom.dto.ProductDTO;
import com.ecom.event.ProductCreatedEvent;
import com.ecom.event.ProductDeletedEvent;
import com.ecom.product.model.Product;
import com.ecom.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductService(ProductRepository productRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<ProductDTO> getProducts(String sort, String category, String search, Integer limit) {
        Sort sortObj = switch (sort != null ? sort.toLowerCase() : "") {
            case "asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !category.isBlank()) predicates.add(cb.equal(root.get("categorySlug"), category));
            if (search != null && !search.isBlank()) predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Product> products = (limit != null && limit > 0)
                ? productRepository.findAll(spec, PageRequest.of(0, limit, sortObj)).getContent()
                : productRepository.findAll(spec, sortObj);

        return products.stream().map(this::toDTO).toList();
    }

    public ProductDTO getProduct(Long id) {
        return productRepository.findById(id).map(this::toDTO).orElse(null);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        List<String> colors = dto.getColors();
        Map<String, String> images = dto.getImages();

        if (colors == null || colors.isEmpty()) throw new IllegalArgumentException("Colors array is required!");
        if (images == null || images.isEmpty()) throw new IllegalArgumentException("Images object is required!");

        List<String> missingColors = colors.stream()
                .filter(color -> !images.containsKey(color) || images.get(color) == null || images.get(color).isBlank())
                .toList();

        if (!missingColors.isEmpty()) throw new IllegalArgumentException("Missing images for colors: " + missingColors);

        Product product = Product.builder()
                .name(dto.getName())
                .shortDescription(dto.getShortDescription())
                .description(dto.getDescription())
                .price(dto.getPrice() != null ? dto.getPrice().doubleValue() : (dto.getCostPrice() != null ? dto.getCostPrice() : 0.0))
                .costPrice(dto.getCostPrice() != null ? dto.getCostPrice() : 0.0)
                .stockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0)
                .leadTimeDays(dto.getLeadTimeDays() != null ? dto.getLeadTimeDays() : 5)
                .categorySlug(dto.getCategorySlug())
                .sizes(dto.getSizes() != null ? new ArrayList<>(dto.getSizes()) : new ArrayList<>())
                .colors(dto.getColors() != null ? new ArrayList<>(dto.getColors()) : new ArrayList<>())
                .images(dto.getImages() != null ? dto.getImages() : Map.of())
                .build();

        Product savedProduct = productRepository.save(product);

        sendEvent("product.created", ProductCreatedEvent.builder()
                .id(savedProduct.getId().toString())
                .name(savedProduct.getName())
                .price(savedProduct.getPrice() != null ? savedProduct.getPrice().intValue() : 0)
                .build(), savedProduct.getId());

        return toDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getShortDescription() != null) product.setShortDescription(dto.getShortDescription());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice().doubleValue());
        if (dto.getCostPrice() != null) product.setCostPrice(dto.getCostPrice());
        if (dto.getStockQuantity() != null) product.setStockQuantity(dto.getStockQuantity());
        if (dto.getLeadTimeDays() != null) product.setLeadTimeDays(dto.getLeadTimeDays());
        if (dto.getCategorySlug() != null) product.setCategorySlug(dto.getCategorySlug());
        if (dto.getSizes() != null) product.setSizes(new ArrayList<>(dto.getSizes()));
        if (dto.getColors() != null) product.setColors(new ArrayList<>(dto.getColors()));
        if (dto.getImages() != null) product.setImages(dto.getImages());

        return toDTO(productRepository.save(product));
    }

    @Transactional
    public ProductDTO deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        productRepository.delete(product);

        sendEvent("product.deleted", ProductDeletedEvent.builder().id(id).build(), id);

        return toDTO(product);
    }

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;
        var cat = product.getCategory();
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice() != null ? product.getPrice().intValue() : null)
                .costPrice(product.getCostPrice())
                .stockQuantity(product.getStockQuantity())
                .leadTimeDays(product.getLeadTimeDays())
                .sizes(product.getSizes())
                .colors(product.getColors())
                .images(product.getImages())
                .categorySlug(product.getCategorySlug())
                .category(cat != null ? CategoryDTO.builder().id(cat.getId()).name(cat.getName()).slug(cat.getSlug()).build() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private void sendEvent(String topic, Object event, Object id) {
        try {
            kafkaTemplate.send(topic, event);
            log.info("Sent {} event to Kafka for product id: {}", topic, id);
        } catch (Exception e) {
            log.error("Failed to publish {} event", topic, e);
        }
    }
}
