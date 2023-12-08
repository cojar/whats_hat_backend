package com.cojar.whats_hot.domain.category.entity;

import com.cojar.whats_hot.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(callSuper = true)
@Entity
public class Category extends BaseEntity {

    private String name;

    private Integer depth;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Category> children;
}
