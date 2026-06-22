package me.amjath.expense_tracker_api.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.amjath.expense_tracker_api.common.BaseEntity;
import me.amjath.expense_tracker_api.user.entity.User;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * Category entity.
 * System categories: is_system=true, user_id=null (shared across all users).
 * Custom categories: is_system=false, user_id=<owning user>.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String icon;

    @Column(length = 20)
    private String color;

    @Column(name = "is_system", nullable = false)
    private boolean system = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
