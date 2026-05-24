package com.omkarsathe.outvoice.country;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<Country, UUID> {

    @Query("SELECT c FROM Country c LEFT JOIN FETCH c.defaultCurrency WHERE c.isActive = true ORDER BY c.name ASC")
    List<Country> findAllActiveWithCurrency();
}
