package com.pinkkila.resourceserver.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends ListCrudRepository<Message, Long>, PagingAndSortingRepository<Message, Long> {
    Page<Message> findByUserId(UUID userId, Pageable pageable);
    Optional<Message> findByIdAndUserId(Long id, UUID userId);
}
