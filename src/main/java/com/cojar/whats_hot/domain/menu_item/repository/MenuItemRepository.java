package com.cojar.whats_hot.domain.menu_item.repository;

import com.cojar.whats_hot.domain.menu_item.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}
