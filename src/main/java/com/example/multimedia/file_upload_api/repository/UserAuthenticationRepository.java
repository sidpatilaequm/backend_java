package com.example.multimedia.file_upload_api.repository;

import com.example.multimedia.file_upload_api.entity.Authorization;
import com.example.multimedia.file_upload_api.entity.UserAuthentication;
import com.example.multimedia.file_upload_api.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthenticationRepository extends JpaRepository<UserAuthentication, Long> {

    @Query("SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END FROM UserAuthentication ua WHERE ua.userId = :#{#user.userId} AND ua.authKey = :#{#authorization.authKey}")
    boolean existsByUserIdAndAuthKey(@Param("user") UserDetail user, @Param("authorization") Authorization authorization);
    Optional<UserAuthentication> findByUserId(Long userId);
    @Query("SELECT ua FROM UserAuthentication ua WHERE ua.authKey = :authKey AND ua.isActive = true")
    List<UserAuthentication> findByAuthKey(@Param("authKey") String authKey);

}
