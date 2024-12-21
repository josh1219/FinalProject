package com.DogProject.controller.Shopping;

import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Controller
@RequestMapping("/shop/cart")
public class CartController {

    private final CartService cartService;
    private final MemberService memberService;

    @Autowired
    public CartController(CartService cartService, MemberService memberService) {
        this.cartService = cartService;
        this.memberService = memberService;
    }

    @GetMapping("")
    public String cartPage(Model model, Principal principal, HttpSession session) {
        try {
            System.out.println("=== Session Debug Info ===");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
            
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
            
            // 회원 주소 정보 가져오기
            if (principal != null) {
                System.out.println("=== Member Debug Info ===");
                // OAuth2 인증 정보에서 이메일 가져오기
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userEmail = null;
                Member member = null;
                
                if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                    
                    // OAuth2 제공자 확인
                    String provider = "";
                    if (authentication instanceof OAuth2AuthenticationToken) {
                        provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
                    }
                    
                    System.out.println("OAuth2 Provider: " + provider);
                    
                    // 제공자별로 이메일 속성 이름이 다름
                    if ("google".equals(provider)) {
                        userEmail = oauth2User.getAttribute("email");
                    } else if ("naver".equals(provider)) {
                        Map<String, Object> response = oauth2User.getAttribute("response");
                        if (response != null) {
                            userEmail = (String) response.get("email");
                        }
                    } else if ("kakao".equals(provider)) {
                        // 카카오의 경우 ID를 통해 회원 조회
                        Object kakaoId = oauth2User.getAttribute("id");
                        String socialId = kakaoId != null ? String.valueOf(kakaoId) : null;
                        if (socialId != null) {
                            member = memberService.findByProviderAndSocialId("kakao", socialId);
                            if (member != null) {
                                userEmail = member.getMEmail();
                            }
                            System.out.println("Kakao social ID: " + socialId);
                        }
                    }
                    System.out.println("OAuth2 email: " + userEmail);
                } else {
                    userEmail = principal.getName();
                    System.out.println("Regular email: " + userEmail);
                }
                
                if (userEmail != null && member == null) {
                    member = memberService.getMemberByEmail(userEmail);
                }
                
                if (member != null) {
                    System.out.println("Found member with email: " + member.getMEmail());
                    System.out.println("Member m_idx: " + member.getMIdx());
                    
                    try {
                        Member memberWithAddress = memberService.findByMIdx(member.getMIdx());
                        System.out.println("Found member by m_idx: " + memberWithAddress.getMIdx());
                        System.out.println("Raw address: " + memberWithAddress.getAddress());
                        
                        if (memberWithAddress.getAddress() != null) {
                            model.addAttribute("memberAddress", memberWithAddress.getAddress());
                            System.out.println("Added address to model: " + memberWithAddress.getAddress());
                        } else {
                            System.out.println("Address is null");
                        }
                        
                        // 전화번호 추가
                        if (memberWithAddress.getPhone() != null) {
                            String phone = memberWithAddress.getPhone();
                            // 전화번호에 하이픈 추가
                            if (phone.length() == 11 && !phone.contains("-")) {
                                phone = phone.substring(0, 3) + "-" 
                                     + phone.substring(3, 7) + "-"
                                     + phone.substring(7);
                            }
                            model.addAttribute("memberPhone", phone);
                            System.out.println("Added phone to model: " + phone);
                        } else {
                            System.out.println("Phone is null");
                        }
                    } catch (Exception e) {
                        System.out.println("Error finding member by m_idx: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Member not found for email: " + userEmail);
                }
            } else {
                System.out.println("Principal is null - user not logged in");
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
