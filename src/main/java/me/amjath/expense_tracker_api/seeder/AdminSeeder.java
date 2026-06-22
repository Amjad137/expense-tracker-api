package me.amjath.expense_tracker_api.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.user.entity.Role;
import me.amjath.expense_tracker_api.user.entity.User;
import me.amjath.expense_tracker_api.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Runs once at application startup.
 * If no admin account exists, generates a secure random password,
 * creates the admin user, and prints the credentials to the console.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@expensetracker.com";
    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("Admin account already exists — skipping seeder.");
            return;
        }

        String rawPassword = generateSecurePassword(12);

        User admin = new User();
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);

        // Print credentials to console (only visible once)
        String banner = """
                
                ╔════════════════════════════════════════╗
                ║         ADMIN ACCOUNT CREATED          ║
                ╠════════════════════════════════════════╣
                ║  Email   : admin@expensetracker.com    ║
                ║  Password: %-30s ║
                ╠════════════════════════════════════════╣
                ║  ⚠  CHANGE THIS PASSWORD AFTER LOGIN   ║
                ╚════════════════════════════════════════╝
                """.formatted(rawPassword);

        System.out.println(banner);
        log.warn("Admin account created with temporary password. CHANGE IT IMMEDIATELY.");
    }

    private String generateSecurePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        // Guarantee at least one uppercase, lowercase, digit, and special char
        sb.append(randomChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ", random));
        sb.append(randomChar("abcdefghijklmnopqrstuvwxyz", random));
        sb.append(randomChar("0123456789", random));
        sb.append(randomChar("!@#$%^&*", random));
        for (int i = 4; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        // Shuffle to avoid predictable position of mandatory chars
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    private char randomChar(String set, SecureRandom random) {
        return set.charAt(random.nextInt(set.length()));
    }
}
