package com.sparta.topster.domain.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.topster.domain.user.service.kakao.KakaoService;
import com.sparta.topster.global.response.RootNoDataRes;
import com.sparta.topster.global.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        HttpHeaders headers = kakaoService.kakaoLogin(code);
        headers.setLocation(URI.create("/"));

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).headers(headers).build();
    }

}
