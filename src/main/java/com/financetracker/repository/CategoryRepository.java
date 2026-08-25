package com.financetracker.repository;

import com.financetracker.domain.entity.Category;
import com.financetracker.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Returns all system defaults (user IS NULL) plus the given user's personal categories. */
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user ORDER BY c.name")
    List<Category> findAllVisibleToUser(@Param("user") User user);

    Optional<Category> findByIdAndUser(Long id, User user);
}
