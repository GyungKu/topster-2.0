package com.sparta.topster.domain.user.service;

import static com.sparta.topster.global.exception.ErrorCode.DUPLICATE_NICKNAME;
import static com.sparta.topster.global.exception.ErrorCode.DUPLICATE_USERNAME;
import static com.sparta.topster.global.exception.ErrorCode.NOT_FOUND_PASSWORD;
import static com.sparta.topster.global.exception.ErrorCode.WRONG_ADMIN_CODE;

import com.sparta.topster.domain.user.dto.signup.SignupReq;
import com.sparta.topster.domain.user.dto.signup.SignupRes;
import com.sparta.topster.domain.user.dto.update.UpdateReq;
import com.sparta.topster.domain.user.dto.update.UpdateRes;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.domain.user.entity.UserRoleEnum;
import com.sparta.topster.domain.user.repository.UserRepository;
import com.sparta.topster.global.exception.ServiceException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String ADMIN_TOKEN = "AAlrnYxKZ0aHgTBcXukeZygoC";

    public SignupRes signup(SignupReq signupReq) {
        String username = signupReq.getUsername();
        String nickname = signupReq.getNickname();
        String password = passwordEncoder.encode(signupReq.getPassword());

        Optional<User> byUsername = userRepository.findByUsername(username);
        Optional<User> byNickname = userRepository.findBynickname(nickname);

        if(byUsername.isPresent()){
            throw new ServiceException(DUPLICATE_USERNAME);
        }

        if (byNickname.isPresent()){
            throw new ServiceException(DUPLICATE_NICKNAME);
        }

        UserRoleEnum role = UserRoleEnum.USER;

        if(signupReq.isAdmin()){
            if(!ADMIN_TOKEN.equals(signupReq.getAdminToken())){
                throw new ServiceException(WRONG_ADMIN_CODE);
            }
            role = UserRoleEnum.ADMIN;
        }

        User user = User.builder()
            .username(username)
            .nickname(nickname)
            .password(password)
            .email(signupReq.getEmail())
            .intro(signupReq.getIntro())
            .role(role)
            .build();

        User saveUser = userRepository.save(user);

        return SignupRes.builder()
            .username(saveUser.getUsername())
            .nickname(saveUser.getNickname())
            .role(saveUser.getRole())
            .build();
    }

    @Transactional
    public UpdateRes updateUser(User user, UpdateReq updateReq) {
        User findByUser = getUser(user.getId());

        if(!passwordEncoder.matches(updateReq.getPassword(), findByUser.getPassword())){
            throw new ServiceException(NOT_FOUND_PASSWORD);
        }

        if(findByUser.getNickname().equals(updateReq.getNickname())){
           throw new ServiceException(DUPLICATE_NICKNAME);
        }

        findByUser.update(updateReq.getNickname(), updateReq.getIntro());

        return UpdateRes.builder()
            .nickname(updateReq.getNickname())
            .intro(updateReq.getIntro())
            .build();
    }


    private User getUser(Long userId) {
        User findById = userRepository.findById(userId);
        return findById;
    }
}
