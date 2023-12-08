package com.cojar.whats_hot.domain.comment.entity;

import com.cojar.whats_hot.domain.member.entity.Member;
import com.cojar.whats_hot.domain.review.entity.Review;
import com.cojar.whats_hot.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(callSuper = true)
@Entity
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;

    private Long liked;

    @ManyToMany
    private Set<Member> likedMember;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment tag;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Comment> children;
}
