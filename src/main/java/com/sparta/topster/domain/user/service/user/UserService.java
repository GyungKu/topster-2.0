package com.sparta.topster.domain.user.service.user;

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
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    SignupRes signUp(SignupReq signupReq);

    LoginRes loginUser(LoginReq loginReq, HttpServletResponse response);

    UpdateRes updateUser(User user, UpdateReq updateReq);

    GetUserRes userConvertDto(User user);

    void deleteUser(User user, DeleteReq deleteReq);

    String refreshToken(String refreshToken);

    void modifyPassword(User user, ModifyReq modifyReq) throws Exception;
}
