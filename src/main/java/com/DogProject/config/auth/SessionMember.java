package com.DogProject.config.auth;

import com.DogProject.entity.Member;
import lombok.Getter;
import java.io.Serializable;

@Getter
public class SessionMember implements Serializable {
    private int mIdx;
    private String name;
    private String mEmail;
    private String picture;
    private String provider;
    private String mType;

    public SessionMember(Member member) {
        this.mIdx = member.getMIdx();
        this.name = member.getName();
        this.mEmail = member.getMEmail();
        this.picture = member.getPicture();
        this.provider = member.getProvider();
        this.mType = member.getMType();
    }
}
