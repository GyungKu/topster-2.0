package com.sparta.topster.domain.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.sparta.topster.domain.user.dto.deleteDto.DeleteReq;
import com.sparta.topster.domain.user.dto.login.LoginReq;
import com.sparta.topster.domain.user.dto.modifyPassword.ModifyReq;
import com.sparta.topster.domain.user.dto.signup.SignupReq;
import com.sparta.topster.domain.user.dto.update.UpdateReq;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.domain.user.entity.UserRoleEnum;
import com.sparta.topster.domain.user.repository.UserRepository;
import com.sparta.topster.domain.user.service.user.UserServiceImpl;
import com.sparta.topster.global.exception.ServiceException;
import com.sparta.topster.global.util.JwtUtil;
import com.sparta.topster.global.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

public class UserServiceTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RedisUtil redisUtil;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("회원가입")
    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = {"12345", "123123"})
    void signupTest(String signUpCode) {
        //given
        String username = "username";
        String password = "password";
        String email = "username@naver.com";
        String nickname = "nickname";
        String intro = "intro";
        String certificationCode = "12345";

        SignupReq signupReq = SignupReq.builder()
            .username(username)
            .password(password)
            .email(email)
            .nickname(nickname)
            .intro(intro)
            .certification(certificationCode)
            .build();

        when(passwordEncoder.encode(password)).thenReturn(password);
        when(redisUtil.getData(email)).thenReturn(signUpCode);

        //when
        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .email(email)
            .nickname(nickname)
            .intro(intro)
            .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        // then
        if (certificationCode.equals(signUpCode)) {
            assertThatCode(() -> userService.signUp(signupReq))
                .doesNotThrowAnyException();
        } else {
            assertThatCode(() -> userService.signUp(signupReq))
                .hasMessage("인증번호 오류")
                .isInstanceOf(ServiceException.class);
        }
    }

    @DisplayName("로그인")
    @Order(2)
    @ValueSource(strings = {"password", "asdqweqw"})
    @ParameterizedTest
    void LoginTest(String password) {
        //given
        LoginReq loginReq = new LoginReq("username", "password");
        User mockUser = User.builder()
            .username(loginReq.getUsername())
            .password("password")
            .role(UserRoleEnum.USER)
            .build();

        //when
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(eq(password), anyString())).thenReturn(true);
        when(jwtUtil.createToken(anyString(), any())).thenReturn("mockedToken");

        //then
        if (mockUser.getPassword().equals(password)) {
            assertThatCode(() -> userService.loginUser(loginReq, response))
                .doesNotThrowAnyException();
        } else {
            assertThatCode(() -> userService.loginUser(loginReq, response))
                .hasMessage("비밀번호가 일치하지 않습니다.");
        }
    }

    @DisplayName("수정")
    @Order(3)
    @Test
    void updateTest() {
        //given
        UpdateReq updateReq = new UpdateReq("newNickname", "newIntro");

        User user = User.builder()
            .username("username")
            .password("password")
            .nickname("oldNickname")
            .intro("oldIntro")
            .build();

        //when
        when(userRepository.findById(user.getId())).thenReturn(user);

        //then;
        assertThatCode(() -> userService.updateUser(user, updateReq))
            .doesNotThrowAnyException();
        System.out.println(user.getNickname());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sampleRefreshToken", "testRefreshToken"})
    @Order(4)
    @DisplayName("RefreshToken")
    void refreshToken(String testRefreshToken) {
        //given
        String refreshToken = "sampleRefreshToken";
        String username = "username";

        User user = User.builder()
            .username(username)
            .role(UserRoleEnum.USER)
            .build();

        //when
        when(redisUtil.getData(refreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userService.refreshToken(refreshToken)).thenReturn("accessToken");

        //then
        if (refreshToken.equals(testRefreshToken)) {
            assertThatCode(() -> userService.refreshToken(testRefreshToken))
                .doesNotThrowAnyException();
        } else {
            assertThatCode(() -> userService.refreshToken(testRefreshToken))
                .hasMessage("유저가 존재하지 않습니다.");
        }
    }

    @ValueSource(strings = {"password", "password2"})
    @ParameterizedTest
    @Order(5)
    @DisplayName("회원탈퇴")
    void deleteTest(String password) {
        //given
        String username = "username";

        DeleteReq deleteReq = new DeleteReq(password);

        User user = User.builder()
            .username(username)
            .password("password")
            .build();

        //when
        when(userRepository.findById(user.getId())).thenReturn(user);
        when(passwordEncoder.matches(eq(password), eq(user.getPassword()))).thenReturn(
            !password.equals("password2"));

        //then
        if (password.equals(user.getPassword())) {
            assertDoesNotThrow(() -> userService.deleteUser(user, deleteReq));
        } else {
            assertThatCode(() -> userService.deleteUser(user, deleteReq))
                .hasMessage("비밀번호가 일치하지 않습니다.");
        }
    }

    @ValueSource(strings = {"123","123123"})
    @ParameterizedTest
    @Order(6)
    @DisplayName("비밀번호 변경")
    void modifyPassword(String certificationCode) {
        String email = "wogns8030@naver.com";
        String password = "123";
        String modifyPassword = "topster";
        ModifyReq modifyReq = new ModifyReq(certificationCode, modifyPassword);
        User user = User.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .build();

        ReflectionTestUtils.setField(user,"id",1L);
        when(redisUtil.getData(email)).thenReturn("123");
        when(userRepository.findById(user.getId())).thenReturn(user);

        if(certificationCode.equals("123")){
            assertThatCode(() -> userService.modifyPassword(user,modifyReq)).doesNotThrowAnyException();
        }else{
            assertThatCode(() -> userService.modifyPassword(user,modifyReq))
                .isInstanceOf(ServiceException.class)
                .hasMessage("인증번호 오류");
        }
    }
}