package com.sparta.topster.domain.user.entity;

import com.sparta.topster.domain.BaseEntity;
import com.sparta.topster.domain.user.dto.update.UpdateReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column
    private String intro;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private UserRoleEnum role;

    @Column
    private Long kakaoId;

    @Column
    private Long googleId;

    @Builder
    public User(String username, String nickname, String password, String email, String intro, UserRoleEnum role, Long kakaoId, Long googleId){
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.intro = intro;
        this.role = role;
        this.kakaoId = kakaoId;
        this.googleId = googleId;
    }

    public void updateIntro(UpdateReq updateReq) {
        this.nickname = updateReq.getNickname();
        this.intro = updateReq.getIntro();
    }

    public User kakaoIdUpdate(Long kakaoId) {
        this.kakaoId = kakaoId;
        return this;
    }

    public User GoogleIdUpdate(Long googleId){
        this.googleId = googleId;
        return this;
    }

    public void modifyPassword(String password){
        this.password = password;
    }

}
