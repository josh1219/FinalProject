package com.DogProject.entity;

import javax.persistence.Table;

import javax.persistence.*;

import lombok.*;

@Entity
@Table(name = "tour_like")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_idx")
    private Member member;

    private String placeName;  // 관광지 이름
    
    private String category;   // 관광지 카테고리
    
    private String address;    // 관광지 주소
    
    private String phoneNumber;  // 관광지 전화번호
    
    @Column(name = "created_at")
    private String createdAt;  // 찜한 날짜
}
