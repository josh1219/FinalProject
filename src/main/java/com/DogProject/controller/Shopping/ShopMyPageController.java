package com.DogProject.controller.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Order;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopMyPageController {

    private final OrderService orderService;
    private final MemberService memberService;

    @GetMapping("/mypage")
    public String myPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }

        String email = authentication.getName();
        Member member = memberService.getMemberByEmail(email);
        
        if (member != null) {
            // 현재 포인트
            model.addAttribute("point", member.getPoint());

            // 총 사용 포인트
            int totalUsedPoints = orderService.getTotalUsedPoints(member.getMIdx());
            model.addAttribute("totalUsedPoints", totalUsedPoints);

            // 총 주문금액과 주문 횟수
            Map<String, Object> orderInfo = orderService.getTotalOrderInfo(member.getMIdx());
            model.addAttribute("totalAmount", orderInfo.get("totalAmount"));
            model.addAttribute("orderCount", orderInfo.get("orderCount"));
        }

        return "shop/mypage";
    }

    @GetMapping("/order-history")
    public String orderList(Model model, Principal principal,
                         @RequestParam(required = false) String period,
                         @RequestParam(required = false) String status) {
        if (principal == null) {
            // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/member/login";
        }
        model.addAttribute("order-history", orderService.getOrderHistory(principal.getName(), period, status));
        return "shop/order-history";
    }

    @PostMapping("/order-history")
    public String createOrder(@RequestParam Map<String, Object> orderData, Model model) {
        model.addAttribute("orderItems", orderData);
        return "shop/order-history";
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
