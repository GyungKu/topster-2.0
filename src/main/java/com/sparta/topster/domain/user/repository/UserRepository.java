package com.sparta.topster.domain.user.repository;

import com.sparta.topster.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(domainClass = User.class, idClass = Long.class)
public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findBynickname(String nickname);

    User save(User user);

    User findById(Long id);

    User findByKakaoId(Long kakaoId);

    User findByGoogleId(Long googleId);

    User findByEmail(String kakaoEmail);

    @Query("select u from User u where u.email = :googleEmail")
    User findByGoogleEmail(String googleEmail);

    @Query("select u from User u where u.email = :email")
    Optional<User> findByUserEmail(String email);

    @Modifying
    @Query("delete from User u where u.id = :userId")
    void deleteById(Long userId);
}
