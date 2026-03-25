package com.example.deustobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.deustobank.model.User;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDni(String dni);

    @Query("SELECT DISTINCT a.user FROM Account a WHERE a.balance < 0")
    List<User> findUsersWithDebt();
}