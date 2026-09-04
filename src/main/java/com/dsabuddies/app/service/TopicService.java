package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.TopicDto;
import com.dsabuddies.app.model.Topic;
import com.dsabuddies.app.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;

    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(t -> new TopicDto(t.getId(), t.getName(), t.getColor(), t.getIcon()))
                .collect(Collectors.toList());
    }

    public TopicDto createTopic(String name, String color, String icon) {
        Topic topic = Topic.builder().name(name).color(color).icon(icon).build();
        topic = topicRepository.save(topic);
        return new TopicDto(topic.getId(), topic.getName(), topic.getColor(), topic.getIcon());
    }
}
