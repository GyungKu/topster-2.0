package com.sparta.topster.domain.user.service.user;

import static com.sparta.topster.domain.user.excepetion.UserException.DUPLICATE_EMAIL;
import static com.sparta.topster.domain.user.excepetion.UserException.DUPLICATE_NICKNAME;
import static com.sparta.topster.domain.user.excepetion.UserException.DUPLICATE_USERNAME;
import static com.sparta.topster.domain.user.excepetion.UserException.NOT_EXIST_USER;
import static com.sparta.topster.domain.user.excepetion.UserException.NOT_FOUND_AUTHENTICATION_CODE;
import static com.sparta.topster.domain.user.excepetion.UserException.NOT_FOUND_PASSWORD;
import static com.sparta.topster.domain.user.excepetion.UserException.NOT_FOUND_USERID;
import static com.sparta.topster.domain.user.excepetion.UserException.TOKEN_ERROR;

import com.sparta.topster.domain.user.dto.deleteDto.DeleteReq;
import com.sparta.topster.domain.user.dto.getUser.GetUserRes;
import com.sparta.topster.domain.user.dto.login.LoginReq;
import com.sparta.topster.domain.user.dto.login.LoginRes;
import com.sparta.topster.domain.user.dto.modifyPassword.ModifyReq;
import com.sparta.topster.domain.user.dto.signup.SignupReq;
import com.sparta.topster.domain.user.dto.signup.SignupRes;
import com.sparta.topster.domain.user.dto.update.UpdateReq;
import com.sparta.topster.domain.user.dto.update.UpdateRes;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.domain.user.entity.UserRoleEnum;
import com.sparta.topster.domain.user.excepetion.UserException;
import com.sparta.topster.domain.user.repository.UserRepository;
import com.sparta.topster.global.exception.ServiceException;
import com.sparta.topster.global.util.JwtUtil;
import com.sparta.topster.global.util.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

//    private final String ADMIN_TOKEN = "AAlrnYxKZ0aHgTBcXukeZygoC";

    @Override
    public SignupRes signUp(SignupReq signupReq) {
        String username = signupReq.getUsername();
        String nickname = signupReq.getNickname();
        String password = passwordEncoder.encode(signupReq.getPassword());
        String signupCode = redisUtil.getData(signupReq.getEmail());

        checkMailCode(signupCode, signupReq.getCertification());

        Optional<User> byUsername = userRepository.findByUsername(username);
        Optional<User> byNickname = userRepository.findBynickname(nickname);
        Optional<User> byEmail = userRepository.findByUserEmail(signupReq.getEmail());

        checkDuplicate(byUsername.isPresent(), DUPLICATE_USERNAME);
        checkDuplicate(byNickname.isPresent(), DUPLICATE_NICKNAME);
        checkDuplicate(byEmail.isPresent(), DUPLICATE_EMAIL);

        UserRoleEnum role = UserRoleEnum.USER;

//        if (signupReq.isAdmin()) {
//            checkPassword(!ADMIN_TOKEN.equals(signupReq.getAdminToken()), WRONG_ADMIN_CODE);
//            role = UserRoleEnum.ADMIN;
//        }

        User user = User.builder()
            .username(username)
            .nickname(nickname)
            .password(password)
            .email(signupReq.getEmail())
            .intro(signupReq.getIntro())
            .role(role)
            .build();

        User saveUser = userRepository.save(user);
        redisUtil.deleteData(user.getEmail());

        return SignupRes.builder()
            .username(saveUser.getUsername())
            .nickname(saveUser.getNickname())
            .role(saveUser.getRole())
            .build();
    }

    @Override
    public LoginRes loginUser(LoginReq loginReq, HttpServletResponse response) {
        String username = loginReq.getUsername();
        User byUsername = getByUsername(username);

        checkPassword(loginReq.getPassword(), byUsername.getPassword());

        UserRoleEnum role = byUsername.getRole();

        response.setHeader(JwtUtil.AUTHORIZATION_HEADER,
            jwtUtil.createToken(byUsername.getUsername(), role));
        response.setHeader(JwtUtil.REFRESH_TOKEN_PREFIX,
            jwtUtil.createRefreshToken(byUsername.getUsername()));

        return LoginRes.builder().username(byUsername.getUsername()).build();
    }

    @Override
    @Transactional
    public UpdateRes updateUser(User user, UpdateReq updateReq) {
        User findByUser = getUser(user.getId());

        if (!updateReq.getNickname().equals(findByUser.getNickname())) {
            Optional<User> byNickName = userRepository.findBynickname(updateReq.getNickname());
            checkDuplicate(byNickName.isPresent(), DUPLICATE_NICKNAME);
        }

        findByUser.updateIntro(updateReq);

        return UpdateRes.builder()
            .nickname(updateReq.getNickname())
            .intro(updateReq.getIntro())
            .build();
    }

    @Override
    public GetUserRes userConvertDto(User user) {
        getUser(user.getId());

        return GetUserRes.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .intro(user.getIntro())
            .build();
    }

    @Override
    @Transactional
    public void deleteUser(User user, DeleteReq deleteReq) {
        String password = deleteReq.getPassword();
        User getUser = getUser(user.getId());
        checkPassword(password, getUser.getPassword());
        userRepository.deleteById(getUser.getId());
    }

    @Override
    @Transactional
    public String refreshToken(String refreshToken) {
        String username = redisUtil.getData(refreshToken);

        if (refreshToken == null) {
            throw new ServiceException(TOKEN_ERROR);
        }
        User byUsername = getByUsername(username);
        return jwtUtil.createToken(username, byUsername.getRole());
    }

    @Override
    @Transactional
    public void modifyPassword(User user, ModifyReq modifyReq){
        User findByUser = getUser(user.getId());
        String certification = redisUtil.getData(user.getEmail());
        checkMailCode(modifyReq.getCertification(),certification);
        findByUser.modifyPassword(passwordEncoder.encode(modifyReq.getModifyPassword()));
        redisUtil.deleteData(user.getEmail());
    }

    private User getByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
            () -> new ServiceException(NOT_EXIST_USER)
        );
    }

    public User getUser(Long userId) {
        User byId = userRepository.findById(userId);
        if (byId == null) {
            throw new ServiceException(NOT_FOUND_USERID);
        }
        return byId;
    }

    private void checkMailCode(String signupCode, String inputCode) {
        if (!inputCode.equals(signupCode)) {
            throw new ServiceException(NOT_FOUND_AUTHENTICATION_CODE);
        }
    }

    private void checkDuplicate(boolean present, UserException userException) {
        if (present) {
            throw new ServiceException(userException);
        }
    }

    private void checkPassword(String rowPassword, String encodePassword) {
        if (!passwordEncoder.matches(rowPassword, encodePassword)) {
            throw new ServiceException(NOT_FOUND_PASSWORD);
        }
    }
}
