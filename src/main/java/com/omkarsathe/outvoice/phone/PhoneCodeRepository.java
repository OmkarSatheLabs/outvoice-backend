package com.omkarsathe.outvoice.phone;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhoneCodeRepository extends JpaRepository<PhoneCode, UUID> {
    List<PhoneCode> findAllByOrderByCodeAsc();
}
