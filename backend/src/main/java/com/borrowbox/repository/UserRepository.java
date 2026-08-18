package com.borrowbox.repository;

import com.borrowbox.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByIsActive(boolean isActive, Pageable pageable);

    long countByIsActive(boolean isActive);

    long countByIsVerified(boolean isVerified);
}
