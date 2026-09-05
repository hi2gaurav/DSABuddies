package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(Long userId);
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    @Query("SELECT ub.badge.id FROM UserBadge ub WHERE ub.user.id = :userId")
    List<Long> findBadgeIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
