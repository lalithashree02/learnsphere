package com.skill_learning_platform.skill_learning_platform.service;

import com.skill_learning_platform.skill_learning_platform.model.Admin;
import com.skill_learning_platform.skill_learning_platform.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Admin loginAdmin(String email, String password) {

        Admin admin =
                adminRepository.findByEmail(email).orElse(null);

        if (admin == null || password == null || admin.getPassword() == null) {
            return null;
        }

        String storedPassword = admin.getPassword();
        if (passwordEncoder.matches(password, storedPassword)) {
            return admin;
        }

        if (password.equals(storedPassword)) {
            admin.setPassword(passwordEncoder.encode(password));
            return adminRepository.save(admin);
        }

        return null;
    }
}