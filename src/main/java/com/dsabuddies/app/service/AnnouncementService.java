package com.dsabuddies.app.service;

import com.dsabuddies.app.dto.AnnouncementDto;
import com.dsabuddies.app.dto.BroadcastRequest;
import com.dsabuddies.app.model.Announcement;
import com.dsabuddies.app.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public List<AnnouncementDto> getActiveAnnouncements() {
        LocalDateTime now = LocalDateTime.now();
        return announcementRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .filter(a -> a.getExpiresAt() == null || a.getExpiresAt().isAfter(now))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDto> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementDto createAnnouncement(BroadcastRequest request, String authorName) {
        LocalDateTime expiresAt = null;
        if (request.expiresInDays() != null && request.expiresInDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.expiresInDays());
        }

        Announcement announcement = Announcement.builder()
                .title(request.title().trim())
                .message(request.message().trim())
                .priority(request.priority() != null ? request.priority().toUpperCase() : "NORMAL")
                .active(true)
                .authorName(authorName != null ? authorName : "Admin")
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();

        return toDto(announcementRepository.save(announcement));
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }

    private AnnouncementDto toDto(Announcement a) {
        return new AnnouncementDto(
                a.getId(),
                a.getTitle(),
                a.getMessage(),
                a.getPriority(),
                a.isActive(),
                a.getAuthorName(),
                a.getCreatedAt(),
                a.getExpiresAt()
        );
    }
}
