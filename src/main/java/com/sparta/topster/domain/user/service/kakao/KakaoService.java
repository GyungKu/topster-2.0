package com.sparta.topster.domain.user.service.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sparta.topster.domain.user.dto.kakao.KakaoUserInfoDto;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.domain.user.entity.UserRoleEnum;
import com.sparta.topster.domain.user.repository.UserRepository;
import com.sparta.topster.global.util.JwtUtil;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final JwtUtil jwtUtil;
    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.secret}")
    private String secret;
    @Value("${kakao.redirect}")
    private String redirectUrl;

    public HttpHeaders kakaoLogin(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);

        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 3. 필요시 회원가입
        User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);

        // 4. JWT 토큰 생성
        String createToken = jwtUtil.createToken(kakaoUser.getUsername(), kakaoUser.getRole());
        String refreshToken = jwtUtil.createRefreshToken(kakaoUser.getUsername());

        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtUtil.AUTHORIZATION_HEADER, createToken);
        headers.add(JwtUtil.REFRESH_TOKEN_PREFIX, refreshToken);

        // 생성 토큰 반환
        return headers;
    }

    private String getToken(String code) throws JsonProcessingException {
        log.info("인가코드 : " + code);
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
            .fromUriString("https://kauth.kakao.com")
            .path("/oauth/token")
            .encode()
            .build()
            .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUrl);
        body.add("code", code);
        body.add("client_secret", secret);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
            .post(uri)
            .headers(headers)
            .body(body);

        // HTTP 요청 보내기
        ResponseEntity<JsonNode> response =
            restClient.post()
                .uri(uri)
                .body(body)
                .header("Content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .toEntity(JsonNode.class);

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = response.getBody();
        return jsonNode.get("access_token").asText();
    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        log.info("accessToken : " + accessToken);
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
            .fromUriString("https://kapi.kakao.com")
            .path("/v2/user/me")
            .encode()
            .build()
            .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
            .post(uri)
            .headers(headers)
            .body(new LinkedMultiValueMap<>()); //body에 따로 보내줄 필요가 없음

        // HTTP 요청 보내기
        ResponseEntity<JsonNode> response = restClient.get()
            .uri(uri)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .toEntity(JsonNode.class);

        JsonNode jsonNode = response.getBody();
        Long id = jsonNode.get("id").asLong(); //Long타입으로 해당하는 id값 받아옴
        String nickname = jsonNode.get("properties")
            .get("nickname").asText();//닉네임 값 가져옴
        String email = jsonNode.get("kakao_account")
            .get("email").asText();//이메일 값 가져옴
        log.info("카카오 사용자 정보 : " + id + ", " + nickname + ", " + email);
        return new KakaoUserInfoDto(id, nickname, email);
    }

    //회원가입 조건
    private User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        //kakaoId가 없다면 null 반환
        User kakaoUser = userRepository.findByKakaoId(kakaoId);
        //null일시 회원가입 시작
        if (kakaoUser == null) {
            // 카카오 사용자 email 동일한 email 가진 회원이 있는지 확인
            String kakaoEmail = kakaoUserInfo.getEmail();
            User sameEmailUser = userRepository.findByEmail(kakaoEmail);
            if (sameEmailUser != null) {
                kakaoUser = sameEmailUser;
                // 기존 회원정보에 카카오 Id 추가
                kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
            } else {
                // 신규 회원가입
                // password: random UUID
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);
                String nickname = kakaoUserInfo.getNickname();

                // email: kakao email
                String email = kakaoUserInfo.getEmail();

                kakaoUser = User.builder()
                    .username(nickname)
                    .nickname(nickname)
                    .password(encodedPassword)
                    .email(email)
                    .role(UserRoleEnum.USER)
                    .kakaoId(kakaoId)
                    .build();
            }
            userRepository.save(kakaoUser);
        }
        return kakaoUser;
    }
}

