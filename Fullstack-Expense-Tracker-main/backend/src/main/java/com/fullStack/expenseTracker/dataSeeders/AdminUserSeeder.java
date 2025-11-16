package com.fullStack.expenseTracker.dataSeeders;

import com.fullStack.expenseTracker.enums.ERole;
import com.fullStack.expenseTracker.models.Role;
import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.repository.RoleRepository;
import com.fullStack.expenseTracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
public class AdminUserSeeder {

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin@123";
    
    // New admin user credentials - modify these as needed
    private static final String ADMIN2_EMAIL = "admin2@gmail.com";
    private static final String ADMIN2_USERNAME = "admin2";
    private static final String ADMIN2_PASSWORD = "admin2@123";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @EventListener
    @Transactional
    public void seedAdminUser(ContextRefreshedEvent event) {
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));
        
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

        // Seed first admin user
        seedAdminUser(ADMIN_EMAIL, ADMIN_USERNAME, ADMIN_PASSWORD, adminRole, userRole);
        
        // Seed second admin user
        seedAdminUser(ADMIN2_EMAIL, ADMIN2_USERNAME, ADMIN2_PASSWORD, adminRole, userRole);
    }
    
    private void seedAdminUser(String email, String username, String password, Role adminRole, Role userRole) {
        User adminUser = userRepository.findByEmail(email).orElse(null);

        if (adminUser == null) {
            // Create new admin user
            adminUser = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .enabled(true)
                    .build();

            adminUser.setRoles(new HashSet<>());
            adminUser.getRoles().add(adminRole);
            adminUser.getRoles().add(userRole);

            userRepository.save(adminUser);
            System.out.println("Created admin user: " + email);
        } else {
            // Update existing admin user to ensure it has both roles
            if (adminUser.getRoles() == null) {
                adminUser.setRoles(new HashSet<>());
            }
            if (!adminUser.getRoles().contains(adminRole)) {
                adminUser.getRoles().add(adminRole);
            }
            if (!adminUser.getRoles().contains(userRole)) {
                adminUser.getRoles().add(userRole);
            }
            userRepository.save(adminUser);
            System.out.println("Updated admin user: " + email);
        }
    }
}

