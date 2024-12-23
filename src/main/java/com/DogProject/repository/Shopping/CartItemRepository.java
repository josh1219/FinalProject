package com.DogProject.repository.Shopping;

import com.DogProject.entity.Member;
import com.DogProject.entity.Shopping.CartItem;
import com.DogProject.entity.Shopping.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByMember(Member member);
    Optional<CartItem> findByMemberAndProduct(Member member, Product product);
    void deleteByMemberAndProduct(Member member, Product product);
    void deleteByMember(Member member);
}
