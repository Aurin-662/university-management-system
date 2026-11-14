package com.university.ums.repository;

import com.university.ums.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    List<UserAccount> findByRole(Role role);
    Optional<UserAccount> findByIdAndRole(Long id, Role role);
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    boolean existsByRole(Role role);
}
