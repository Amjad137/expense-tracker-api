package me.amjath.expense_tracker_api.category.repository;

import me.amjath.expense_tracker_api.category.entity.Category;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /** System categories (shared, user-independent). */
    List<Category> findAllBySystemTrue();

    /** User's custom categories. */
    Page<Category> findAllByUserAndSystemFalse(User user, Pageable pageable);

    /** All categories visible to a user: system + their own custom. */
    @Query("SELECT c FROM Category c WHERE (c.system = true OR c.user = :user)")
    Page<Category> findAllVisibleToUser(User user, Pageable pageable);

    /** Prevent duplicate custom category names per user. */
    boolean existsByNameAndUserAndSystemFalse(String name, User user);

    Optional<Category> findByIdAndUser(UUID id, User user);

    Optional<Category> findByIdAndSystemTrue(UUID id);
}
