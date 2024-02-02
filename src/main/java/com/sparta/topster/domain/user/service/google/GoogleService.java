package com.sparta.topster.domain.user.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.domain.user.entity.UserRoleEnum;
import com.sparta.topster.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GoogleService {

    private final Environment env;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestClient restClient;

    public User socialLogin(String code, String registrationId) {
        String accessToken = getAccessToken(code, registrationId);
        JsonNode userResourceNode = getUserResource(accessToken, registrationId);
        System.out.println("userResourceNode = " + userResourceNode);

        String id = userResourceNode.get("id").asText();
        String email = userResourceNode.get("email").asText();
        String nickname = userResourceNode.get("name").asText();
        String saveId = id.substring(0, 7);
        System.out.println("id = " + saveId);
        System.out.println("email = " + email);
        System.out.println("nickname = " + nickname);

        String password = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password);

        User googleUser = userRepository.findByGoogleId(Long.valueOf(saveId));
        //OAuthId -> null 회원가입 시작
        if (googleUser == null) {
            //email 비교
            User googleEmail = userRepository.findByGoogleEmail(email);
            if (googleEmail != null) {
                //동일 email 있을 시
                googleUser = googleEmail;
                googleUser = googleUser.GoogleIdUpdate(Long.valueOf(saveId));
            } else {
                //신규 회원가입
                googleUser = User.builder()
                    .username(nickname)
                    .nickname(nickname)
                    .password(encodedPassword)
                    .email(email)
                    .role(UserRoleEnum.USER)
                    .googleId(Long.valueOf(saveId))
                    .build();
            }
            return userRepository.save(googleUser);
        }
        return googleUser;
    }

    private String getAccessToken(String authorizationCode, String registrationId) {
        String clientId = env.getProperty("oauth2." + registrationId + ".client-id");
        String clientSecret = env.getProperty("oauth2." + registrationId + ".client-secret");
        String redirectUri = env.getProperty("oauth2." + registrationId + ".redirect-uri");
        String tokenUri = env.getProperty("oauth2." + registrationId + ".token-uri");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<JsonNode> responseNode =
            restClient.post()
                .uri(tokenUri)
                .body(params)
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .toEntity(JsonNode.class);
        JsonNode accessTokenNode = responseNode.getBody();
        return accessTokenNode.get("access_token").asText();
    }

    private JsonNode getUserResource(String accessToken, String registrationId) {
        String resourceUri = env.getProperty("oauth2." + registrationId + ".resource-uri");

        return restClient.get()
            .uri(resourceUri)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .toEntity(JsonNode.class).getBody();
    }
}
