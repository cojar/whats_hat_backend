package com.cojar.whats_hot.domain.file.repository;

import com.cojar.whats_hot.domain.file.entity.SaveFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaveFileRepository extends JpaRepository<SaveFile, Long> {
}
