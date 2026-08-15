package bd.cityv1.adminProfile;

import bd.cityv1.admin.Admin;
import bd.cityv1.admin.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminProfileService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin getAdmin(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    // Email intentionally excluded here - it's the login identifier and stays fixed.
    public Admin updatePersonalInfo(String email, UpdateAdminPersonalInfoDto dto) {
        Admin admin = getAdmin(email);

        admin.setName(dto.getName());
        admin.getContactInfo().setPhone(dto.getPhone());
        admin.getContactInfo().setDepartment(dto.getDepartment());

        return adminRepository.save(admin);
    }

    public void changePassword(String email, ChangePasswordDto dto) {
        Admin admin = getAdmin(email);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), admin.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (dto.getNewPassword().equals(dto.getCurrentPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        adminRepository.save(admin);
    }
}