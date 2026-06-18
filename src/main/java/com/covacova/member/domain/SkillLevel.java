package com.covacova.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "skill_levels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkillLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_level_id", nullable = false, updatable = false)
    private Long skillLevelId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
