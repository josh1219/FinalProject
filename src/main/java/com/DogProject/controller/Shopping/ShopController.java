package com.DogProject.controller.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import java.util.List;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    @Autowired
    private MemberService memberService;

    private final ProductService productService;

    @GetMapping("")
    public String shopMain(Model model, HttpServletRequest request, 
                      @RequestParam(required = false) String category,
                      @RequestParam(required = false) String keyword) {
        // 쿠키에서 사용자 정보 가져오기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("USER_INFO")) {
                    String[] userInfo = cookie.getValue().split("★");
                    if (userInfo.length >= 3) {
                        model.addAttribute("userName", userInfo[2]); // name은 세 번째 값
                        // DB에서 멤버 정보를 가져와서 포인트 설정
                        Member member = memberService.findBymEmail(userInfo[1]);
                        if (member != null) {
                            model.addAttribute("point", member.getPoint());
                        }
                    }
                    break;
                }
            }
        }
        
        List<Product> products;
        
        if (category != null && !category.isEmpty()) {
            products = productService.getProductsByCategory(category);
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
}
