package com.sparta.topster.domain.user.dto.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class UpdateReq {

    private String nickname;
    private String intro;

}
