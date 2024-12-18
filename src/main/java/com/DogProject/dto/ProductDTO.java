package com.DogProject.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private String name;
    private int price;
    private String imageUrl;
    private String productUrl;
    private String source;
    private LocalDateTime crawledAt;
}
