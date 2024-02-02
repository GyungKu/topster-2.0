package com.sparta.topster.domain.like.service;

import com.sparta.topster.domain.like.dto.LikeCountStatusRes;
import com.sparta.topster.domain.like.entity.Like;
import com.sparta.topster.domain.like.repository.LikeRepository;
import com.sparta.topster.domain.sse.NotificationService;
import com.sparta.topster.domain.topster.entity.Topster;
import com.sparta.topster.domain.topster.service.TopsterService;
import com.sparta.topster.domain.user.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "LikeService")
public class LikeService {

    private final LikeRepository likeRepository;
    private final TopsterService topsterService;
    private final NotificationService notificationService;

  @Transactional
  public boolean toggleLike(Long topsterId, User user) {
    // 해당 id의 탑스터가 존재하는지 검증
    Topster topster = topsterService.getTopster(topsterId);

    // 해당 탑스타에 대한 모든 좋아요 정보를 조회
    Like findLike = getLike(user.getId(), topsterId);

    log.info("조회된 좋아요 목록을 순회");
      // 현재 사용자가 이미 해당 게시물에 좋아요를 눌렀는지 확인
      if(findLike != null) {
        // 이미 좋아요를 눌렀다면 해당 좋아요 삭제
        likeRepository.delete(findLike);
        topster.upAndDownLikeCount(-1);
        return false;
      }
     log.info("좋아요 누르지 않았다면 실행");
    // likeCount + 1
    topster.upAndDownLikeCount(1);
    // 해당 탑스타와 사용자 정보를 가지고 있는 좋아요 객체 생성
    Like like = Like.builder()
        .user(user)
        .topster(topster)
        .build();

    // 사용자가 아직 좋아요를 누르지 않았다면 좋아요 정보 추가
    likeRepository.save(like);
    notificationService.notifyLikeAdded(topster.getUser().getId());

    return true;
  }

  public Like getLike(Long userId, Long topsterId) {
    Optional<Like> optionalLike = likeRepository.findByTopsterIdAndUserId(topsterId, userId);
    if(optionalLike.isPresent()) {
      return optionalLike.get();
    } else {
      log.info("해당 유저가 탑스터에 좋아요를 누르지 않았습니다.");
    }
    return null;
  }

  public LikeCountStatusRes getLikeCountAndStatus(Long topsterId, Long userId) {
    // 좋아요 수 가져오기
    Long likeCount = likeRepository.countByTopsterId(topsterId);

    // 사용자가 좋아요를 눌렀는지 확인
    boolean isLiked = false;
    if(userId != null) {
      // 특정 사용자가 특정 topster에 좋아요를 눌렀는지 여부를 확인
      isLiked = likeRepository.existsByTopsterIdAndUserId(topsterId, userId);
    }

    // 좋아요 수와 좋아요 여부를 담은 응답 객체 반환
    return LikeCountStatusRes.builder()
        .topsterId(topsterId)
        .likeCount(likeCount)
        .isLiked(isLiked)
        .build();
  }
}
