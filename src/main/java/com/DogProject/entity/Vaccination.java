package com.DogProject.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
public class Vaccination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("예방접종 고유 ID")
    private int yIdx;

    @Comment("예방접종 유형")
    private String yType;

    @Comment("예방접종 날짜")
    private String yDate;

    @ManyToOne
    @JoinColumn(name = "didx")
    @Comment("예방접종을 받은 강아지")
    private Dog dog;
}

