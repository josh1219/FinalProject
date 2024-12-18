package com.DogProject.controller.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.service.MemberService;
import com.DogProject.service.Shopping.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/shop")
public class ShopMyPageController {
    
    private final MemberService memberService;
    private final OrderService orderService;

    @Autowired
    public ShopMyPageController(MemberService memberService, OrderService orderService) {
        this.memberService = memberService;
        this.orderService = orderService;
    }

    private void addUserInfoToModel(Model model, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("USER_INFO")) {
                    String[] userInfo = cookie.getValue().split("★");
                    if (userInfo.length >= 3) {
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
    
    @GetMapping("/mypage")
    public String myPage(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "shop/mypage";
    }
    
    @GetMapping("/orders")
    public String orderList(Model model, HttpServletRequest request, 
                             @RequestParam(required = false) String period,
                             @RequestParam(required = false) String status) {
        addUserInfoToModel(model, request);
        model.addAttribute("orders", orderService.getOrderHistory(period, status));
        return "shop/order";
    }
    
    @GetMapping("/returns")
    public String returnList(Model model, HttpServletRequest request,
                              @RequestParam(required = false) String period) {
        addUserInfoToModel(model, request);
        model.addAttribute("returns", orderService.getReturnHistory(period));
        return "shop/return";
    }
}
