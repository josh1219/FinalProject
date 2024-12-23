package com.DogProject.service.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.Order;
import com.DogProject.entity.Shopping.OrderItem;
import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Product;
import com.DogProject.repository.Shopping.CartItemRepository;
import com.DogProject.repository.Shopping.OrderRepository;
import com.DogProject.service.MemberService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                           String address, String shippingRequest, int usedPoint) {
        // 포인트 사용 가능 여부 확인
        if (usedPoint > member.getPoint()) {
            throw new IllegalStateException("사용 가능한 포인트를 초과했습니다.");
        }

        // 상품 조회
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
        }

        // 재고 확인
        if (product.getStock() < quantity) {
            throw new IllegalStateException("상품의 재고가 부족합니다.");
        }

        // 포인트 차감
        if (usedPoint > 0) {
            member.setPoint(member.getPoint() - usedPoint);
            memberService.updateMemberPoint(member);
        }

        // 주문 생성
        Order order = Order.builder()
                .member(member)
                .orderDate(LocalDateTime.now())
                .status("배송준비중")
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .recipientAddress(address)
                .shippingRequest(shippingRequest)
                .usedPoint(usedPoint)
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
        order.setTotalAmount(product.getPrice() * quantity - usedPoint);

        // 재고 감소
        product.setStock(product.getStock() - quantity);
        productService.saveProduct(product);

        // 주문 저장
        return orderRepository.save(order);
    }

    @Transactional
    public Order createOrderFromCart(Member member, String recipientName,
                                   String recipientPhone, String address,
                                   String shippingRequest, int usedPoint) {
        // 포인트 사용 가능 여부 확인
        if (usedPoint > member.getPoint()) {
            throw new IllegalStateException("사용 가능한 포인트를 초과했습니다.");
        }

        // 장바구니 아이템 조회
        List<CartItem> cartItems = cartService.getCartItems(member);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }

        // 포인트 차감
        if (usedPoint > 0) {
            member.setPoint(member.getPoint() - usedPoint);
            memberService.updateMemberPoint(member);
        }

        // 2. 주문 생성
        Order order = Order.builder()
                .member(member)
                .orderDate(LocalDateTime.now())
                .status("배송준비중")
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .recipientAddress(address)
                .shippingRequest(shippingRequest)
                .usedPoint(usedPoint)
                .build();

        // 3. 주문 아이템 생성 및 연결
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // 재고 확인
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException(
                    String.format("상품 '%s'의 재고가 부족합니다.", product.getName())
                );
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice() * cartItem.getQuantity())
                    .build();
            
            orderItems.add(orderItem);
            totalAmount += product.getPrice() * cartItem.getQuantity();

            // 재고 감소
            product.setStock(product.getStock() - cartItem.getQuantity());
            productService.saveProduct(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount - usedPoint);

        // 장바구니 비우기
        cartService.clearCart(member);

        // 주문 저장
        return orderRepository.save(order);
    }

    // 회원의 총 사용 포인트 조회
    public int getTotalUsedPoints(int mIdx) {
        // 회원의 모든 주문을 조회
        List<Order> orders = orderRepository.findByMember_mIdx(mIdx);
        System.out.println("주문 개수: " + orders.size()); // 디버깅용 로그
        
        int totalPoints = 0;
        for (Order order : orders) {
            totalPoints += order.getUsedPoint();
            System.out.println("주문번호: " + order.getOIdx() + ", 사용포인트: " + order.getUsedPoint()); // 디버깅용 로그
        }
        System.out.println("총 사용 포인트: " + totalPoints); // 디버깅용 로그
        return totalPoints;
    }

    // 회원의 총 주문 정보를 조회합니다.
    public Map<String, Object> getTotalOrderInfo(int mIdx) {
        Map<String, Object> result = new HashMap<>();
        
        // 회원의 모든 주문을 조회
        List<Order> orders = orderRepository.findByMember_mIdx(mIdx);
        System.out.println("총 주문 개수: " + orders.size()); // 디버깅용 로그
        
        int totalAmount = 0;
        for (Order order : orders) {
            totalAmount += (order.getTotalAmount() + order.getUsedPoint());
            System.out.println("주문번호: " + order.getOIdx() + 
                             ", 결제금액: " + order.getTotalAmount() + 
                             ", 사용포인트: " + order.getUsedPoint()); // 디버깅용 로그
        }
        System.out.println("총 주문금액: " + totalAmount); // 디버깅용 로그
                
        result.put("totalAmount", totalAmount);
        result.put("orderCount", orders.size());
        
        return result;
    }

    // 주문 생성 시 상태를 "배송준비중"으로 설정
    public Order saveOrder(Order order) {
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        if (order.getStatus() == null) {
            order.setStatus("배송준비중");
        }
        return orderRepository.save(order);
    }

    // 매일 자정에 실행되는 배송 상태 업데이트 스케줄러
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findAll();
        
        for (Order order : orders) {
            LocalDateTime orderDate = order.getOrderDate();
            
            // 주문 후 1일 경과: 배송중
            if (orderDate.plusDays(1).isBefore(now) && "배송준비중".equals(order.getStatus())) {
                order.setStatus("배송중");
                orderRepository.save(order);
            }
            // 주문 후 3일 경과: 배송완료
            else if (orderDate.plusDays(3).isBefore(now) && "배송중".equals(order.getStatus())) {
                order.setStatus("배송완료");
                orderRepository.save(order);
            }
        }
    }
}
