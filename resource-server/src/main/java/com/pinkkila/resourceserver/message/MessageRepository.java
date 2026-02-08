package com.pinkkila.resourceserver.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface MessageRepository extends ListCrudRepository<Message, Long>, PagingAndSortingRepository<Message, Long> {
    Page<Message> findByUserId(String userId, Pageable pageable);
    Optional<Message> findByIdAndUserId(Long id, String userId);
}
