package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.BookmarkDto;
import com.dsabuddies.app.dto.SaveNoteRequest;
import com.dsabuddies.app.dto.UserNoteDto;
import com.dsabuddies.app.model.Bookmark;
import com.dsabuddies.app.model.Task;
import com.dsabuddies.app.model.User;
import com.dsabuddies.app.model.UserNote;
import com.dsabuddies.app.repository.BookmarkRepository;
import com.dsabuddies.app.repository.TaskRepository;
import com.dsabuddies.app.repository.UserNoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkAndNoteTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserNoteRepository userNoteRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    @InjectMocks
    private UserNoteService userNoteService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("dev@dsabuddies.com").name("Dev").build();
        task = Task.builder().id(20L).title("LRU Cache").build();
    }

    @Test
    void testAddBookmark_Success() {
        when(bookmarkRepository.existsByUserIdAndTaskId(1L, 20L)).thenReturn(false);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(bookmarkRepository.save(any(Bookmark.class))).thenAnswer(inv -> {
            Bookmark b = inv.getArgument(0);
            b.setId(101L);
            return b;
        });

        BookmarkDto dto = bookmarkService.addBookmark(user, 20L);

        assertNotNull(dto);
        assertEquals(20L, dto.taskId());
        assertEquals("LRU Cache", dto.taskTitle());
    }

    @Test
    void testSaveNote_CreatesNewNote() {
        when(userNoteRepository.findByUserIdAndTaskId(1L, 20L)).thenReturn(Optional.empty());
        when(taskRepository.findById(20L)).thenReturn(Optional.of(task));
        when(userNoteRepository.save(any(UserNote.class))).thenAnswer(inv -> {
            UserNote un = inv.getArgument(0);
            un.setId(201L);
            return un;
        });

        SaveNoteRequest request = new SaveNoteRequest("Use Doubly Linked List + HashMap", "class LRU {}", "java");
        UserNoteDto dto = userNoteService.saveNote(user, 20L, request);

        assertNotNull(dto);
        assertEquals("Use Doubly Linked List + HashMap", dto.content());
        assertEquals("class LRU {}", dto.codeSnippet());
        assertEquals("java", dto.language());
    }
}
