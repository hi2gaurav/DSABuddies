package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.BookmarkDto;
import com.dsabuddies.app.model.Bookmark;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.repository.BookmarkRepository;
import com.dsabuddies.app.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<BookmarkDto> getBookmarks(Long userId) {
        return bookmarkRepository.findByUserIdWithTask(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long userId, Long taskId) {
        return bookmarkRepository.existsByUserIdAndTaskId(userId, taskId);
    }

    @Transactional
    public BookmarkDto addBookmark(User user, Long taskId) {
        if (bookmarkRepository.existsByUserIdAndTaskId(user.getId(), taskId)) {
            return toDto(bookmarkRepository.findByUserIdAndTaskId(user.getId(), taskId).orElseThrow());
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .task(task)
                .build();

        return toDto(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public void removeBookmark(Long userId, Long taskId) {
        bookmarkRepository.deleteByUserIdAndTaskId(userId, taskId);
    }

    private BookmarkDto toDto(Bookmark b) {
        Task t = b.getTask();
        return new BookmarkDto(
                b.getId(),
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getDifficulty(),
                t.getTopic() != null ? t.getTopic().getName() : null,
                t.getTopic() != null ? t.getTopic().getColor() : null,
                t.getPlatformLink(),
                t.getXpReward(),
                b.getCreatedAt()
        );
    }
}
