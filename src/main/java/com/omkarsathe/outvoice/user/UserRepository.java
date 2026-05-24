package com.omkarsathe.outvoice.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE (:email IS NOT NULL AND u.email = :email) OR (:mobile IS NOT NULL AND u.mobile = :mobile)")
    Optional<User> findByEmailOrMobileIfPresent(@Param("email") String email, @Param("mobile") String mobile);
}
