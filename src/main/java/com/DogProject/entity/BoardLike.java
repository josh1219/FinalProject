package com.DogProject.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "board_like", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"midx", "b_idx"})
})
public class BoardLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midx")
    @Comment("좋아요를 누른 회원")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_idx")
    @Comment("좋아요가 눌린 게시글")
    private Board board;
}
