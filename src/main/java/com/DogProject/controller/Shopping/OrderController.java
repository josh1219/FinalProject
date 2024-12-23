package com.DogProject.controller.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.Order;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.CartService;
import com.DogProject.service.Shopping.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final CartService cartService;

    /**
     * 주문내역 페이지를 보여줍니다.
     */
    @GetMapping("/history")
    public String orderHistory(Model model, Authentication authentication) {
        // 로그인 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }

        Member member = getCurrentMember(authentication);
        if (member != null) {
            // 주문 내역 조회
            List<Order> orders = orderService.getOrderHistory(member.getMEmail(), null, null);
            model.addAttribute("orders", orders);
            
            // 회원 정보 추가
            model.addAttribute("memberName", member.getName());
            model.addAttribute("memberPhone", member.getPhone());
            model.addAttribute("memberAddress", member.getAddress());
        }

        return "shop/order-history";
    }

    /**
     * 주문 상세 정보를 조회합니다.
     */
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model, Authentication authentication) {
        // 로그인 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }

        return "shop/order-detail"; // 주문 상세 페이지 뷰 (아직 미구현)
    }

    /**
     * 주문을 취소합니다.
     */
    @PostMapping("/cancel/{orderId}")
    @ResponseBody
    public String cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        // 로그인 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            return "로그인이 필요합니다.";
        }

        try {
            // 주문 취소 로직 구현 예정
            // orderService.cancelOrder(orderId);
            return "주문이 성공적으로 취소되었습니다.";
        } catch (Exception e) {
            return "주문 취소 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 결제 처리를 위한 API 엔드포인트
     */
    @PostMapping("/api/purchase")
    @ResponseBody
    public Map<String, Object> processPurchase(@RequestBody Map<String, Object> purchaseData, 
                                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        try {
            // 현재 로그인한 회원 정보 가져오기
            Member member = getCurrentMember(authentication);
            if (member == null) {
                return Map.of("success", false, "message", "회원 정보를 찾을 수 없습니다.");
            }

            // 장바구니에서 주문하는 경우
            if (purchaseData.get("fromCart") != null && (boolean) purchaseData.get("fromCart")) {
                // 포인트 사용 금액 가져오기
                int usedPoint = purchaseData.get("usedPoint") != null ? 
                    ((Number) purchaseData.get("usedPoint")).intValue() : 0;

                Order savedOrder = orderService.createOrderFromCart(
                    member,
                    (String) purchaseData.get("recipientName"),
                    (String) purchaseData.get("recipientPhone"),
                    (String) purchaseData.get("recipientAddress"),
                    (String) purchaseData.get("shippingRequest"),
                    usedPoint
                );
                
                return Map.of(
                    "success", true,
                    "message", "주문이 성공적으로 처리되었습니다.",
                    "orderId", savedOrder.getOIdx()
                );
            }
            // 단일 상품 주문하는 경우
            else if (purchaseData.get("productId") != null && purchaseData.get("quantity") != null) {
                // 포인트 사용 금액 가져오기
                int usedPoint = purchaseData.get("usedPoint") != null ? 
                    ((Number) purchaseData.get("usedPoint")).intValue() : 0;

                Order savedOrder = orderService.createOrder(
                    member,
                    ((Number) purchaseData.get("productId")).intValue(),
                    ((Number) purchaseData.get("quantity")).intValue(),
                    (String) purchaseData.get("recipientName"),
                    (String) purchaseData.get("recipientPhone"),
                    (String) purchaseData.get("recipientAddress"),
                    (String) purchaseData.get("shippingRequest"),
                    usedPoint
                );
                
                return Map.of(
                    "success", true,
                    "message", "주문이 성공적으로 처리되었습니다.",
                    "orderId", savedOrder.getOIdx()
                );
            }

            return Map.of("success", false, "message", "잘못된 주문 요청입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 현재 로그인한 회원 정보를 가져옵니다.
     */
    private Member getCurrentMember(Authentication authentication) {
        String userEmail = null;
        Member member = null;

        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            
            if ("google".equals(provider)) {
                userEmail = oauth2User.getAttribute("email");
            } else if ("naver".equals(provider)) {
                Map<String, Object> response = oauth2User.getAttribute("response");
                if (response != null) {
                    userEmail = (String) response.get("email");
                }
            } else if ("kakao".equals(provider)) {
                Object kakaoId = oauth2User.getAttribute("id");
                String socialId = kakaoId != null ? String.valueOf(kakaoId) : null;
                if (socialId != null) {
                    member = memberService.findByProviderAndSocialId("kakao", socialId);
                }
            }
        } else {
            userEmail = authentication.getName();
        }

        if (userEmail != null && member == null) {
            member = memberService.getMemberByEmail(userEmail);
        }

        return member;
    }
}
