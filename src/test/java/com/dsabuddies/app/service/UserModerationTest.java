package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.CompleteTaskRequest;
import com.dsabuddies.app.dto.UserDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskSheet;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TaskSheetRepository;
import com.dsabuddies.app.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserModerationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskCompletionService taskCompletionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskSheetRepository taskSheetRepository;

    @Test
    @DisplayName("Admin can mute user and muted status is properly set with expiration")
    void testMuteUser() {
        User user = userRepository.save(User.builder()
                .email("spammer@dsabuddies.com")
                .name("Spammer")
                .build());

        UserDto mutedDto = userService.updateMemberStatus(user.getId(), "MUTED", 48, "Inappropriate chat messages");
        assertThat(mutedDto.status()).isEqualTo("MUTED");
        assertThat(mutedDto.mutedUntil()).isAfter(LocalDateTime.now().plusHours(47));
        assertThat(mutedDto.moderationReason()).isEqualTo("Inappropriate chat messages");
    }

    @Test
    @DisplayName("Primary admin account status cannot be modified")
    void testAdminCannotBeMutedOrBanned() {
        User admin = userRepository.save(User.builder()
                .email(UserService.ADMIN_EMAIL)
                .name("Primary Admin")
                .role("ROLE_ADMIN")
                .build());

        assertThatThrownBy(() -> userService.updateMemberStatus(admin.getId(), "BANNED", null, "Testing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Primary admin account status cannot be modified");
    }

    @Test
    @DisplayName("Banned user cannot complete tasks and receives AccessDeniedException")
    void testBannedUserCannotCompleteTasks() {
        User bannedUser = userRepository.save(User.builder()
                .email("cheater@dsabuddies.com")
                .name("Cheater")
                .status("BANNED")
                .moderationReason("Automated script usage")
                .build());

        TaskSheet sheet = taskSheetRepository.save(TaskSheet.builder()
                .title("Test Sheet")
                .description("Test")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build());

        Task task = taskRepository.save(Task.builder()
                .taskSheet(sheet)
                .title("Test Problem")
                .xpReward(50)
                .build());

        assertThatThrownBy(() -> taskCompletionService.completeTask(task.getId(), bannedUser.getId(), new CompleteTaskRequest(null, null, null, null)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("suspended");
    }
}
