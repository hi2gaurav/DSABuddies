package com.dsabuddies.app.config;

import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskSheet;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TaskSheetRepository;
import com.dsabuddies.app.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final TaskSheetRepository taskSheetRepository;
    private final TaskRepository taskRepository;
    private final com.dsabuddies.app.repository.UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        cleanupAdminRoles();
        if (topicRepository.count() == 0) {
            seedTopics();
            seedTasks();
        }
    }

    private void cleanupAdminRoles() {
        List<com.dsabuddies.app.model.User> users = userRepository.findAll();
        for (com.dsabuddies.app.model.User u : users) {
            if ("hi2gauravgb@gmail.com".equalsIgnoreCase(u.getEmail())) {
                u.setRole("ROLE_ADMIN");
            } else if ("ROLE_ADMIN".equals(u.getRole())) {
                u.setRole("ROLE_USER");
            }
        }
        userRepository.saveAll(users);
    }

    private void seedTopics() {
        topicRepository.saveAll(List.of(
            Topic.builder().name("Arrays").color("#3B82F6").icon("📊").build(),
            Topic.builder().name("Strings").color("#8B5CF6").icon("🔤").build(),
            Topic.builder().name("Linked Lists").color("#EC4899").icon("🔗").build(),
            Topic.builder().name("Stacks").color("#F59E0B").icon("📚").build(),
            Topic.builder().name("Queues").color("#14B8A6").icon("🚶").build(),
            Topic.builder().name("Trees").color("#22C55E").icon("🌳").build(),
            Topic.builder().name("Graphs").color("#6366F1").icon("🕸️").build(),
            Topic.builder().name("Hashing").color("#EF4444").icon("#️⃣").build(),
            Topic.builder().name("Sorting").color("#F97316").icon("🔄").build(),
            Topic.builder().name("Searching").color("#06B6D4").icon("🔍").build(),
            Topic.builder().name("Dynamic Programming").color("#A855F7").icon("💡").build(),
            Topic.builder().name("Greedy").color("#84CC16").icon("🎯").build(),
            Topic.builder().name("Backtracking").color("#E11D48").icon("↩️").build()
        ));
    }

    private void seedTasks() {
        Topic arrays = topicRepository.findByName("Arrays").orElseThrow();
        Topic strings = topicRepository.findByName("Strings").orElseThrow();

        TaskSheet sheet = TaskSheet.builder()
                .title("Week 1 — Arrays & Strings Fundamentals")
                .description("Fundamental array and string manipulation problems to build a strong base.")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .sheetType("WEEKLY")
                .build();
        
        sheet = taskSheetRepository.save(sheet);

        taskRepository.saveAll(List.of(
            Task.builder().title("Two Sum").difficulty("EASY").topic(arrays).xpReward(100).platformLink("https://leetcode.com/problems/two-sum/").taskSheet(sheet).build(),
            Task.builder().title("Valid Anagram").difficulty("EASY").topic(strings).xpReward(100).platformLink("https://leetcode.com/problems/valid-anagram/").taskSheet(sheet).build(),
            Task.builder().title("3Sum").difficulty("MEDIUM").topic(arrays).xpReward(200).platformLink("https://leetcode.com/problems/3sum/").taskSheet(sheet).build(),
            Task.builder().title("Longest Substring Without Repeating Characters").difficulty("MEDIUM").topic(strings).xpReward(200).platformLink("https://leetcode.com/problems/longest-substring-without-repeating-characters/").taskSheet(sheet).build(),
            Task.builder().title("Trapping Rain Water").difficulty("HARD").topic(arrays).xpReward(350).platformLink("https://leetcode.com/problems/trapping-rain-water/").taskSheet(sheet).build()
        ));
    }
}
