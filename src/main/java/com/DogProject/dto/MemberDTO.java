package com.DogProject.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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

    private String mPassword;

    @NotBlank(message = "생년월일은 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자여야 합니다.")
    private String birthday;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10-11자리의 숫자여야 합니다.")
    private String phone;

    private String gender;
    private String address;
    private String picture;
    private String provider;
    private String mType;

    public boolean isPasswordValid() {
        // mType이 "1"(일반 회원가입)인 경우에만 비밀번호 검증
        if ("1".equals(mType)) {
            if (mPassword == null || mPassword.trim().isEmpty()) {
                return false;
            }
            // 비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 함
            return mPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$");
        }
        return true; // 소셜 로그인의 경우 비밀번호 검증 스킵
    }
}
