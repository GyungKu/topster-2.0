package com.sparta.topster.domain.album.service;

import com.sparta.topster.domain.album.dto.res.AlbumRes;
import com.sparta.topster.domain.album.entity.Album;
import com.sparta.topster.domain.album.entity.CacheAlbum;
import com.sparta.topster.domain.album.repository.AlbumRepository;
import com.sparta.topster.domain.album.repository.CacheAlbumRepository;
import com.sparta.topster.domain.openApi.service.OpenApiService;
import org.apache.catalina.LifecycleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAlbumService {
    AlbumRepository albumRepository = mock();
    OpenApiService openApiService = mock();
    CacheAlbumRepository cacheAlbumRepository = mock();
    AlbumService albumService = new AlbumService(albumRepository, openApiService, cacheAlbumRepository);

    Album albumA;
    Album albumB;
    @BeforeEach
    void init(){
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
    }
    @Test
    @DisplayName("getAlbumsByArtist 캐싱된 데이터가 있을 경우")
    void testGetAlbumsByArtist(){
        //given
        List<AlbumRes>albumResList = new ArrayList<>();

        AlbumRes albumResA = AlbumRes.builder()
                .title(albumA.getTitle())
                .artist(albumA.getArtist())
                .releaseDate(albumA.getReleaseDate())
                .image(albumA.getImage())
                .build();

        AlbumRes albumResB = AlbumRes.builder()
                .title(albumB.getTitle())
                .artist(albumB.getArtist())
                .releaseDate(albumB.getReleaseDate())
                .image(albumB.getImage())
                .build();

        albumResList.add(albumResA);
        albumResList.add(albumResB);

        CacheAlbum cacheAlbumA = CacheAlbum.builder()
                .albums(albumResList)
                .artist(albumA.getArtist())
                .build();

        //when
        when(cacheAlbumRepository.findById(any())).thenReturn(Optional.of(cacheAlbumA));
        List<AlbumRes> cachedAlbumResList = albumService.getAlbumsByArtist(any());
        //then
        assertThat(cachedAlbumResList.get(0)).isEqualTo(cacheAlbumA.getAlbums().get(0));
    }


    @Test
    @DisplayName("getAlbumsByArtist 캐싱된 데이터가 없을 경우")
    void testGetAlbumsByArtistNotCaching(){
        //given
        List<AlbumRes>albumResList = new ArrayList<>();

        AlbumRes albumResA = AlbumRes.builder()
                .title(albumA.getTitle())
                .artist(albumA.getArtist())
                .releaseDate(albumA.getReleaseDate())
                .image(albumA.getImage())
                .build();

        AlbumRes albumResB = AlbumRes.builder()
                .title(albumB.getTitle())
                .artist(albumB.getArtist())
                .releaseDate(albumB.getReleaseDate())
                .image(albumB.getImage())
                .build();

        albumResList.add(albumResA);
        albumResList.add(albumResB);

        CacheAlbum cacheAlbumA = CacheAlbum.builder()
                .albums(albumResList)
                .artist(albumA.getArtist())
                .build();

        //when
        when(cacheAlbumRepository.findById(any())).thenReturn(Optional.empty());
        when(openApiService.getAlbums(any())).thenReturn(albumResList);
        List<AlbumRes> getAlbumResList = albumService.getAlbumsByArtist(any());
        //then
        assertThat(getAlbumResList.get(0)).isEqualTo(cacheAlbumA.getAlbums().get(0));
    }

}
