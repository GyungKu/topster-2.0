package com.sparta.topster.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import com.sparta.topster.domain.comment.dto.req.CommentCreateReq;
import com.sparta.topster.domain.comment.dto.req.CommentModifyReq;
import com.sparta.topster.domain.comment.dto.res.CommentRes;
import com.sparta.topster.domain.comment.entity.Comment;
import com.sparta.topster.domain.comment.exception.CommentException;
import com.sparta.topster.domain.comment.repository.CommentRespository;
import com.sparta.topster.domain.post.entity.Post;
import com.sparta.topster.domain.post.service.PostService;
import com.sparta.topster.domain.sse.NotificationService;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.global.exception.ServiceException;
import com.sparta.topster.global.response.RootNoDataRes;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class) // Mockito의 목 객체 자동으로 초기화
class CommentServiceTest {


  @InjectMocks
  private CommentService commentService;
  @Mock
  private CommentRespository commentRepository;
  @Mock
  private PostService postService;

  User user;
  User otherUser;
  Post post;

  @BeforeEach
  void init(){
    user = User.builder()
        .nickname("asdf")
        .username("hhhh")
        .build();
    otherUser = User.builder()
        .nickname("zxcv")
        .username("qqqq")
        .build();
    post = Post.builder()
        .user(user)
        .build();

    ReflectionTestUtils.setField(user, "id", 1L);
    ReflectionTestUtils.setField(otherUser, "id", 2L);
    ReflectionTestUtils.setField(post, "id", 1L);

  }

  @Test
  void 댓글_작성_성공_테스트() {
    // Given
    CommentCreateReq req = CommentCreateReq.builder()
        .content("댓글테스트")
        .build();

    given(postService.getPost(any())).willReturn(post);

    // When
    CommentRes result = commentService.createComment(post.getId(), req, user);

    // Then
    assertThat(result.getContent()).isEqualTo(req.getContent());
    assertThat(result.getAuthor()).isEqualTo(user.getNickname());
  }

  @Test
  void 댓글_조회_테스트() {
    // given
    Long postId = 1L;
    Integer pageNum = 1;
    Pageable pageable = PageRequest.of(0, 9);

    Comment comment = Comment.builder()
        .content("댓글")
        .post(post)
        .user(user)
        .build();
    ReflectionTestUtils.setField(comment, "id", 1L);

    Page<Comment> commentPage = new PageImpl<>(Collections.singletonList(comment), pageable, 1);

    given(commentRepository.findByPostId(anyLong(), any(Pageable.class))).willReturn(commentPage);

    // when
    Page<CommentRes> result = commentService.getComments(postId, pageNum);

    // then
    assert(!result.getContent().isEmpty());
    CommentRes commentRes = result.getContent().get(0);
    assertEquals(commentRes.getId(), comment.getId());
    assertEquals(commentRes.getContent(), comment.getContent());
    assertEquals(commentRes.getAuthor(), comment.getUser().getNickname());
    assertEquals(commentRes.getCreatedAt(), comment.getCreatedAt());
  }

  @Test
  void 수정을_할_때_댓글이_존재하지_않으면_예외를_반환한다() {
    //given
    CommentModifyReq req = CommentModifyReq.builder()
        .content("댓글 수정")
        .build();

    // When, Then
    assertThatThrownBy(() -> commentService.modifyComment(9999L, req, user)).isInstanceOf(
        ServiceException.class).hasMessage("댓글이 존재하지 않습니다.");
  }

  @Test
  void 수정을_할_때_작성자가_아니면_예외를_반환한다() {
    CommentModifyReq req = CommentModifyReq.builder()
        .content("댓글 수정")
        .build();

    Comment comment = Comment.builder()
        .content("댓글")
        .post(post)
        .user(user)
        .build();

    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.modifyComment(9999L, req, otherUser)).isInstanceOf(
        ServiceException.class).hasMessage("작성자만 수정 및 삭제 할 수 있습니다.");
  }

  @Test
  void 수정_성공_테스트() {
    CommentModifyReq req = CommentModifyReq.builder()
        .content("댓글 수정")
        .build();

    Comment comment = Comment.builder()
        .content("댓글")
        .post(post)
        .user(user)
        .build();
    ReflectionTestUtils.setField(comment, "id", 1L);

    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

    RootNoDataRes res = commentService.modifyComment(comment.getId(), req, user);

    assertThat(res.code()).isEqualTo("200");
    assertThat(res.message()).isEqualTo(comment.getId()+ "번 댓글을 수정하였습니다.");
  }

  @Test
  void 삭제_성공_테스트() {
    Comment comment = Comment.builder()
        .content("댓글")
        .post(post)
        .user(user)
        .build();
    ReflectionTestUtils.setField(comment, "id", 1L);

    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

    RootNoDataRes res = commentService.deleteComment(comment.getId(), user);
    assertThat(res.code()).isEqualTo("200");
    assertThat(res.message()).isEqualTo(comment.getId()+ "번 댓글을 삭제하였습니다.");
  }

  @Test
  void 삭제를_할_때_작성자가_아니면_예외를_반환한다() {
    Comment comment = Comment.builder()
        .content("댓글")
        .post(post)
        .user(user)
        .build();

    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.deleteComment(9999L, otherUser)).isInstanceOf(
        ServiceException.class).hasMessage("작성자만 수정 및 삭제 할 수 있습니다.");
  }
  @Test
  void 댓글_작성자가_같은_경우() {
  // given
    Long commentId = 1L;
    User user = User.builder().nickname("Test User").build();
    Comment comment = Comment.builder()
        .user(user)
        .build();
    ReflectionTestUtils.setField(comment, "id", 1L);

    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

      // when
      Comment result = commentService.getCommentUser(commentId, user);

      // then
      assertEquals(result, comment);
  }
  @Test
  void 댓글_작성자가_다른_경우() {
    // given
    Comment comment = Comment.builder()
        .user(user)
        .build();
    ReflectionTestUtils.setField(comment, "id", 1L);

    given(commentRepository.findById(any())).willReturn(Optional.of(comment));

    // when, then
    assertThatThrownBy(() -> commentService.deleteComment(9999L, otherUser)).isInstanceOf(
        ServiceException.class).hasMessage("작성자만 수정 및 삭제 할 수 있습니다.");
  }

        Comment comment = Comment.builder()
            .content("댓글")
            .post(post)
            .user(user)
            .build();
        ReflectionTestUtils.setField(comment, "id", 1L);

        given(commentRepository.findById(any())).willReturn(Optional.of(comment));

        RootNoDataRes res = commentService.modifyComment(comment.getId(), req, user);

        assertThat(res.code()).isEqualTo("200");
        assertThat(res.message()).isEqualTo(comment.getId() + "번 댓글을 수정하였습니다.");
    }
}