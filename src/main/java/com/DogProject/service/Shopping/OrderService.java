package com.DogProject.service.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Order;
import com.DogProject.entity.Shopping.OrderItem;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.repository.Shopping.CartItemRepository;
import com.DogProject.repository.Shopping.OrderRepository;
import com.DogProject.service.MemberService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberService memberService;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;
    @Autowired
    private CartService cartService;

    public List<Order> getOrderHistory(String userEmail, String period, String status) {
        int mIdx = memberService.findBymEmail(userEmail).getMIdx();
        LocalDateTime startDate = LocalDateTime.now().minusYears(100); // 모든 주문 내역을 가져오기 위해 충분히 과거 시점 설정
        
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByMember_mIdxAndOrderDateAfterAndStatusOrderByOrderDateDesc(mIdx, startDate, status);
        }
        return orderRepository.findByMember_mIdxAndOrderDateAfterOrderByOrderDateDesc(mIdx, startDate);
    }

    public List<Order> getReturnHistory(String userEmail, String period) {
        LocalDateTime startDate = calculateStartDate(period);
                int mIdx = memberService.findBymEmail(userEmail).getMIdx();
                return orderRepository.findByMember_mIdxAndOrderDateAfterAndStatusInOrderByOrderDateDesc(
                    mIdx, 
                    startDate, 
                    List.of("RETURNED", "RETURN_REQUESTED")
                );
            }
        
            private LocalDateTime calculateStartDate(String period) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'calculateStartDate'");
            }
        
            @Transactional
    public Order createOrder(Member member, int productId, int quantity,
                           String recipientName, String recipientPhone,
                           String address, String shippingRequest) {
        // 상품 조회
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }

        // 재고 확인
        if (product.getStock() < quantity) {
            throw new IllegalStateException("상품의 재고가 부족합니다.");
        }

        // 주문 생성
        Order order = Order.builder()
                .member(member)
                .orderDate(LocalDateTime.now())
                .status("ORDERED")
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .recipientAddress(address)
                .shippingRequest(shippingRequest)
                .build();

        // 주문 상품 생성
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice() * quantity)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        order.setTotalAmount(product.getPrice() * quantity);

        // 재고 감소
        product.setStock(product.getStock() - quantity);
        productService.saveProduct(product);

        // 주문 저장
        return orderRepository.save(order);
    }

    @Transactional
    public Order createOrderFromCart(Member member, String recipientName, String recipientPhone, String recipientAddress, String shippingRequest) {
        // 1. 장바구니 아이템 조회
        List<CartItem> cartItems = cartService.getCartItems(SecurityContextHolder.getContext().getAuthentication());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }

        // 2. 주문 생성
        Order order = Order.builder()
                .member(member)
                .orderDate(LocalDateTime.now())
                .status("ORDERED")
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .recipientAddress(recipientAddress)
                .shippingRequest(shippingRequest)
                .build();

        // 3. 주문 아이템 생성 및 연결
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProduct().getPrice() * cartItem.getQuantity())
                    .build();
            orderItems.add(orderItem);
            totalAmount += orderItem.getPrice();
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 4. 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 5. 장바구니 비우기
        cartService.clearCart(SecurityContextHolder.getContext().getAuthentication());

        return savedOrder;
    }

    
}
