package bd.cityv1.admin;

import bd.cityv1.admin.Admin;
import bd.cityv1.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// App start howar shomoy check kore, admins table-e kono row na thakle
// nijei ekta default SUPER_ADMIN toiri kore dey. Manually DB te kichu korte hobe na.
// Eita-i shurute thaka ekmatro admin, tai eke shob-thekey upore SUPER_ADMIN role deya hoyeche.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@cityfix.com";
    private static final String ADMIN_PASSWORD = "Admin@123"; // <-- login korar por change kore nao
    private static final String INITIAL_ROLE = "SUPER_ADMIN";

    @Override
    public void run(String... args) {
        if (adminRepository.existsByEmail(ADMIN_EMAIL)) {
            return; // already ache, dobara toiri korbe na
        }

        Admin admin = new Admin();
        admin.setName("System Admin");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRoles(new ArrayList<>(List.of(INITIAL_ROLE)));

        adminRepository.save(admin);

        log.info("Default {} created -> email: {} | password: {}", INITIAL_ROLE, ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}