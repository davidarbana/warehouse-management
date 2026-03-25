package com.davidarbana.warehouse.repository;

import com.davidarbana.warehouse.entity.User;
import com.davidarbana.warehouse.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findAllByRole(Role role);

    Boolean existsByEmail (String email);
}
