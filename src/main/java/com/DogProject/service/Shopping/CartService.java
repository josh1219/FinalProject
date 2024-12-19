package com.DogProject.service.Shopping;

import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.repository.Shopping.CartItemRepository;
import com.DogProject.repository.Shopping.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.security.Principal;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final HttpSession httpSession;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, HttpSession httpSession) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.httpSession = httpSession;
    }

    private String getCurrentUserIdentifier(Principal principal) {
        if (principal != null) {
            return "USER:" + principal.getName();
        }
        return "SESSION:" + httpSession.getId();
    }

    public List<CartItem> getCartItems(Principal principal) {
        String identifier = getCurrentUserIdentifier(principal);
        if (identifier.startsWith("USER:")) {
            return cartItemRepository.findByUserId(identifier);
        }
        return cartItemRepository.findBySessionId(identifier);
    }

    @Transactional
    public void addToCart(Long productId, int quantity, Principal principal) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        String identifier = getCurrentUserIdentifier(principal);
        CartItem cartItem;
        
        if (identifier.startsWith("USER:")) {
            cartItem = cartItemRepository.findByProductAndUserId(product, identifier)
                    .orElse(new CartItem());
            cartItem.setUserId(identifier);
        } else {
            cartItem = cartItemRepository.findByProductAndSessionId(product, identifier)
                    .orElse(new CartItem());
            cartItem.setSessionId(identifier);
        }

        if (cartItem.getId() == null) {
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void updateCartItemQuantity(Long productId, int change, Principal principal) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        String identifier = getCurrentUserIdentifier(principal);
        CartItem cartItem;
        
        if (identifier.startsWith("USER:")) {
            cartItem = cartItemRepository.findByProductAndUserId(product, identifier)
                    .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        } else {
            cartItem = cartItemRepository.findByProductAndSessionId(product, identifier)
                    .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        }

        int newQuantity = cartItem.getQuantity() + change;
        if (newQuantity < 1) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void removeFromCartByProductId(Long productId, Principal principal) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        String identifier = getCurrentUserIdentifier(principal);
        if (identifier.startsWith("USER:")) {
            cartItemRepository.deleteByProductAndUserId(product, identifier);
        } else {
            cartItemRepository.deleteByProductAndSessionId(product, identifier);
        }
    }

    public boolean isProductInCart(Long productId, Principal principal) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        String identifier = getCurrentUserIdentifier(principal);
        if (identifier.startsWith("USER:")) {
            return cartItemRepository.findByProductAndUserId(product, identifier).isPresent();
        }
        return cartItemRepository.findByProductAndSessionId(product, identifier).isPresent();
    }
}
