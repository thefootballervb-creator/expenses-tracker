package com.fullStack.expenseTracker.repository;


import java.util.Optional;

import com.fullStack.expenseTracker.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query(value = "SELECT DISTINCT u.* FROM users u " +
            "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE (:roleId IS NULL OR ur.role_id = :roleId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR u.username LIKE '%' || :keyword || '%' OR u.email LIKE '%' || :keyword || '%') " +
            "ORDER BY u.id",
            countQuery = "SELECT COUNT(DISTINCT u.id) FROM users u " +
                    "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
                    "WHERE (:roleId IS NULL OR ur.role_id = :roleId) " +
                    "AND (:keyword IS NULL OR :keyword = '' OR u.username LIKE '%' || :keyword || '%' OR u.email LIKE '%' || :keyword || '%')",
            nativeQuery = true)
    Page<User> findAll(Pageable pageable, @Param("roleId") Integer roleId, @Param("keyword") String keyword);

}
