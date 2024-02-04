package com.sparta.topster.domain.topster.service;

import com.sparta.topster.domain.album.dto.req.AlbumInsertReq;
import com.sparta.topster.domain.album.entity.Album;
import com.sparta.topster.domain.album.service.AlbumService;
import com.sparta.topster.domain.topster.dto.req.TopsterCreateReq;
import com.sparta.topster.domain.topster.dto.res.TopsterGetRes;
import com.sparta.topster.domain.topster.entity.Topster;
import com.sparta.topster.domain.topster.repository.TopsterRepository;
import com.sparta.topster.domain.topsterAlbum.entity.TopsterAlbum;
import com.sparta.topster.domain.topsterAlbum.repository.TopsterAlbumRepository;
import com.sparta.topster.domain.user.entity.User;
import com.sparta.topster.domain.user.entity.UserRoleEnum;
import com.sparta.topster.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopsterServiceTest {
    @InjectMocks
    TopsterService topsterService;

    User userA;
    User userB;

    Album albumA;
    Album albumB;

    @Mock
    TopsterRepository topsterRepository;

    @BeforeEach
    void init(){
        userA = User.builder()
                .nickname("test1")
                .email("aaa@aaa.com")
                .intro("testIntro1")
                .password("1234")
                .username("testUserA")
                .role(UserRoleEnum.USER)
                .build();
        userB = User.builder()
                .nickname("test2")
                .email("aaa@aaa.com")
                .intro("testIntro1")
                .password("1234")
                .username("testUserB")
                .role(UserRoleEnum.USER)
                .build();

        albumA = Album.builder()
                .title("albumA 제목")
                .image("albumA 이미지")
                .release("albumA 발매일")
                .artist("albumA가수")
                .build();

        albumB = Album.builder()
                .title("albumB 제목")
                .image("albumB 이미지")
                .release("albumB 발매일")
                .artist("albumB 가수")
                .build();

        ReflectionTestUtils.setField(userA, "id", 1L);
        ReflectionTestUtils.setField(userB, "id", 2L);

    }

    @Test
    @DisplayName("Topster를_등록할_수_있다")
    void testCreate(){
        //given
        TopsterCreateReq topsterCreateReq = TopsterCreateReq.builder().
                title("탑스터 제목")
                .build();

        Topster topster = Topster.builder().title("탑스터 제목").
                user(userA).
                build();

        given(topsterRepository.save(any())).willReturn(topster);

        //when
        Topster savedTopster = topsterService.create(topsterCreateReq, userA);

        //then
        assertThat(savedTopster.getTitle()).isEqualTo(topster.getTitle());
        assertThat(savedTopster.getUser().getId()).
                isEqualTo(1L);
    }


    @Test
    @DisplayName("탑스터 아이디로 탑스터를 조회할 수 있다.")
    void testGetTopsterService() {
        //given
        Topster topster = Topster.builder().title("탑스터 제목").
                user(userA).
                build();

        when(topsterRepository.findById(1L)).thenReturn(Optional.of(topster));
        //when
        TopsterGetRes topsterGetRes = topsterService.getTopsterService(1L);
        //then
        assertThat(topsterGetRes.getTitle()).isEqualTo(topster.getTitle());
        assertThat(topsterGetRes.getAuthor()).isEqualTo(topster.getUser().getNickname());
    }

    @Test
    @DisplayName("두명의 유저의 topster를 각각 조회할 수 있다.")
    void testGetTopsterByUserService(){

        //given 첫번째 유저의 Topster List
        //userA의 첫번째 탑스터
        Topster topsterA = Topster.builder()
                .user(userA)
                .title("topsterA")
                .build();
        //userA의 두번째 탑스터
        Topster topsterB = Topster.builder()
                .user(userA)
                .title("topsterB")
                .build();

        //유저 A의 첫번재 탑스터의 첫번재 TopsterAlbum
        TopsterAlbum topsterAlbumA = TopsterAlbum.builder()
                .album(albumA).topster(topsterA).build();
        //유저 A의 첫번재 탑스터의 두번재 TopsterAlbum
        TopsterAlbum topsterAlbumB = TopsterAlbum.builder()
                .album(albumB).topster(topsterA).build();
        //유저 A의 두번재 탑스터의 첫번재 TopsterAlbum
        TopsterAlbum topsterAlbumC = TopsterAlbum.builder()
                .album(albumA).topster(topsterB).build();
        //유저 A의 첫번재 탑스터의 두번재 TopsterAlbum
        TopsterAlbum topsterAlbumD = TopsterAlbum.builder()
                .album(albumB).topster(topsterB).build();

        topsterA.getTopsterAlbumList().add(topsterAlbumA);
        topsterA.getTopsterAlbumList().add(topsterAlbumB);

        topsterB.getTopsterAlbumList().add(topsterAlbumC);
        topsterB.getTopsterAlbumList().add(topsterAlbumD);

        List<Topster> topsterList = new ArrayList<>();
        topsterList.add(topsterA);
        topsterList.add(topsterB);
        when(topsterRepository.findByUserId(1L)).thenReturn(topsterList);

        //given 두번째 유저의 Topster List
        Topster topsterOtherUserA = Topster.builder()
                .user(userB)
                .title("topsterOtherUserA")
                .build();

        Topster topsterOtherUserB = Topster.builder()
                .user(userB)
                .title("topsterOtherUserB")
                .build();

        //Other 유저의 첫번재 탑스터의 두번째 TopsterAlbum
        TopsterAlbum topsterAlbumOtherUserA = TopsterAlbum.builder()
                .album(albumA).topster(topsterOtherUserA).build();
        //Other 유저의 첫번재 탑스터의 두번째 TopsterAlbum
        TopsterAlbum topsterAlbumOtherUserB = TopsterAlbum.builder()
                .album(albumB).topster(topsterOtherUserA).build();
        //Other 유저의 두번째 탑스터의 첫번재 TopsterAlbum
        TopsterAlbum topsterAlbumOtherUserC = TopsterAlbum.builder()
                .album(albumA).topster(topsterOtherUserB).build();
        //Other 유저의 두번째 탑스터의 두번재 TopsterAlbum
        TopsterAlbum topsterAlbumOtherUserD = TopsterAlbum.builder()
                .album(albumB).topster(topsterOtherUserB).build();

        topsterOtherUserA.getTopsterAlbumList().add(topsterAlbumOtherUserA);
        topsterOtherUserA.getTopsterAlbumList().add(topsterAlbumOtherUserB);

        topsterOtherUserB.getTopsterAlbumList().add(topsterAlbumOtherUserC);
        topsterOtherUserB.getTopsterAlbumList().add(topsterAlbumOtherUserD);

        List<Topster> topsterListOtherUser = new ArrayList<>();
        topsterListOtherUser.add(topsterOtherUserA);
        topsterListOtherUser.add(topsterOtherUserB);
        when(topsterRepository.findByUserId(2L)).thenReturn(topsterListOtherUser);

        //when
        List<TopsterGetRes> topsterGetResListA =
                topsterService.getTopsterByUserService(1L);

        List<TopsterGetRes> topsterGetResListB =
                topsterService.getTopsterByUserService(2L);
        //then
        assertThat(topsterA.getTitle()).isEqualTo(topsterGetResListA.get(0).getTitle());
        assertThat(topsterA.getUser().getNickname())
                .isEqualTo(topsterGetResListA.get(0).getAuthor());
        assertThat(topsterB.getTitle()).isEqualTo(topsterGetResListA.get(1).getTitle());
        assertThat(topsterB.getUser().getNickname())
                .isEqualTo(topsterGetResListA.get(1).getAuthor());

        assertThat(topsterOtherUserA.getTitle()).isEqualTo(topsterGetResListB.get(0).getTitle());
        assertThat(topsterOtherUserA.getUser().getNickname())
                .isEqualTo(topsterGetResListB.get(0).getAuthor());
        assertThat(topsterOtherUserB.getTitle()).isEqualTo(topsterGetResListB.get(1).getTitle());
        assertThat(topsterOtherUserB.getUser().getNickname())
                .isEqualTo(topsterGetResListB.get(1).getAuthor());
    }

    @Test
    @DisplayName("탑스터를 Slice로 조회할 수 있다.")
    void testGetTopstersService(){
        //given
        // 탑스터 한페이지에 9개가 뜸 두번째 페이지까지 잘 가져오는지 test위해 10개의 topster 생성
        Topster topster1 = Topster.builder()
                .user(userA)
                .title("topster1")
                .build();
        Topster topster2 = Topster.builder()
                .user(userA)
                .title("topster2")
                .build();
        Topster topster3 = Topster.builder()
                .user(userA)
                .title("topster3")
                .build();
        Topster topster4 = Topster.builder()
                .user(userA)
                .title("topster4")
                .build();
        Topster topster5 = Topster.builder()
                .user(userA)
                .title("topster5")
                .build();
        Topster topster6 = Topster.builder()
                .user(userA)
                .title("topster6")
                .build();
        Topster topster7 = Topster.builder()
                .user(userA)
                .title("topster7")
                .build();
        Topster topster8 = Topster.builder()
                .user(userA)
                .title("topster8")
                .build();
        Topster topster9 = Topster.builder()
                .user(userA)
                .title("topster9")
                .build();
        Topster topster10 = Topster.builder()
                .user(userA)
                .title("topster10")
                .build();

        //첫번째 페이지
        List<Topster> topsterList1 = new ArrayList<>();
        topsterList1.add(topster1);
        topsterList1.add(topster2);
        topsterList1.add(topster3);
        topsterList1.add(topster4);
        topsterList1.add(topster5);
        topsterList1.add(topster6);
        topsterList1.add(topster7);
        topsterList1.add(topster8);
        topsterList1.add(topster9);

        //두번째 페이지
        List<Topster> topsterList2 = new ArrayList<>();
        topsterList2.add(topster10);

        Page<Topster> topsterPage1 = new PageImpl<>(topsterList1);
        Page<Topster> topsterPage2 = new PageImpl<>(topsterList2);

        Integer pageNum =1;

        //when
        //첫번째 페이지
        when(topsterRepository.findAll(any())).thenReturn(topsterPage1);
        Slice<TopsterGetRes> findTopsterPage1 = topsterService.getTopstersService(pageNum);
        List<TopsterGetRes> findTopsterPageList1 = findTopsterPage1.getContent();

        //두번째 페이지
        when(topsterRepository.findAll(any())).thenReturn(topsterPage2);
        Slice<TopsterGetRes> findTopsterPage2 = topsterService.getTopstersService(pageNum+1);
        List<TopsterGetRes> findTopsterPageList2 = findTopsterPage2.getContent();

        //then
        //첫번째 페이지
        assertThat(findTopsterPageList1.get(0).getTitle()).isEqualTo(topster1.getTitle());
        assertThat(findTopsterPageList1.get(8).getTitle()).isEqualTo(topster9.getTitle());

        //두번째 페이지
        assertThat(findTopsterPageList2.get(0).getTitle()).isEqualTo(topster10.getTitle());
    }

    @Test
    @DisplayName("탑스터를 삭제할 수 있다.")
    void testDeleteTopstersService(){
        //given
        Topster topster = Topster.builder()
                .title("탑스터 제목")
                .user(userA)
                .build();

        ReflectionTestUtils.setField(topster, "id", 1L);

        //when
        when(topsterRepository.findById(any())).thenReturn(Optional.of(topster));

        topsterService.deleteTopster(topster.getId(), userA);
        //then
        verify(topsterRepository, times(1)).delete(topster);
    }

    @Test
    @DisplayName("탑스터 좋아요 Top3를 조회할 수 있다.")
    void testGetTopsterTopThree(){
        //given
        Topster topster3 = Topster.builder()
                .title("탑스터 제목3")
                .user(userA)
                .build();
        ReflectionTestUtils.setField(topster3, "likeCount", 3L);
        Topster topster2 = Topster.builder()
                .title("탑스터 제목2")
                .user(userA)
                .build();
        ReflectionTestUtils.setField(topster2, "likeCount", 2L);
        Topster topster1 = Topster.builder()
                .title("탑스터 제목1")
                .user(userA)
                .build();
        ReflectionTestUtils.setField(topster1, "likeCount", 1L);
        Topster topster0 = Topster.builder()
                .title("탑스터 제목0")
                .user(userA)
                .build();
        ReflectionTestUtils.setField(topster0, "likeCount", 0L);

        TopsterGetRes topsterGetRes3 = TopsterGetRes.builder()
                .title(topster3.getTitle())
                .build();
        TopsterGetRes topsterGetRes2 = TopsterGetRes.builder()
                .title(topster2.getTitle())
                .build();
        TopsterGetRes topsterGetRes1 = TopsterGetRes.builder()
                .title(topster1.getTitle())
                .build();
        TopsterGetRes topsterGetRes0 = TopsterGetRes.builder()
                .title(topster0.getTitle())
                .build();

        List<Topster> topsterList = new ArrayList<>();
        topsterList.add(topster3);
        topsterList.add(topster2);
        topsterList.add(topster1);

        List<TopsterGetRes> topsterGetResList = new ArrayList<>();
        topsterGetResList.add(topsterGetRes0);
        topsterGetResList.add(topsterGetRes1);
        topsterGetResList.add(topsterGetRes2);
        topsterGetResList.add(topsterGetRes3);

        //when
        when(topsterRepository.findTop3ByOrderByLikeCountDesc()).thenReturn(topsterList);
        List<TopsterGetRes> getTopsterGetResList = topsterService.getTopsterTopThree();
        //then
        assertThat(getTopsterGetResList.get(0).getTitle()).isEqualTo(topster3.getTitle());
        assertThat(getTopsterGetResList.get(1).getTitle()).isEqualTo(topster2.getTitle());
        assertThat(getTopsterGetResList.get(2).getTitle()).isEqualTo(topster1.getTitle());
    }

    @Test
    @DisplayName("탑스터의 작성자만 권한을 가질 수 있다.")
    void testIsAuthor(){
        //given
        Topster topster = Topster.builder()
                .title("탑스터 제목3")
                .user(userA)
                .build();
        ReflectionTestUtils.setField(topster, "id", 1L);

        //when
        when(topsterRepository.findById(1L)).thenReturn(Optional.of(topster));

        //then
        assertThat(topsterService.isAuthor(userA.getId(), topster.getId())).isTrue();
    }

    @Test
    @DisplayName("탑스터의 작성자만 권한을 가질 수 있다. 작성자가 아닌 케이스")
    void testIsAuthorFalse(){
        //given
        Topster topster = Topster.builder()
                .title("탑스터 제목3")
                .user(userA)
                .build();
        ReflectionTestUtils.setField(topster, "id", 1L);

        //when
        when(topsterRepository.findById(1L)).thenReturn(Optional.of(topster));

        //then
        assertThatThrownBy(()-> topsterService.isAuthor(userB.getId(), topster.getId()))
                .isInstanceOf(ServiceException.class);
    }
}