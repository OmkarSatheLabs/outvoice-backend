package com.omkarsathe.outvoice.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(UUID id);

    Optional<User> findByMobileAndPhoneCodeId(String mobile, UUID phoneCodeId);

    Optional<User> findByEmail(String email);
}
