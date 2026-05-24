package com.omkarsathe.outvoice.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserOrganizationRepository extends JpaRepository<UserOrganization, UUID> {
}
