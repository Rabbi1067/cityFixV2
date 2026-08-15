package bd.cityv1.complaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByCitizenId(Long citizenId);

    @Query("SELECT c FROM Complaint c WHERE " +
            "c.citizen.id = :citizenId " +
            "AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR c.category = :category) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.createdAt DESC")
    Page<Complaint> searchComplaints(@Param("citizenId") Long citizenId,
                                     @Param("keyword") String keyword,
                                     @Param("category") String category,
                                     @Param("status") Status status,
                                     Pageable pageable);

    long countByStatus(Status status);
    long countByStatusNot(Status status);

    // Logged-in citizen-er nijer complaint-er count (dashboard-e "Active"/"Resolved" card-er jonno)
    long countByCitizenIdAndStatusNot(Long citizenId, Status status);
    long countByCitizenIdAndStatus(Long citizenId, Status status);

    List<Complaint> findAllByOrderByCreatedAtDesc();
}