package com.DogProject.controller.Shopping;

import com.DogProject.service.Shopping.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import java.util.ArrayList;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopMyPageController {

    private final OrderService orderService;

    @GetMapping("/mypage")
    public String myPage(Model model) {
        return "shop/mypage";
    }

    @GetMapping("/orders")
    public String orderList(Model model, Principal principal,
                         @RequestParam(required = false) String period,
                         @RequestParam(required = false) String status) {
        if (principal == null) {
            // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        model.addAttribute("orders", orderService.getOrderHistory(principal.getName(), period, status));
        return "shop/order";
    }

    @GetMapping("/returns")
    public String returnList(Model model, Principal principal,
                          @RequestParam(required = false) String period) {
        if (principal == null) {
            // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        model.addAttribute("returns", orderService.getReturnHistory(principal.getName(), period));
        return "shop/return";
    }
}
