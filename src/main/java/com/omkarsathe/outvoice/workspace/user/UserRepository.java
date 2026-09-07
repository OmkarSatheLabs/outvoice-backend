package com.omkarsathe.outvoice.workspace.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByMobileAndPhoneCode_Code(String mobile, String phoneCode);

    @EntityGraph(attributePaths = "phoneCode")
    Optional<User> findByEmailAndPhoneCode_CodeAndMobile(
            String email,
            String phoneCode,
            String mobile
    );

    @EntityGraph(attributePaths = "phoneCode")
    Optional<User> findByPhoneCode_CodeAndMobile(
            String phoneCode,
            String mobile
    );
}
