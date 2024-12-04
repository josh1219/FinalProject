package com.DogProject.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("게시글 고유 ID")
    private int bIdx;

    @Comment("게시글 유형")
    private String bType;

    @Comment("게시글 작성 날짜")
    private String insertDate;

    @Comment("게시글 수정 날짜")
    private String updateDate;

    @Column(name = "delete_check", length = 1, columnDefinition = "CHAR(1) CHECK (delete_check IN ('Y', 'N'))")
    @Comment("게시글 삭제 여부")
    private String deleteCheck;

    @Comment("게시글 조회수")
    private int readRate;

    @Comment("게시글 좋아요 수")
    private int likeCount;  // 예약어 "like" 대신 "likeCount"로 수정

    @Comment("게시글 신고 수")
    private int report;

    @Comment("게시글 제목")
    private String title;

    @Comment("게시글 내용")
    @Column(name = "content", columnDefinition = "VARCHAR(3000)")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midx", nullable = false)
    @Comment("게시글 작성자")
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    @Comment("게시글의 댓글 리스트")
    private List<Reply> replies;
}
