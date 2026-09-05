package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.WeakTopicDto;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.TaskCompletion;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.TaskCompletionRepository;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeakAreaServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCompletionRepository taskCompletionRepository;

    @InjectMocks
    private WeakAreaService weakAreaService;

    @Test
    void testGetWeakTopics_IdentifiesLowCompletionTopic() {
        Topic dp = Topic.builder().id(1L).name("Dynamic Programming").color("#8B5CF6").build();
        Topic arrays = Topic.builder().id(2L).name("Arrays").color("#3B82F6").build();

        User user = User.builder().id(5L).build();
        Task t1 = Task.builder().id(10L).topic(arrays).build();
        Task t2 = Task.builder().id(11L).topic(dp).build();

        when(topicRepository.findAll()).thenReturn(List.of(dp, arrays));
        when(taskRepository.countByTopicId(1L)).thenReturn(10L); // 10 DP problems
        when(taskRepository.countByTopicId(2L)).thenReturn(2L);  // 2 Array problems

        // User solved 1 DP problem (10% completion -> weak) and 2 Array problems (100% completion -> strong)
        TaskCompletion tcDp = TaskCompletion.builder().task(t2).user(user).selfRating(2).build();
        TaskCompletion tcArr1 = TaskCompletion.builder().task(t1).user(user).selfRating(5).build();
        TaskCompletion tcArr2 = TaskCompletion.builder().task(t1).user(user).selfRating(5).build();

        when(taskCompletionRepository.findByUserId(5L)).thenReturn(List.of(tcDp, tcArr1, tcArr2));

        List<WeakTopicDto> weakTopics = weakAreaService.getWeakTopics(5L);

        assertEquals(1, weakTopics.size());
        assertEquals("Dynamic Programming", weakTopics.get(0).topicName());
        assertEquals(10.0, weakTopics.get(0).completionPercentage());
        assertEquals(2.0, weakTopics.get(0).averageRating());
    }
}
