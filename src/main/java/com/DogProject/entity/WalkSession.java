package com.DogProject.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class WalkSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("산책 세션 고유 ID")
    private int wsIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_idx", nullable = false)
    @Comment("회원과의 연관 관계 (Foreign Key)")
    private Member member;

    @Column(nullable = false, columnDefinition = "DOUBLE")
    @Comment("총 산책 거리")
    private double totalDistance;

    @Column(nullable = false)
    @Comment("산책 날짜")
    private LocalDateTime walkDate;

    @OneToMany(mappedBy = "walkSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Path> paths = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        walkDate = LocalDateTime.now();
    }
}
