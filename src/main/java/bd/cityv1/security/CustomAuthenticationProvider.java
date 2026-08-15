package bd.cityv1.security;

import bd.cityv1.admin.Admin;
import bd.cityv1.admin.AdminRepository;
import bd.cityv1.citizen.Citizen;
import bd.cityv1.citizen.CitizenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final CitizenRepository citizenRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        // Age Citizen table-e khoja hoy; na pele Admin table-e khoja hoy.
        Optional<Citizen> citizen = citizenRepository.findByEmail(email);
        if (citizen.isPresent()) {
            return authenticate(email, rawPassword, citizen.get().getPassword(), List.of("CITIZEN"));
        }

        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        if (!admin.isEnabled()) {
            boolean tempBlockExpired = admin.getBlockedUntil() != null
                    && admin.getBlockedUntil().isBefore(LocalDateTime.now());

            if (tempBlockExpired) {
                // Temp block-er shomoy pero geche — nijei abar enable kore dey.
                admin.setEnabled(true);
                admin.setBlockedUntil(null);
                adminRepository.save(admin);
            } else {
                throw new DisabledException("This admin account has been blocked. Contact a super admin.");
            }
        }

        return authenticate(email, rawPassword, admin.getPassword(), admin.getRoles());
    }

    private Authentication authenticate(String email, String rawPassword, String encodedPassword, List<String> roles) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new UsernameNotFoundException("Invalid email or password");
        }

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(email, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}