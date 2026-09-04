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

    // UserAuthentication.authKey stores the numeric Authorization.authId AS A STRING (every writer
    // does setAuthKey(String.valueOf(authId))) — comparing it against authorization.authKey (the
    // string role key, e.g. "employee") compared two different string spaces and could never
    // match, so the "already registered with this role" guard this backs silently never fired.
    @Query("SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END FROM UserAuthentication ua WHERE ua.userId = :#{#user.userId} AND ua.authKey = :#{#authorization.authId.toString()}")
    boolean existsByUserIdAndAuthKey(@Param("user") UserDetail user, @Param("authorization") Authorization authorization);
    Optional<UserAuthentication> findByUserId(Long userId);
    List<UserAuthentication> findByUserIdIn(List<Long> userIds);
    @Query("SELECT ua FROM UserAuthentication ua WHERE ua.authKey = :authKey AND ua.isActive = true")
    List<UserAuthentication> findByAuthKey(@Param("authKey") String authKey);

}
