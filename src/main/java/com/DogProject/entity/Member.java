package com.DogProject.entity;

import javax.persistence.*;
import com.DogProject.constant.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("회원 고유 ID")
    private int mIdx;

    @Column(nullable = false, length = 50)
    @Comment("회원 이름")
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    @Comment("회원 이메일")
    private String mEmail;

    // @Column(length = 255)  
    // @Comment("회원 비밀번호")
    // private String mPassword;

    @Column(length = 10)  
    @Comment("회원 생년월일 (yyyy-MM-dd)")
    private String birthday;

    @Column(length = 20)
    @Comment("회원 전화번호")
    private String phone;

    @Column(length = 10)
    @Comment("회원 성별 (예: 남성/여성)")
    private String gender;

    @Column(length = 255, nullable = false)
    @Comment("회원 주소")
    private String address;

    @Column(length = 255)
    @Comment("프로필 이미지 URL")
    private String picture;

    @Column(length = 50)
    @Comment("소셜 로그인 제공자")
    private String provider;

    @Column(length = 255)
    @Comment("소셜 로그인 ID")
    private String socialId;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    @Comment("회원 포인트 (적립금)")
    private int point = 0;

    @Column(nullable = false)
    @Comment("계정 활성화 여부")
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(columnDefinition = "TIMESTAMP(0)")
    @Comment("마지막 로그인 날짜")
    private LocalDateTime lastLoginDate;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Comment("회원이 등록한 강아지 리스트")
    private List<Dog> dogs = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Comment("회원이 작성한 게시글 리스트")
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Comment("회원이 보낸 채팅 리스트")
    private List<Chat> sentChats = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Comment("회원이 받은 채팅 리스트")
    private List<Chat> receivedChats = new ArrayList<>();



    /**
     * 소셜 로그인 회원을 위한 빌더
     */
    @Builder(builderMethodName = "socialBuilder")
    public Member(String name, String mEmail, String mPassword, String picture, String provider, 
                 boolean enabled, String socialId, String birthday, String phone, String gender, String address) {
        this.name = name;
        this.mEmail = mEmail;
        //this.mPassword = mPassword;
        this.picture = picture;
        this.provider = provider;
        this.enabled = enabled;
        this.role = Role.USER;
        this.socialId = socialId;
        this.birthday = birthday;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
        this.point = 0;
    }

    /**
     * 일반 회원가입을 위한 빌더
     */
    @Builder(builderMethodName = "userBuilder")
    public Member(String name, String mEmail, String mPassword, String birthday, String phone,
                 String gender, String address) {
        this.name = name;
        this.mEmail = mEmail;
        //this.mPassword = mPassword;
        this.birthday = birthday;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
        this.enabled = true;
        this.point = 0;
        this.role = Role.USER;
        this.provider = "local";  // 명시적으로 provider를 'local'로 설정
    }

    /**
     * OAuth2 정보 업데이트
     */
    public void updateOAuth2Info(String name, String picture, String provider, String socialId) {
        if (name != null) this.name = name;
        if (picture != null) this.picture = picture;
        if (provider != null) this.provider = provider;
        if (socialId != null) this.socialId = socialId;
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLoginDate() {
        this.lastLoginDate = LocalDateTime.now().withNano(0);
    }

    /**
     * 계정 활성화 상태 확인 및 업데이트
     * 6개월 이상 로그인하지 않은 계정은 비활성화
     */
    public void updateAccountStatus() {
        if (this.lastLoginDate != null) {
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            this.enabled = this.lastLoginDate.isAfter(sixMonthsAgo);
        }
    }

    /**
     * 계정 활성화 상태를 문자열로 반환
     */
    public String getAccountStatus() {
        return this.enabled ? "활성화" : "비활성화";
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
