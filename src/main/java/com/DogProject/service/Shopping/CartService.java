package com.DogProject.service.Shopping;

import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.repository.Shopping.CartItemRepository;
import com.DogProject.repository.Shopping.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public List<CartItem> getCartItems() {
        return cartItemRepository.findAll();
    }

    @Transactional
    public void addToCart(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findByProduct(product)
                .orElse(CartItem.builder()
                        .product(product)
                        .quantity(0)
                        .build());

        cartItem.addQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void updateQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        
        cartItem.updateQuantity(quantity);
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}
