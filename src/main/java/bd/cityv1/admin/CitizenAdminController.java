package bd.cityv1.admin;

import bd.cityv1.citizen.Citizen;
import bd.cityv1.citizen.CitizenAddress;
import bd.cityv1.citizen.CitizenRepository;
import bd.cityv1.complaint.Complaint;
import bd.cityv1.complaint.ComplaintRepository;
import bd.cityv1.complaint.Priority;
import bd.cityv1.complaint.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class CitizenAdminController {

    private final CitizenRepository citizenRepository;
    private final ComplaintRepository complaintRepository;
    private final AdminRepository adminRepository;   // <-- এই লাইনটা যোগ করুন

    // Lists all complaints for the admin console.
    // Search, category/status/priority filtering, and pagination (5 per page)
    // are all handled on the frontend with JS against this full list.
    @GetMapping("/complaints")
    public String manageComplaints(Model model,Authentication authentication) {

        List<Complaint> complaints = complaintRepository.findAllByOrderByCreatedAtDesc();

        long totalActive = complaints.stream()
                .filter(c -> c.getStatus() != Status.RESOLVED)
                .count();

        long highPriority = complaints.stream()
                .filter(c -> c.getPriority() == Priority.HIGH || c.getPriority() == Priority.CRITICAL)
                .count();
        Admin admin = adminRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        model.addAttribute("activePage", "manage-complaints");
        model.addAttribute("complaints", complaints);
        model.addAttribute("totalActive", totalActive);
        model.addAttribute("highPriority", highPriority);
        model.addAttribute("admin", admin);

        return "admin/complaints";
    }

    // Shows full complaint details, including the attached image.
    // Status/Priority are editable right here via a form on this same page.
    @GetMapping("/complaints/{id}/view")
    public String viewComplaint(@PathVariable Long id, Model model) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with id: " + id));

        model.addAttribute("complaint", complaint);

        return "admin/viewComplaint";
    }

    // Saves the updated status/priority submitted from the view page.
    // Status is what drives the citizen-facing "My Complaints" page,
    // so this is the single source of truth for progress.
    @PostMapping("/complaints/{id}/edit")
    public String updateComplaint(@PathVariable Long id,
                                  @RequestParam Status status,
                                  @RequestParam Priority priority) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with id: " + id));

        complaint.setStatus(status);
        complaint.setPriority(priority);

        complaintRepository.save(complaint);

        return "redirect:/admin/complaints/" + id + "/view";
    }

    // Shows the confirmation page before deleting a complaint
    @GetMapping("/complaints/{id}/delete")
    public String confirmDeleteComplaint(@PathVariable Long id, Model model) {

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with id: " + id));

        model.addAttribute("complaint", complaint);

        return "admin/deleteComplaint";
    }

    // Actually deletes the complaint after confirmation
    @PostMapping("/complaints/{id}/delete")
    public String deleteComplaint(@PathVariable Long id) {

        complaintRepository.deleteById(id);

        return "redirect:/admin/complaints";
    }

    @GetMapping("/users")
    public String manageUsers(Model model,Authentication authentication) {

        // Frontend handles search, district filter, and pagination (5 per page).
        List<Citizen> citizens = citizenRepository.findAllByOrderByCreatedAtDesc();

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        long newThisMonth = citizenRepository.countByCreatedAtAfter(startOfMonth);

        // Header e logged-in admin er naam/chobi dekhanor jonno
        Admin admin = adminRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        model.addAttribute("activePage", "manage-users");
        model.addAttribute("citizens", citizens);
        model.addAttribute("totalRegistered", citizens.size());
        model.addAttribute("newThisMonth", newThisMonth);
        model.addAttribute("admin", admin);

        return "admin/users";
    }

    // Shows the confirmation page with the citizen's details before deleting
    @GetMapping("/users/{id}/delete")
    public String confirmDelete(@PathVariable Long id, Model model) {

        Citizen citizen = citizenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found with id: " + id));

        model.addAttribute("citizen", citizen);

        return "admin/deleteUser";
    }

    // Actually performs the delete after the user confirms on the page above
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {

        citizenRepository.deleteById(id);

        return "redirect:/admin/users";
    }

    // Shows the edit form pre-filled with the citizen's current details
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {

        Citizen citizen = citizenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found with id: " + id));

        // Prevent Thymeleaf from throwing when a citizen has no address yet
        if (citizen.getAddress() == null) {
            citizen.setAddress(new CitizenAddress());
        }

        model.addAttribute("citizen", citizen);

        return "admin/editUser";
    }

    // Saves the updated citizen details submitted from the edit form
    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute Citizen updatedCitizen) {

        Citizen citizen = citizenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found with id: " + id));

        citizen.setName(updatedCitizen.getName());
        citizen.setEmail(updatedCitizen.getEmail());
        citizen.setPhone(updatedCitizen.getPhone());

        if (updatedCitizen.getAddress() != null) {
            if (citizen.getAddress() == null) {
                citizen.setAddress(new CitizenAddress());
            }
            citizen.getAddress().setCity(updatedCitizen.getAddress().getCity());
        }

        citizenRepository.save(citizen);

        return "redirect:/admin/users";
    }
}