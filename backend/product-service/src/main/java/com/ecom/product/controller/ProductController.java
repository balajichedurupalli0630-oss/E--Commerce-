package com.ecom.product.controller;

import com.ecom.dto.ProductDTO;
import com.ecom.dto.ProductRecommendationDTO;
import com.ecom.product.model.Product;
import com.ecom.product.service.ProductService;
import com.ecom.product.service.StochasticFinancialEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final StochasticFinancialEngineService financialEngineService;

    public ProductController(ProductService productService, StochasticFinancialEngineService financialEngineService) {
        this.productService = productService;
        this.financialEngineService = financialEngineService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(productService.getProducts(sort, category, search, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO product = productService.getProduct(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductRecommendationDTO>> getRecommendations() {
        return ResponseEntity.ok(financialEngineService.generateAllRecommendations());
    }

    @PatchMapping("/{id}/apply-price")
    public ResponseEntity<Product> applyPrice(@PathVariable Long id, @RequestParam Double price) {
        return ResponseEntity.ok(financialEngineService.applyRecommendedPrice(id, price));
    }

    @PatchMapping("/{id}/restock")
    public ResponseEntity<Product> restock(@PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(financialEngineService.restockProduct(id, quantity));
    }
}
