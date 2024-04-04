package com.cl.publisher.online.iamservice.repositories;

import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cl.publisher.online.iamservice.entities.Role;
import com.cl.publisher.online.iamservice.enums.Roles;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Roles name);
}
