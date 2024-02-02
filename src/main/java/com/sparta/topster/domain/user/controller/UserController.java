package com.sparta.topster.domain.user.controller;

import static com.sparta.topster.domain.user.excepetion.UserException.MODIFY_PROFILE_FAILED;

import com.sparta.topster.domain.user.dto.deleteDto.DeleteReq;
import com.sparta.topster.domain.user.dto.getUser.GetUserRes;
import com.sparta.topster.domain.user.dto.modifyPassword.ModifyReq;
import com.sparta.topster.domain.user.dto.update.UpdateReq;
import com.sparta.topster.domain.user.dto.update.UpdateRes;
import com.sparta.topster.domain.user.service.user.UserServiceImpl;
import com.sparta.topster.global.exception.ServiceException;
import com.sparta.topster.global.response.RootNoDataRes;
import com.sparta.topster.global.security.UserDetailsImpl;
import com.sparta.topster.global.util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "유저 API")
public class UserController {

    private final UserServiceImpl userService;

    @PatchMapping("/update")
    public ResponseEntity<UpdateRes> updateUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody @Valid UpdateReq updateReq,
        BindingResult bindingResult) {

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        if (!fieldErrors.isEmpty()) {
            throw new ServiceException(MODIFY_PROFILE_FAILED);
        }

        UpdateRes updateRes = userService.updateUser(userDetails.getUser(), updateReq);

        return ResponseEntity.ok(updateRes);
    }

    @PatchMapping("/modifyPassword")
    public ResponseEntity<RootNoDataRes> modifyPassword(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody ModifyReq modifyReq) {
        userService.modifyPassword(userDetails.getUser(), modifyReq);
        return ResponseEntity.ok().body(RootNoDataRes.builder()
            .code("200")
            .message("비밀번호 변경완료")
            .build());
    }

    @GetMapping
    public ResponseEntity<GetUserRes> getUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        GetUserRes user = userService.userConvertDto(userDetails.getUser());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody @Valid DeleteReq deleteReq) {
        userService.deleteUser(userDetails.getUser(), deleteReq);

        return ResponseEntity.ok().body(HttpStatus.OK);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("refreshToken") String refreshToken) {
        try {
            String newAccessToken = userService.refreshToken(refreshToken);
            return ResponseEntity.ok().header(JwtUtil.AUTHORIZATION_HEADER, newAccessToken).build();
        } catch (ServiceException e) {
            return ResponseEntity.badRequest().body(e.getCode());
        }
    }
}
