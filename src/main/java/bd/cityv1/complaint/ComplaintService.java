package bd.cityv1.complaint;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public Complaint saveComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with id: " + id));
    }

    public Page<Complaint> searchComplaints(Long citizenId, String keyword, String category, Status status, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        return complaintRepository.searchComplaints(
                citizenId,
                (keyword == null || keyword.isBlank()) ? null : keyword,
                (category == null || category.isBlank()) ? null : category,
                status,
                pageable
        );
    }

    public long countActive(Long citizenId) {
        return complaintRepository.countByCitizenIdAndStatusNot(citizenId, Status.RESOLVED);
    }

    public long countResolved(Long citizenId) {
        return complaintRepository.countByCitizenIdAndStatus(citizenId, Status.RESOLVED);
    }
}