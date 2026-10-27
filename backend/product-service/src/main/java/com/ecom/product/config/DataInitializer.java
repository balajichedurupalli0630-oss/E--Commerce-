package com.ecom.product.config;

import com.ecom.product.model.Category;
import com.ecom.product.model.Product;
import com.ecom.product.repository.CategoryRepository;
import com.ecom.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("No categories found in database. Seeding initial categories...");
            categoryRepository.saveAll(List.of(
                    Category.builder().name("All").slug("all").build(),
                    Category.builder().name("T-Shirts").slug("t-shirts").build(),
                    Category.builder().name("Hoodies").slug("hoodies").build(),
                    Category.builder().name("Jackets").slug("jackets").build(),
                    Category.builder().name("Pullovers").slug("pullovers").build(),
                    Category.builder().name("Shoes").slug("shoes").build()
            ));
            log.info("Categories seeded successfully.");
        }

        if (productRepository.count() == 0) {
            log.info("No products found in database. Seeding initial catalog...");
            List<Product> initialProducts = List.of(
                    Product.builder()
                            .name("Adidas CoreFit T-Shirt")
                            .shortDescription("Breathable high-performance athletic wear.")
                            .description("Crafted from lightweight, moisture-wicking fabric for maximum comfort and durability during workouts or casual wear.")
                            .price(3990)
                            .sizes(List.of("S", "M", "L", "XL", "XXL"))
                            .colors(List.of("gray", "purple", "green"))
                            .images(Map.of(
                                    "gray", "/products/1g.png",
                                    "purple", "/products/1p.png",
                                    "green", "/products/1gr.png"
                            ))
                            .categorySlug("t-shirts")
                            .build(),

                    Product.builder()
                            .name("Puma Ultra Warm Zip")
                            .shortDescription("Thermal insulated full-zip winter jacket.")
                            .description("Features advanced heat retention technology with ergonomic fleece lining and water-resistant outer shell.")
                            .price(5990)
                            .sizes(List.of("S", "M", "L", "XL"))
                            .colors(List.of("gray", "green"))
                            .images(Map.of(
                                    "gray", "/products/2g.png",
                                    "green", "/products/2gr.png"
                            ))
                            .categorySlug("jackets")
                            .build(),

                    Product.builder()
                            .name("Nike Air Essentials Pullover")
                            .shortDescription("Classic relaxed-fit everyday fleece pullover.")
                            .description("Premium brushed-back fleece delivers everyday warmth with ribbed cuffs and adjustable drawcord hood.")
                            .price(6990)
                            .sizes(List.of("S", "M", "L"))
                            .colors(List.of("green", "blue", "black"))
                            .images(Map.of(
                                    "green", "/products/3gr.png",
                                    "blue", "/products/3b.png",
                                    "black", "/products/3bl.png"
                            ))
                            .categorySlug("pullovers")
                            .build(),

                    Product.builder()
                            .name("Nike Dri Flex T-Shirt")
                            .shortDescription("Lightweight stretch training tee.")
                            .description("Engineered with four-way stretch fabric and anti-odor technology to keep you dry and comfortable all day.")
                            .price(2990)
                            .sizes(List.of("S", "M", "L"))
                            .colors(List.of("white", "pink"))
                            .images(Map.of(
                                    "white", "/products/4w.png",
                                    "pink", "/products/4p.png"
                            ))
                            .categorySlug("t-shirts")
                            .build(),

                    Product.builder()
                            .name("Under Armour StormFleece")
                            .shortDescription("Water-repellent storm fleece hoodie.")
                            .description("UA Storm technology repels water without sacrificing breathability. Armor Fleece is light, breathable and stretchy.")
                            .price(4990)
                            .sizes(List.of("S", "M", "L"))
                            .colors(List.of("red", "orange", "black"))
                            .images(Map.of(
                                    "red", "/products/5r.png",
                                    "orange", "/products/5o.png",
                                    "black", "/products/5bl.png"
                            ))
                            .categorySlug("hoodies")
                            .build(),

                    Product.builder()
                            .name("Reebok Heritage Sweatshirt")
                            .shortDescription("Vintage-inspired crewneck sweatshirt.")
                            .description("Heavyweight cotton-blend fleece with iconic embroidered logo and ribbed collar and hem.")
                            .price(4490)
                            .sizes(List.of("M", "L", "XL"))
                            .colors(List.of("gray", "white"))
                            .images(Map.of(
                                    "gray", "/products/6g.png",
                                    "white", "/products/6w.png"
                            ))
                            .categorySlug("pullovers")
                            .build(),

                    Product.builder()
                            .name("Champion Eco Weave Hoodie")
                            .shortDescription("Sustainable heavyweight reverse-weave fleece.")
                            .description("Double-needle construction with signature stretch side panels for unmatched mobility and durability.")
                            .price(5490)
                            .sizes(List.of("S", "M", "L", "XL"))
                            .colors(List.of("gray", "purple"))
                            .images(Map.of(
                                    "gray", "/products/7g.png",
                                    "purple", "/products/7p.png"
                            ))
                            .categorySlug("hoodies")
                            .build(),

                    Product.builder()
                            .name("Columbia Alpine Thermal Jacket")
                            .shortDescription("All-weather insulated outdoor shell.")
                            .description("OmniHeat reflective thermal lining with seam-sealed waterproof protection for harsh conditions.")
                            .price(8990)
                            .sizes(List.of("M", "L", "XL", "XXL"))
                            .colors(List.of("blue", "green"))
                            .images(Map.of(
                                    "blue", "/products/8b.png",
                                    "green", "/products/8gr.png"
                            ))
                            .categorySlug("jackets")
                            .build(),

                    Product.builder()
                            .name("Nike White Sneakers")
                            .shortDescription("Classic lightweight everyday casual sneakers.")
                            .description("Durable synthetic leather upper with cushioned sole and herringbone grip for all-day comfort.")
                            .price(2999)
                            .sizes(List.of("UK 7", "UK 8", "UK 9", "UK 10", "UK 11"))
                            .colors(List.of("white"))
                            .images(Map.of(
                                    "white", "/products/1g.png"
                            ))
                            .categorySlug("shoes")
                            .build()
            );

            productRepository.saveAll(initialProducts);
            log.info("Catalog seeded with {} products.", initialProducts.size());
        }
    }
}
