package com.DogProject.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DogDTO {
    private Long id;

    @NotBlank(message = "강아지 이름을 입력해주세요")
    @Size(min = 1, max = 20, message = "이름은 1-20자 사이로 입력해주세요")
    private String dName;

    @NotBlank(message = "강아지 종류를 입력해주세요")
    private String dType;

    @NotNull(message = "생일을 선택해주세요")
    @Past(message = "생일은 현재 날짜보다 이전이어야 합니다")
    private LocalDate dBirth;

    @Min(value = 0, message = "나이는 0보다 작을 수 없습니다")
    private int age;

    @NotNull(message = "성별을 선택해주세요")
    private String gender;

    private MultipartFile image;

    private String imagePath;
}
