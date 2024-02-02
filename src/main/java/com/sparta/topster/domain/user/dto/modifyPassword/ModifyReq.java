package com.sparta.topster.domain.user.dto.modifyPassword;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class ModifyReq {
    String certification;
    String modifyPassword;
}
