package com.DogProject.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.springframework.context.annotation.DependsOn;

@Entity
@Getter
@Setter
@DependsOn({"board", "member"})
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("댓글 고유 ID")
    private int rIdx;

    @Comment("댓글 작성 날짜")
    private String insertDate;

    @Comment("댓글 수정 날짜")
    private String updateDate;

    @Column(name = "delete_check", length = 1, columnDefinition = "CHAR(1) CHECK (delete_check IN ('Y', 'N'))")
    @Comment("댓글 삭제 여부")
    private String deleteCheck;

    @Comment("댓글 좋아요 수")
    private int likeCount;

    @Comment("댓글 신고 수")
    private int report;

    @Comment("댓글 내용")
    @Column(name = "content", columnDefinition = "VARCHAR(3000)")
    private String content;

    @ManyToOne
    @JoinColumn(name = "bidx")
    @Comment("댓글이 속한 게시글")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "midx")
    @Comment("댓글 작성자")
    private Member member;
}
