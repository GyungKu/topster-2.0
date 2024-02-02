package com.sparta.topster.domain.user.controller;

import com.sparta.topster.domain.user.service.google.GoogleService;
import com.sparta.topster.global.response.RootNoDataRes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/users")
public class GoogleController {

    GoogleService googleService;

    public GoogleController(GoogleService googleService) {
        this.googleService = googleService;
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<RootNoDataRes> googleLogin(@RequestParam String code, @PathVariable String registrationId) {
        googleService.socialLogin(code, registrationId);

        RootNoDataRes res = RootNoDataRes.builder()
            .code("200")
            .message("구글 로그인 성공")
            .build();
        return ResponseEntity.ok().body(res);
    }
}