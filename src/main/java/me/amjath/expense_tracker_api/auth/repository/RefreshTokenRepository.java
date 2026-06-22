package me.amjath.expense_tracker_api.auth.repository;

import me.amjath.expense_tracker_api.auth.entity.RefreshToken;
import me.amjath.expense_tracker_api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllUserTokens(User user, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteAllExpiredTokens(LocalDateTime now);
}
