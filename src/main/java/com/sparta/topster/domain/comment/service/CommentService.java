package com.sparta.topster.domain.comment.service;

import com.sparta.topster.domain.comment.dto.req.CommentCreateReq;
import com.sparta.topster.domain.comment.dto.req.CommentModifyReq;
import com.sparta.topster.domain.comment.dto.res.CommentRes;
import com.sparta.topster.domain.comment.entity.Comment;
import com.sparta.topster.domain.comment.exception.CommentException;
import com.sparta.topster.domain.comment.repository.CommentRespository;
import com.sparta.topster.domain.post.entity.Post;
import com.sparta.topster.domain.post.exception.PostException;
import com.sparta.topster.domain.post.service.PostService;
import com.sparta.topster.domain.sse.NotificationService;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.global.exception.ServiceException;
import com.sparta.topster.global.response.RootNoDataRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

  private final CommentRespository commentRespository;
  private final PostService postService;
  private final NotificationService notificationService;

  public CommentRes createComment(Long postId, CommentCreateReq commentCreateReq, User user) {
    log.info("댓글 작성");
    Post post = postService.getPost(postId);

    Comment comment = Comment.builder()
        .content(commentCreateReq.getContent())
        .user(user)
        .post(post)
        .build();

    commentRespository.save(comment);
    Long postUserId = post.getUser().getId();
    notificationService.notifyComment(postUserId);

    log.info("댓글 작성 완료");
    return CommentRes.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(comment.getUser().getNickname())
        .createdAt(comment.getCreatedAt())
        .build();
  }

  public Page<CommentRes> getComments(Long postId, Integer pageNum) {
    pageNum = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
    Pageable pageable = PageRequest.of(pageNum - 1, 9);
    log.info("해당 게시글에 대한 댓글 조회");
    Page<Comment> findCommentList = commentRespository.findByPostId(postId, pageable);

    return findCommentList.map(comment -> CommentRes.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(comment.getUser().getNickname())
        .createdAt(comment.getCreatedAt()).build());
  }

  @Transactional
  public RootNoDataRes modifyComment(Long commentId, CommentModifyReq commentModifyReq, User user) {
    log.info("댓글 수정");
    Comment comment = getCommentUser(commentId, user);

    comment.update(commentModifyReq.getContent());

    log.info("댓글 수정 완료");
    commentRespository.save(comment);

    return RootNoDataRes.builder()
        .message(commentId + "번 댓글을 수정하였습니다.")
        .code("200").build();
  }

  @Transactional
  public RootNoDataRes deleteComment(Long commentId, User user) {
    log.info("댓글 삭제");
    Comment comment = getCommentUser(commentId, user);

    log.info("댓글 삭제 완료");
    commentRespository.delete(comment);

    return RootNoDataRes.builder()
        .message(commentId + "번 댓글을 삭제하였습니다.")
        .code("200").build();
  }

  public Comment getComment(Long commentId) {
    return commentRespository.findById(commentId)
        .orElseThrow(() -> new ServiceException(CommentException.NO_COMMENT));
  }

  public Comment getCommentUser(Long commentId, User user) {
    Comment comment = getComment(commentId);

    if(!comment.getUser().getNickname().equals(user.getNickname())) {
      throw new ServiceException(CommentException.MODIFY_AND_DELETE_ONLY_AUTHOR); // 작성자만 수정 및 삭제 할 수 있습니다.
    }

    return comment;
  }

  public RootNoDataRes isAuthor(Long commentId, User user) {
    Comment comment = getComment(commentId);

    if(!comment.getUser().getId().equals(user.getId())){
      throw new ServiceException(PostException.AccessDeniedError);
    }
    return RootNoDataRes.builder()
        .message("해당 댓글의 작성자입니다.")
        .code("200").build();
  }
}
