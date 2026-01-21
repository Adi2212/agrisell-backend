package com.agridev.repository;

import java.util.List;
import java.util.Optional;

import com.agridev.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import com.agridev.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
}
