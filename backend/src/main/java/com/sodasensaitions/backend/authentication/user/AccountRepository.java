package com.sodasensaitions.backend.authentication.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {

  Optional<Account> findByUsernameOrEmail(String username, String email);

  Optional<Account> findByUsername(String username);
}
