package com.sparta.topster.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.topster.domain.user.service.kakao.KakaoService;
import com.sparta.topster.global.response.RootNoDataRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class KakaoController {

   private final KakaoService kakaoService;

    @GetMapping("/kakao/callback")
    public ResponseEntity<RootNoDataRes> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        HttpHeaders headers = kakaoService.kakaoLogin(code);

        RootNoDataRes res = RootNoDataRes.builder()
            .code("200")
            .message("카카오 로그인 성공")
            .build();

        return ResponseEntity.ok().headers(headers).body(res);
    }

}
