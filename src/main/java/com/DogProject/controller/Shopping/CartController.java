package com.DogProject.controller.Shopping;

import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.service.Shopping.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shop/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("")
    public String viewCart(Model model) {
        List<CartItem> cartItems = cartService.getCartItems();
        model.addAttribute("cartItems", cartItems);
        return "shop/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        cartService.addToCart(productId, quantity);
        return "success";
    }

    @PostMapping("/update")
    @ResponseBody
    public String updateQuantity(@RequestParam Long cartItemId, @RequestParam int quantity) {
        cartService.updateQuantity(cartItemId, quantity);
        return "success";
    }

    @PostMapping("/remove")
    @ResponseBody
    public String removeFromCart(@RequestParam Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return "success";
    }
}
