package bd.cityv1.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT a FROM Admin a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Admin> searchByNameOrEmail(@Param("query") String query);
}