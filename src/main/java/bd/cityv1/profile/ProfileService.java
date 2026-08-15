package bd.cityv1.profile;

import bd.cityv1.citizen.Citizen;
import bd.cityv1.citizen.CitizenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CitizenRepository citizenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "uploads/";

    public Citizen getCitizen(String email) {
        return citizenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Citizen not found"));
    }

    public Citizen updatePersonalInfo(String email, UpdatePersonalInfoDto dto) {
        Citizen citizen = getCitizen(email);

        boolean phoneChanged = !citizen.getPhone().equals(dto.getPhone());
        if (phoneChanged && citizenRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Phone number already in use");
        }

        citizen.setName(dto.getName());
        citizen.setPhone(dto.getPhone());
        citizen.getAddress().setStreet(dto.getStreet());
        citizen.getAddress().setCity(dto.getCity());
        citizen.getAddress().setZipCode(dto.getZipCode());

        return citizenRepository.save(citizen);
    }

    public void changePassword(String email, ChangePasswordDto dto) {
        Citizen citizen = getCitizen(email);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), citizen.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (dto.getNewPassword().equals(dto.getCurrentPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        citizen.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        citizenRepository.save(citizen);
    }

    public Citizen updateAvatar(String email, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select an image to upload");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Image must be under 5MB");
        }

        Citizen citizen = getCitizen(email);

        Files.createDirectories(Path.of(UPLOAD_DIR));
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.write(Path.of(UPLOAD_DIR + fileName), file.getBytes());

        citizen.setProfileImagePath("/" + UPLOAD_DIR + fileName);
        return citizenRepository.save(citizen);
    }
}