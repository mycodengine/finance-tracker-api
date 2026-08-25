package com.financetracker.repository;

import com.financetracker.domain.entity.Account;
import com.financetracker.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUser(User user);

    Optional<Account> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
