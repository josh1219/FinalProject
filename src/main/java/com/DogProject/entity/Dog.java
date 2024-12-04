package com.DogProject.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.List;

@Entity
@Getter
@Setter
public class Dog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("강아지 고유 ID")
    private int dIdx;

    @Comment("강아지 이름")
    private String name;

    @Comment("강아지 나이")
    private int age;

    @Comment("강아지 생일")
    private String birthday;

    @Comment("강아지 성별")
    private String gender;

    @Comment("강아지 유형 또는 AI 데이터 타입")
    private String dType;

    @ManyToOne
    @JoinColumn(name = "midx")
    @Comment("강아지와 연결된 회원 고유 ID")
    private Member member;

    @OneToMany(mappedBy = "dog")
    @Comment("강아지의 예방 접종 기록")
    private List<Vaccination> vaccinations;

    @OneToMany(mappedBy = "dog")
    @Comment("강아지의 일정")
    private List<Schedule> schedules;
}
