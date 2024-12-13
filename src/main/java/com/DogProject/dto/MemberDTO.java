package com.DogProject.dto;

import com.DogProject.entity.Member;
import com.DogProject.constant.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MemberDTO {
    
    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String mEmail;

    @NotBlank(message = "생년월일은 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자여야 합니다.")
    private String birthday;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10-11자리의 숫자여야 합니다.")
    private String phone;

    @NotBlank(message = "성별을 선택해주세요.")
    private String gender;

    @NotBlank(message = "주소는 필수 입력값입니다.")
    private String address;

    private String picture;
    private String provider;
    private String socialId;

    public Member toEntity() {
        Member member = Member.socialBuilder()
            .name(name)
            .mEmail(mEmail.toLowerCase())
            .picture(picture)
            .provider(provider)
            .socialId(socialId)
            .birthday(birthday)
            .phone(phone)
            .gender(gender)
            .address(address)
            .enabled(true)
            .build();
        
        member.setRole(Role.USER);
        member.setPoint(0);
        member.setLastLoginDate(LocalDateTime.now());
        
        return member;
    }
}
