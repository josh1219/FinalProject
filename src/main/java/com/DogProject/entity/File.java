package com.DogProject.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("파일 고유 ID")
    private int fIdx;

    @Comment("파일 저장 유형 (1: 회원, 2: 게시글, 3: 강아지 등등)")
    private int fType;

    @Comment("파일 저장 이름")
    private String fileSaveName;

    @Comment("파일 실제 이름")
    private String fileRealName;
}
