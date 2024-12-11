    package com.DogProject.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Path {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("경로 고유 ID")
    private int pIdx;

    @Column(nullable = false, columnDefinition = "DOUBLE")
    @Comment("경로의 위도")
    private double latitude;

    @Column(nullable = false, columnDefinition = "DOUBLE")
    @Comment("경로의 경도")
    private double longitude;

    @Column(nullable = false)
    @Comment("경로 순서")
    private int sequence;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Comment("경로 생성 일시")
    private LocalDateTime createTime;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Comment("경로 수정 일시")
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "m_idx", nullable = false)
    @Comment("회원과의 연관 관계 (Foreign Key)")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ws_idx", nullable = false)
    @Comment("산책 세션과의 연관 관계 (Foreign Key)")
    private WalkSession walkSession;
}
