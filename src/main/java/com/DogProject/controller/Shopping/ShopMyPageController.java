package com.DogProject.controller.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.Order;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.security.Principal;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopMyPageController {

    private final OrderService orderService;
    private final MemberService memberService;

    @GetMapping("/mypage")
    public String myPage(Model model, HttpSession session) {
        // 세션에서 m_idx 가져오기
        Integer mIdx = (Integer) session.getAttribute("mIdx");
        
        if (mIdx == null) {
            return "redirect:/member/login";
        }

        Member member = memberService.getMemberById(mIdx);
        if (member != null) {
            // 현재 포인트
            model.addAttribute("point", member.getPoint());

            // 총 사용 포인트
            int totalUsedPoints = member.getTotalUsedPoint();
            model.addAttribute("totalUsedPoints", totalUsedPoints);

            // 총 주문금액과 주문 횟수
            Map<String, Object> orderInfo = orderService.getTotalOrderInfo(mIdx);
            model.addAttribute("totalAmount", orderInfo.get("totalAmount"));
            model.addAttribute("orderCount", orderInfo.get("orderCount"));
            
            // 주문 상태별 개수
            Map<String, Long> statusCount = orderService.getOrderStatusCount(mIdx);
            model.addAttribute("preparingCount", statusCount.get("배송준비중"));
            model.addAttribute("shippingCount", statusCount.get("배송중"));
            model.addAttribute("completedCount", statusCount.get("배송완료"));
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
