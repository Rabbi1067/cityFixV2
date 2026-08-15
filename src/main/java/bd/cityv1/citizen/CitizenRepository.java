package bd.cityv1.citizen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Citizen> findByEmail(String email);

    List<Citizen> findAllByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime dateTime);
}