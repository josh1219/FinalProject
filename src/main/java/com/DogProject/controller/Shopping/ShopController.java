package com.DogProject.controller.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.repository.Shopping.CartItemRepository;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.ProductService;
import com.DogProject.service.Shopping.CartService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CartService cartService;

    private final ProductService productService;

    private final CartItemRepository cartItemRepository;

    @GetMapping("")
    public String shopMain(Model model, HttpServletRequest request, Principal principal,
                      @RequestParam(required = false) String category,
                      @RequestParam(required = false) String keyword) {
        addUserInfoToModel(model, request, principal);
        List<Product> products;
        
        if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("category", category);
        } else if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchProducts(keyword);
        } else {
            products = productService.getAllProducts();
        }
        
        model.addAttribute("products", products);
        return "shop/shop";
    }

    @GetMapping("/categories")
    public String shopCategory(Model model) {
        return "shop/category";
    }

    @GetMapping("/category/{category}")
    public String shopByCategory(@PathVariable String category, Model model) {
        model.addAttribute("products", productService.getProductsByCategory(category));
        return "shop/shop";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam String keyword, Model model) {
        model.addAttribute("products", productService.searchProducts(keyword));
        return "shop/shop";
    }

    @GetMapping("/shop")
    public String shopMain(@RequestParam(required = false) String sort, Model model) {
        List<Product> products;
        if (sort != null && !sort.isEmpty()) {
            products = productService.getProductsSorted(sort);
        } else {
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("selectedSort", sort != null ? sort : "new"); // 현재 선택된 정렬 옵션 전달
        return "shop/shop";
    }

    @GetMapping("/detail/{id}")
    public String shopDetail(@PathVariable int id, Model model, HttpServletRequest request, Principal principal) {
        addUserInfoToModel(model, request, principal);
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        model.addAttribute("product", product);
        return "shop/shop-detail";
    }

    @GetMapping("/purchase/{productId}")
    public String purchasePage(@PathVariable int productId, 
                             @RequestParam(required = false) Integer quantity,
                             Model model,
                             Authentication authentication) {
        // 로그인 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }

        // 상품 정보 조회
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 구매 수량이 지정되지 않은 경우 기본값 1로 설정
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }

        // 사용자 정보 조회 및 모델에 추가
        String userEmail = null;
        
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            // OAuth2 제공자 확인
            String provider = "";
            if (authentication instanceof OAuth2AuthenticationToken) {
                provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            }
            
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
                    Member kakaoMember = memberService.findByProviderAndSocialId("kakao", socialId);
                    if (kakaoMember != null) {
                        userEmail = kakaoMember.getMEmail();
                    }
                }
            }
        } else {
            userEmail = authentication.getName();
        }

        if (userEmail != null) {
            Member member = memberService.getMemberByEmail(userEmail);
            if (member != null) {
                try {
                    Member memberWithAddress = memberService.findByMIdx(member.getMIdx());
                    if (memberWithAddress != null) {
                        if (memberWithAddress.getAddress() != null) {
                            model.addAttribute("memberAddress", memberWithAddress.getAddress());
                        }
                        
                        // 전화번호 처리
                        if (memberWithAddress.getPhone() != null) {
                            String phone = memberWithAddress.getPhone();
                            // 전화번호에 하이픈 추가
                            if (phone.length() == 11 && !phone.contains("-")) {
                                phone = phone.substring(0, 3) + "-" 
                                     + phone.substring(3, 7) + "-"
                                     + phone.substring(7);
                            }
                            model.addAttribute("memberPhone", phone);
                        }
                        
                        model.addAttribute("memberName", memberWithAddress.getName());
                        // 포인트 정보 추가
                        model.addAttribute("memberPoint", memberWithAddress.getPoint());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("quantity", quantity);
        model.addAttribute("isLoggedIn", true);

        return "shop/purchase";
    }

    @GetMapping("/cart/purchase")
    public String cartPurchasePage(Model model, Authentication authentication, HttpSession session) {
        // 로그인 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }

        // 현재 로그인한 회원 정보 가져오기
        Member member = getCurrentMember(authentication);
        if (member != null) {
            if (member.getAddress() != null) {
                model.addAttribute("memberAddress", member.getAddress());
            }
            if (member.getPhone() != null) {
                String phone = member.getPhone();
                if (phone.length() == 11 && !phone.contains("-")) {
                    phone = phone.substring(0, 3) + "-" 
                         + phone.substring(3, 7) + "-"
                         + phone.substring(7);
                }
                model.addAttribute("memberPhone", phone);
            }
            model.addAttribute("memberName", member.getName());
            // 포인트 정보 추가
            model.addAttribute("memberPoint", member.getPoint());
        }

        // 장바구니 정보 조회
        List<CartItem> cartItems = cartItemRepository.findByMember(member);
        if (cartItems.isEmpty()) {
            return "redirect:/shop/cart?error=empty";
        }

        // 총 금액 계산
        int totalAmount = cartItems.stream()
                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("fromCart", true);

        return "shop/purchase";
    }

    @PostMapping("/removeLastTwoDetailImages")
    @ResponseBody
    public ResponseEntity<String> removeLastTwoDetailImages() {
        try {
            productService.removeLastTwoDetailImages();
            return ResponseEntity.ok("Successfully removed last two detail images from all products");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing detail images: " + e.getMessage());
        }
    }

    private void addUserInfoToModel(Model model, HttpServletRequest request, Principal principal) {
        if (principal != null) {
            Member member = memberService.findBymEmail(principal.getName());
            if (member != null) {
                model.addAttribute("userName", member.getName());
                model.addAttribute("point", member.getPoint());
                model.addAttribute("isLoggedIn", true);
            }
        }
        // 쿠키 처리는 유지
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("USER_INFO")) {
                    String[] userInfo = cookie.getValue().split("★");
                    if (userInfo.length >= 3 && !model.containsAttribute("userName")) {
                        model.addAttribute("userName", userInfo[2]);
                        Member member = memberService.findBymEmail(userInfo[1]);
                        if (member != null) {
                            model.addAttribute("point", member.getPoint());
                        }
                    }
                    break;
                }
            }
        }
    }

    private Member getCurrentMember(Authentication authentication) {
        String userEmail = null;
        
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            // OAuth2 제공자 확인
            String provider = "";
            if (authentication instanceof OAuth2AuthenticationToken) {
                provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            }
            
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
                    Member kakaoMember = memberService.findByProviderAndSocialId("kakao", socialId);
                    if (kakaoMember != null) {
                        userEmail = kakaoMember.getMEmail();
                    }
                }
            }
        } else {
            userEmail = authentication.getName();
        }

        if (userEmail != null) {
            return memberService.getMemberByEmail(userEmail);
        } else {
            return null;
        }
    }
}
