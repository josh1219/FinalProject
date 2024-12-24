package com.DogProject.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "board_view", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"midx", "b_idx", "view_date"})
})
public class BoardView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "midx")
    @Comment("조회한 회원")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_idx")
    @Comment("조회한 게시글")
    private Board board;

    @Column(name = "view_date")
    @Comment("조회한 날짜")
    private LocalDate viewDate;
}
