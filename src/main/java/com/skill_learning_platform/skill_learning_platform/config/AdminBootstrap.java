package com.skill_learning_platform.skill_learning_platform.config;

import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminBootstrap(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            Environment environment) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = environment.getProperty("ADMIN_BOOTSTRAP_EMAIL");
        String password = environment.getProperty("ADMIN_BOOTSTRAP_PASSWORD");

        boolean hasEmail = email != null && !email.isBlank();
        boolean hasPassword = password != null && !password.isBlank();

        if (!hasEmail && !hasPassword) {
            return;
        }
        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException(
                    "Set both ADMIN_BOOTSTRAP_EMAIL and ADMIN_BOOTSTRAP_PASSWORD to provision an admin account."
            );
        }
        if (password.length() < 12) {
            throw new IllegalStateException(
                    "ADMIN_BOOTSTRAP_PASSWORD must be at least 12 characters long."
            );
        }

        String normalizedEmail = email.trim();
        if (adminRepository.findByEmail(normalizedEmail).isPresent()) {
            logger.info("Bootstrap admin already exists; no account changes made.");
            return;
        }

        Admin admin = new Admin();
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(password));
        adminRepository.save(admin);
        logger.info("Bootstrap admin account created.");
    }
}
