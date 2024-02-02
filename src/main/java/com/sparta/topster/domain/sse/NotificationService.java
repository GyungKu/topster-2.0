package com.sparta.topster.domain.sse;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    // 메시지 알림
    public SseEmitter subscribe(Long userId) {

        // 1. 현재 클라이언트를 위한 sseEmitter 객체 생성
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);

        // 2. 연결
        try {
            sseEmitter.send(SseEmitter.event().name("connect"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. 저장
        NotificationController.sseEmitters.put(userId, sseEmitter);

        // 4. 연결 종료 처리
        sseEmitter.onCompletion(() -> NotificationController.sseEmitters.remove(userId));	// sseEmitter 연결이 완료될 경우
        sseEmitter.onTimeout(() -> NotificationController.sseEmitters.remove(userId));		// sseEmitter 연결에 타임아웃이 발생할 경우
        sseEmitter.onError((e) -> NotificationController.sseEmitters.remove(userId));		// sseEmitter 연결에 오류가 발생할 경우

        return sseEmitter;
    }

    // 댓글 알림 - 게시글 작성자 에게
    public void notifyComment(Long userId) {
        sendSseEvent(userId, "addComment", "댓글이 추가되었습니다.");
    }

    //좋아요 알림 - 게시글 작성자 에게
    public void notifyLikeAdded(Long userId) {
        sendSseEvent(userId, "addLike", "좋아요가 추가되었습니다.");
    }

    private void sendSseEvent(Long userId, String addLike, String object) {
        if (NotificationController.sseEmitters.containsKey(userId)) {
            SseEmitter sseEmitterReceiver = NotificationController.sseEmitters.get(userId);
            try {
                sseEmitterReceiver.send(SseEmitter.event().name(addLike).data(object));
            } catch (Exception e) {
                NotificationController.sseEmitters.remove(userId);
            }
        }
    }
}