package com.omkarsathe.outvoice.entity;

import com.omkarsathe.outvoice.phone.PhoneCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CountryPhoneCodeRepository extends JpaRepository<CountryPhoneCode, UUID> {

    @Query("SELECT m.phoneCode FROM CountryPhoneCode m WHERE m.country.isoCode2 = :isoCode2 AND m.isPrimary = true")
    Optional<PhoneCode> findPrimaryPhoneCodeByCountryIsoCode(@Param("isoCode2") String isoCode2);
}
