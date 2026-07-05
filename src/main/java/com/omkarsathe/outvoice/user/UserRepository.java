package com.omkarsathe.outvoice.user;

import com.omkarsathe.outvoice.user.dto.UserProfileResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByMobileAndPhoneCodeId(String mobile, UUID phoneCodeId);

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR u.mobile LIKE CONCAT('%', :query, '%')")
    List<UserEntity> searchUsers(@Param("query") String query);

    @Query("SELECT DISTINCT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.userWorkspaces uw " +
            "LEFT JOIN FETCH uw.workspace " +
            "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<UserEntity> findAllByEmailContaining(@Param("query") String query);

    Optional<UserEntity> getUserById(UUID id);

    @Query("SELECT u FROM UserEntity u WHERE u.phoneCode.id = :phoneCodeId AND u.mobile = :mobile")
    List<UserEntity> findAllByPhoneCodeIdAndMobileContaining(@Param("phoneCodeId") UUID phoneCodeId, @Param("mobile") String mobile);
}
