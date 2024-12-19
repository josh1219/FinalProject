package com.DogProject.controller.Shopping;

import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.service.Shopping.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Controller
@RequestMapping("/shop/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("")
    public String cartPage(Model model, Principal principal) {
        try {
            List<CartItem> cartItems = cartService.getCartItems(principal);
            model.addAttribute("cartItems", cartItems);
            
            if (cartItems != null && !cartItems.isEmpty()) {
                int totalPrice = cartItems.stream()
                    .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
                model.addAttribute("totalPrice", totalPrice);
            } else {
                model.addAttribute("totalPrice", 0);
                model.addAttribute("emptyCart", true);
            }
            
            return "shop/cart";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "장바구니를 불러오는 중 오류가 발생했습니다.");
            return "shop/cart";
        }
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(@RequestParam Long productId, @RequestParam int quantity, Principal principal) {
        try {
            cartService.addToCart(productId, quantity, principal);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @DeleteMapping("/remove")
    @ResponseBody
    public ResponseEntity<String> removeFromCart(@RequestParam Long productId, Principal principal) {
        try {
            cartService.removeFromCartByProductId(productId, principal);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateQuantity(@RequestParam Long productId, @RequestParam int change, Principal principal) {
        try {
            cartService.updateCartItemQuantity(productId, change, principal);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkCartStatus(@RequestParam Long productId, Principal principal) {
        boolean inCart = cartService.isProductInCart(productId, principal);
        return ResponseEntity.ok(Collections.singletonMap("inCart", inCart));
    }
}
