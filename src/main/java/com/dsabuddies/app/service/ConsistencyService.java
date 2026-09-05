package com.dsabuddies.app.service;

import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsistencyService {

    private final TaskCompletionRepository taskCompletionRepository;
    private final UserRepository userRepository;

    @Transactional
    public double updateConsistencyScore(User user) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<TaskCompletion> completions = taskCompletionRepository.findByUserIdAndCompletedAtGreaterThanEqual(user.getId(), thirtyDaysAgo);
        
        Set<LocalDate> activeDates = completions.stream()
                .map(c -> c.getCompletedAt().toLocalDate())
                .collect(Collectors.toSet());

        double score = Math.round((activeDates.size() / 30.0) * 1000.0) / 10.0;
        user.setConsistencyScore(score);
        userRepository.save(user);
        return score;
    }

    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    @Transactional
    public void updateAllUsersConsistency() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            updateConsistencyScore(user);
        }
    }
}
