package com.example.deustobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.deustobank.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDni(String dni);
}