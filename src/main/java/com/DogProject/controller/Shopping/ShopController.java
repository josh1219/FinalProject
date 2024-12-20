package com.DogProject.controller.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    @Autowired
    private MemberService memberService;

    private final ProductService productService;

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
    public String shopDetail(@PathVariable Long id, Model model, HttpServletRequest request, Principal principal) {
        addUserInfoToModel(model, request, principal);
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "shop/shop-detail";
    }

    @GetMapping("/purchase/{productId}")
    public String purchasePage(@PathVariable Long productId, 
                             @RequestParam(required = false) Integer quantity,
                             Model model,
                             Authentication authentication) {
        // 로그인 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/member/login";
        }

        // 상품 정보 조회
        Product product = productService.getProductById(productId);
        if (product == null) {
            return "redirect:/shop";
        }

        // 구매 수량이 지정되지 않은 경우 기본값 1로 설정
        if (quantity == null || quantity < 1) {
            quantity = 1;
        }

        // 사용자 정보 조회 및 모델에 추가
        Member member = memberService.findBymEmail(authentication.getName());
        if (member != null) {
            model.addAttribute("memberName", member.getName());
            model.addAttribute("memberPhone", member.getPhone());
            model.addAttribute("memberAddress", member.getAddress());
            model.addAttribute("isLoggedIn", true);
        }

        model.addAttribute("product", product);
        model.addAttribute("quantity", quantity);
        
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
}
