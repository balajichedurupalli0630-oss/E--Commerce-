package com.omnicart.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.omnicart.config.AppConfig;
import com.omnicart.model.Category;
import com.omnicart.model.Product;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ProductApiService {
    private static final ProductApiService INSTANCE = new ProductApiService();
    private final ApiClient apiClient = ApiClient.getInstance();

    private static final List<Category> FALLBACK_CATEGORIES = List.of(
            new Category(1L, "All", "all"), new Category(2L, "T-shirts", "t-shirts"),
            new Category(3L, "Hoodies", "hoodies"), new Category(4L, "Jackets", "jackets"),
            new Category(5L, "Pullovers", "pullovers"), new Category(6L, "Shoes", "shoes"),
            new Category(7L, "Accessories", "accessories"), new Category(8L, "Bags", "bags")
    );

    private ProductApiService() {}

    public static ProductApiService getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<List<Product>> getProducts(String sort, String category, String search, Integer limit) {
        List<String> params = new ArrayList<>();
        if (sort != null && !sort.isBlank()) params.add("sort=" + URLEncoder.encode(sort, StandardCharsets.UTF_8));
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category))
            params.add("category=" + URLEncoder.encode(category, StandardCharsets.UTF_8));
        if (search != null && !search.isBlank()) params.add("search=" + URLEncoder.encode(search, StandardCharsets.UTF_8));
        if (limit != null && limit > 0) params.add("limit=" + limit);

        String url = AppConfig.PRODUCT_SERVICE_URL + "/products?" + String.join("&", params);
        return apiClient.getAsync(url, new TypeReference<List<Product>>() {})
                .exceptionally(ex -> {
                    System.err.println("[ProductApiService] Backend fetch failed (" + ex.getMessage() + "), using fallback products catalog");
                    return getFallbackProducts(sort, category, search, limit);
                });
    }

    public CompletableFuture<Product> getProduct(Long id) {
        return apiClient.getAsync(AppConfig.PRODUCT_SERVICE_URL + "/products/" + id, Product.class)
                .exceptionally(ex -> {
                    System.err.println("[ProductApiService] getProduct failed, searching fallback catalog");
                    return getFallbackProducts(null, null, null, null).stream()
                            .filter(p -> Objects.equals(p.getId(), id))
                            .findFirst().orElse(null);
                });
    }

    public CompletableFuture<Product> createProduct(Product product) {
        return apiClient.postAsync(AppConfig.PRODUCT_SERVICE_URL + "/products", product, Product.class);
    }

    public CompletableFuture<Product> updateProduct(Long id, Product product) {
        return apiClient.putAsync(AppConfig.PRODUCT_SERVICE_URL + "/products/" + id, product, Product.class);
    }

    public CompletableFuture<Product> deleteProduct(Long id) {
        return apiClient.deleteAsync(AppConfig.PRODUCT_SERVICE_URL + "/products/" + id, Product.class);
    }

    public CompletableFuture<List<Category>> getCategories() {
        return apiClient.getAsync(AppConfig.PRODUCT_SERVICE_URL + "/categories", new TypeReference<List<Category>>() {})
                .exceptionally(ex -> {
                    System.err.println("[ProductApiService] getCategories failed, using fallback categories");
                    return getFallbackCategories();
                });
    }

    public CompletableFuture<Category> createCategory(Category category) {
        return apiClient.postAsync(AppConfig.PRODUCT_SERVICE_URL + "/categories", category, Category.class);
    }

    public List<Category> getFallbackCategories() {
        return FALLBACK_CATEGORIES;
    }

    private List<Product> getFallbackProducts(String sort, String category, String search, Integer limit) {
        List<Product> list = List.of(
                new Product(1L, "Adidas CoreFit T-Shirt", "Breathable high-performance athletic wear.",
                        "Crafted from lightweight, moisture-wicking fabric for maximum comfort and durability during workouts or casual wear.",
                        3990.0, List.of("S", "M", "L", "XL", "XXL"), List.of("gray", "purple", "green"),
                        Map.of("gray", "/products/1g.png", "purple", "/products/1p.png", "green", "/products/1gr.png"),
                        "t-shirts", FALLBACK_CATEGORIES.get(1)),
                new Product(2L, "Puma Ultra Warm Zip", "Thermal insulated full-zip winter jacket.",
                        "Features advanced heat retention technology with ergonomic fleece lining and water-resistant outer shell.",
                        5990.0, List.of("S", "M", "L", "XL"), List.of("gray", "green"),
                        Map.of("gray", "/products/2g.png", "green", "/products/2gr.png"),
                        "jackets", FALLBACK_CATEGORIES.get(3)),
                new Product(3L, "Nike Air Essentials Pullover", "Classic relaxed-fit everyday fleece pullover.",
                        "Premium brushed-back fleece delivers everyday warmth with ribbed cuffs and adjustable drawcord hood.",
                        6990.0, List.of("S", "M", "L"), List.of("green", "blue", "black"),
                        Map.of("green", "/products/3gr.png", "blue", "/products/3b.png", "black", "/products/3bl.png"),
                        "pullovers", FALLBACK_CATEGORIES.get(4)),
                new Product(4L, "Nike Dri Flex T-Shirt", "Lightweight stretch training tee.",
                        "Engineered with four-way stretch fabric and anti-odor technology to keep you dry and comfortable all day.",
                        2490.0, List.of("S", "M", "L"), List.of("white", "pink"),
                        Map.of("white", "/products/4w.png", "pink", "/products/4p.png"),
                        "t-shirts", FALLBACK_CATEGORIES.get(1)),
                new Product(5L, "Under Armour StormFleece", "Water-repellent storm fleece hoodie.",
                        "UA Storm technology repels water without sacrificing breathability. Armor Fleece is light, breathable and stretchy.",
                        4990.0, List.of("S", "M", "L"), List.of("red", "orange", "black"),
                        Map.of("red", "/products/5r.png", "orange", "/products/5o.png", "black", "/products/5bl.png"),
                        "hoodies", FALLBACK_CATEGORIES.get(2)),
                new Product(6L, "Reebok Heritage Sweatshirt", "Vintage-inspired crewneck sweatshirt.",
                        "Heavyweight cotton-blend fleece with iconic embroidered logo and ribbed collar and hem.",
                        4490.0, List.of("M", "L", "XL"), List.of("gray", "white"),
                        Map.of("gray", "/products/6g.png", "white", "/products/6w.png"),
                        "pullovers", FALLBACK_CATEGORIES.get(4)),
                new Product(7L, "Champion Eco Weave Hoodie", "Sustainable heavyweight reverse-weave fleece.",
                        "Double-needle construction with signature stretch side panels for unmatched mobility and durability.",
                        5490.0, List.of("S", "M", "L", "XL"), List.of("gray", "purple"),
                        Map.of("gray", "/products/7g.png", "purple", "/products/7p.png"),
                        "hoodies", FALLBACK_CATEGORIES.get(2)),
                new Product(8L, "Columbia Alpine Thermal Jacket", "All-weather insulated outdoor shell.",
                        "OmniHeat reflective thermal lining with seam-sealed waterproof protection for harsh conditions.",
                        8990.0, List.of("M", "L", "XL", "XXL"), List.of("blue", "green"),
                        Map.of("blue", "/products/8b.png", "green", "/products/8gr.png"),
                        "jackets", FALLBACK_CATEGORIES.get(3))
        );

        var stream = list.stream();
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            stream = stream.filter(p -> category.equalsIgnoreCase(p.getCategorySlug()));
        }
        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            stream = stream.filter(p -> p.getName().toLowerCase().contains(s) ||
                    (p.getShortDescription() != null && p.getShortDescription().toLowerCase().contains(s)));
        }
        if ("asc".equalsIgnoreCase(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(Product::getEffectivePrice));
        } else if ("desc".equalsIgnoreCase(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(Product::getEffectivePrice).reversed());
        }
        if (limit != null && limit > 0) {
            stream = stream.limit(limit);
        }
        return new ArrayList<>(stream.toList());
    }
}
