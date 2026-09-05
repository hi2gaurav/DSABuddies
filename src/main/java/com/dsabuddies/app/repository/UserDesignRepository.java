package com.dsabuddies.app.repository;

import com.dsabuddies.app.model.UserDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDesignRepository extends JpaRepository<UserDesign, Long> {
    List<UserDesign> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<UserDesign> findByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
