package com.sparta.topster.domain.sse;

import com.sparta.topster.global.security.UserDetailsImpl;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    //모든 Emitters를 저장하는 ConcurrentHashMap
    public static Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    @GetMapping("/api/notification/subscribe")
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        SseEmitter sseEmitter = notificationService.subscribe(userId);

        return sseEmitter;
    }
}
