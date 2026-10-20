package com.skill_learning_platform.skill_learning_platform.config;

import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapTest {

    private final AdminRepository repository = mock(AdminRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ApplicationArguments arguments = mock(ApplicationArguments.class);
    private final Environment environment = mock(Environment.class);

    @Test
    void createsAdminWithEncodedPasswordWhenConfigured() {
        when(environment.getProperty("ADMIN_BOOTSTRAP_EMAIL")).thenReturn("admin@example.test");
        when(environment.getProperty("ADMIN_BOOTSTRAP_PASSWORD")).thenReturn("a-long-test-password");
        when(repository.findByEmail("admin@example.test")).thenReturn(Optional.empty());

        new AdminBootstrap(repository, passwordEncoder, environment).run(arguments);

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(admin ->
                "admin@example.test".equals(admin.getEmail())
                        && passwordEncoder.matches("a-long-test-password", admin.getPassword())
        ));
    }

    @Test
    void doesNotChangeAnExistingAdmin() {
        when(environment.getProperty("ADMIN_BOOTSTRAP_EMAIL")).thenReturn("admin@example.test");
        when(environment.getProperty("ADMIN_BOOTSTRAP_PASSWORD")).thenReturn("a-long-test-password");
        when(repository.findByEmail("admin@example.test")).thenReturn(Optional.of(new Admin()));

        new AdminBootstrap(repository, passwordEncoder, environment).run(arguments);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Admin.class));
    }

    @Test
    void rejectsPartialOrWeakConfiguration() {
        when(environment.getProperty("ADMIN_BOOTSTRAP_EMAIL")).thenReturn("admin@example.test");
        assertThrows(
                IllegalStateException.class,
                () -> new AdminBootstrap(repository, passwordEncoder, environment).run(arguments)
        );

        when(environment.getProperty("ADMIN_BOOTSTRAP_PASSWORD")).thenReturn("short");
        assertThrows(
                IllegalStateException.class,
                () -> new AdminBootstrap(repository, passwordEncoder, environment).run(arguments)
        );

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Admin.class));
    }
}
