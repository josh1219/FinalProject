package com.DogProject.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("일정 고유 ID")
    private int saIdx;

    @Comment("일정 제목")
    private String title;

    @Comment("일정 내용")
    private String content;

    @ManyToOne
    @JoinColumn(name = "midx")
    @Comment("일정을 작성한 회원")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "didx")
    @Comment("일정과 관련된 강아지")
    private Dog dog;
}

