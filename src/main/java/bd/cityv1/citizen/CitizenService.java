package bd.cityv1.citizen;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final PasswordEncoder passwordEncoder;

    public Citizen registerCitizen(Citizen citizen) {
        citizen.setCitizenId(generateCitizenId());
        citizen.setPassword(passwordEncoder.encode(citizen.getPassword()));
        return citizenRepository.save(citizen);
    }

    public boolean isEmailTaken(String email) {
        return citizenRepository.existsByEmail(email);
    }

    public boolean isPhoneTaken(String phone) {
        return citizenRepository.existsByPhone(phone);
    }

    private String generateCitizenId() {
        return "CTZ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}