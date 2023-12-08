package com.cojar.whats_hot.domain.review.entity;

import com.cojar.whats_hot.domain.comment.entity.Comment;
import com.cojar.whats_hot.domain.file.entity.SaveFile;
import com.cojar.whats_hot.domain.member.entity.Member;
import com.cojar.whats_hot.domain.spot.entity.Spot;
import com.cojar.whats_hot.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(callSuper = true)
@Entity
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Spot spot;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    private LocalDateTime visitDate;

    private String title;

    private String content;

    private Double score;

    @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE)
    private List<SaveFile> images;

    @Enumerated(value = EnumType.STRING)
    private ReviewStatus status;

    private boolean validated;

    @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE)
    private List<Comment> comments;

    private Long liked;

    @ManyToMany
    private Set<Member> likedMember;
}
