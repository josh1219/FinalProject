package com.DogProject.service.Shopping;

import com.DogProject.entity.Shopping.Product;
import com.DogProject.repository.Shopping.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${file.upload.directory}")
    private String uploadDir;

    private String extractImageUrl(String nextImageUrl) {
        try {
            String encodedUrl = nextImageUrl.split("url=")[1].split("&")[0];
            return java.net.URLDecoder.decode(encodedUrl, "UTF-8");
        } catch (Exception e) {
            log.error("Error extracting image URL: {}", e.getMessage(), e);
            return nextImageUrl;
        }
    }

    private String generateFileName(String originalUrl, Long productId) {
        try {
            // URL의 마지막 부분에서 파일명 추출
            String originalFileName = originalUrl.substring(originalUrl.lastIndexOf('/') + 1);
            // 확장자 추출 (없으면 .jpg 사용)
            String extension = originalFileName.contains(".") ? 
                originalFileName.substring(originalFileName.lastIndexOf('.')) : ".jpg";
            
            // 상품ID_원래파일명의일부.확장자 형식으로 생성
            return String.format("product_%d_%s", productId, originalFileName.substring(0, Math.min(20, originalFileName.length()))) + extension;
        } catch (Exception e) {
            log.error("Error generating file name: {}", e.getMessage());
            return "product_" + productId + "_" + UUID.randomUUID().toString() + ".jpg";
        }
    }

    private String downloadAndSaveImage(String imageUrl, Long productId) {
        try {
            log.info("Downloading image from URL: {}", imageUrl);
            
            // 실제 이미지 URL 추출
            String actualImageUrl = extractImageUrl(imageUrl);
            log.info("Actual image URL: {}", actualImageUrl);
            
            // 이미지를 저장할 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                log.info("Creating directory: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }

            // 파일명 생성
            String fileName = generateFileName(actualImageUrl, productId);
            Path targetPath = uploadPath.resolve(fileName);
            log.info("Saving image to: {}", targetPath);

            // 이미지 URL에서 이미지 다운로드
            URL url = new URL(actualImageUrl);
            Resource resource = new org.springframework.core.io.UrlResource(url.toURI());
            Files.copy(resource.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Image saved successfully");

            // 저장된 이미지의 상대 경로 반환
            return "/uploads/" + fileName;
        } catch (Exception e) {
            log.error("Error downloading image: {}", e.getMessage(), e);
            return "/images/default-product.jpg";
        }
    }

    @PostConstruct
    @Transactional
    public void initializeProducts() {
        try {
            if (productRepository.count() == 0) {
                log.info("Initializing products from JSON");
                Resource resource = resourceLoader.getResource("classpath:products.json");
                log.info("Resource exists: {}", resource.exists());
                List<Map<String, Object>> productsData = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                log.info("Found {} products in JSON", productsData.size());

                for (Map<String, Object> productData : productsData) {
                    String originalImageUrl = (String) productData.get("image_url");
                    Long productId = Long.valueOf(productData.get("P_IDX").toString());
                    log.info("Processing product with image URL: {}", originalImageUrl);
                    String savedImageUrl = downloadAndSaveImage(originalImageUrl, productId);
                    log.info("Image saved with path: {}", savedImageUrl);

                    Product product = Product.builder()
                        .pidx(productId)
                        .name((String) productData.get("p_name"))
                        .price(Integer.parseInt(productData.get("p_price").toString()))
                        .stock(Integer.parseInt(productData.get("p_stock").toString()))
                        .imageUrl(savedImageUrl)
                        .category((String) productData.get("category"))
                        .createdAt(LocalDateTime.parse((String) productData.get("crawled_at")))
                        .build();

                    productRepository.save(product);
                    log.info("Saved product: {}", product.getName());
                }
            }
        } catch (IOException e) {
            log.error("Error initializing products: {}", e.getMessage(), e);
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    // 상품 정렬 메서드
    public List<Product> getProductsSorted(String sortType) {
        switch (sortType.toLowerCase()) {
            case "new":
                return productRepository.findAllByOrderByCreatedAtDesc();
            case "popular":
                return productRepository.findAllByOrderBySalesCountDesc();
            case "low":
                return productRepository.findAllByOrderByPriceAsc();
            case "high":
                return productRepository.findAllByOrderByPriceDesc();
            default:
                return getAllProducts();
        }
    }
}
