package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.AdminOverviewStatsDto;
import com.dsabuddies.app.dto.EngagementTrendDto;
import com.dsabuddies.app.dto.TopicDropOffDto;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TopicRepository;
import com.dsabuddies.app.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AdminAnalyticsServiceTest {

    @Autowired
    private AdminAnalyticsService adminAnalyticsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    @DisplayName("AdminAnalyticsService should compute overview metrics and trends")
    void testOverviewAndTrends() {
        userRepository.save(User.builder()
                .email("analytics_user@dsabuddies.com")
                .name("Analytics User")
                .totalXp(300)
                .lastActiveDate(LocalDate.now())
                .currentStreak(2)
                .consistencyScore(75.0)
                .build());

        AdminOverviewStatsDto overview = adminAnalyticsService.getOverviewStats();
        assertThat(overview.totalUsers()).isGreaterThan(0);
        assertThat(overview.dailyActiveUsers()).isGreaterThan(0);

        List<EngagementTrendDto> trends = adminAnalyticsService.getEngagementTrend(7);
        assertThat(trends).hasSize(7);

        List<TopicDropOffDto> dropOffs = adminAnalyticsService.getTopicDropOff();
        assertThat(dropOffs).isNotNull();
    }
}
