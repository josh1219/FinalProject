package com.DogProject.service.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.repository.Shopping.CartItemRepository;
import com.DogProject.repository.Shopping.ProductRepository;
import com.DogProject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MemberService memberService;

    private Member getMemberFromAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Member member = null;
        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
            String provider = "";
            if (auth instanceof OAuth2AuthenticationToken) {
                provider = ((OAuth2AuthenticationToken) auth).getAuthorizedClientRegistrationId();
            }

            if ("google".equals(provider)) {
                String email = oauth2User.getAttribute("email");
                if (email != null) {
                    member = memberService.getMemberByEmail(email);
                }
            } else if ("naver".equals(provider)) {
                Map<String, Object> response = oauth2User.getAttribute("response");
                if (response != null) {
                    String email = (String) response.get("email");
                    if (email != null) {
                        member = memberService.getMemberByEmail(email);
                    }
                }
            } else if ("kakao".equals(provider)) {
                Object kakaoId = oauth2User.getAttribute("id");
                if (kakaoId != null) {
                    String socialId = String.valueOf(kakaoId);
                    member = memberService.findByProviderAndSocialId("kakao", socialId);
                }
            }
        } else {
            member = memberService.getMemberByEmail(auth.getName());
        }

        if (member == null) {
            throw new IllegalStateException("회원을 찾을 수 없습니다.");
        }

        return member;
    }

    // 장바구니 조회
    public List<CartItem> getCartItems(Authentication auth) {
        try {
            Member member = getMemberFromAuth(auth);
            return cartItemRepository.findByMember(member);
        } catch (IllegalStateException e) {
            return Collections.emptyList();
        }
    }

    // 장바구니에 상품 추가
    @Transactional
    public void addToCart(int productId, int quantity, Authentication auth) {
        Member member = getMemberFromAuth(auth);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findByMemberAndProduct(member, product)
                .orElse(new CartItem());

        if (cartItem.getCIdx() == 0) {
            cartItem.setMember(member);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }

        cartItemRepository.save(cartItem);
    }

    // 장바구니 상품 수량 업데이트
    @Transactional
    public void updateCartItemQuantity(int productId, int change, Authentication auth) {
        Member member = getMemberFromAuth(auth);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        CartItem cartItem = cartItemRepository.findByMemberAndProduct(member, product)
                .orElseThrow(() -> new IllegalArgumentException("장바구니에 해당 상품이 없습니다."));

        int newQuantity = cartItem.getQuantity() + change;
        if (newQuantity <= 0) {
            cartItemRepository.deleteByMemberAndProduct(member, product);
        } else {
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }
    }

    // 장바구니에서 상품 제거
    @Transactional
    public void removeFromCart(int productId, Authentication auth) {
        Member member = getMemberFromAuth(auth);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        cartItemRepository.deleteByMemberAndProduct(member, product);
    }

    // 장바구니 비우기
    @Transactional
    public void clearCart(Authentication auth) {
        Member member = getMemberFromAuth(auth);
        cartItemRepository.deleteByMember(member);
    }

    // 상품이 장바구니에 있는지 확인
    public boolean isProductInCart(int productId, Authentication auth) {
        try {
            Member member = getMemberFromAuth(auth);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            return cartItemRepository.findByMemberAndProduct(member, product).isPresent();
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
