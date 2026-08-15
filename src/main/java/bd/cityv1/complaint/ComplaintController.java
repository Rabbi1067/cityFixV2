package bd.cityv1.complaint;

import bd.cityv1.citizen.Citizen;
import bd.cityv1.citizen.CitizenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/citizen")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final CitizenRepository citizenRepository;

    private final String UPLOAD_DIR = "uploads/";

    @GetMapping("/my-complaints")
    public String myComplaints(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) Status status,
                               @RequestParam(defaultValue = "0") int page,
                               Authentication authentication,
                               Model model) {

        // Shudhu logged-in citizen-er nijer complaint dekhano hocche,
        // onno kono citizen-er complaint na
        Citizen citizen = citizenRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Logged-in citizen not found for email: " + authentication.getName()));

        Page<Complaint> result = complaintService.searchComplaints(citizen.getId(), keyword, category, status, page);

        model.addAttribute("activePage", "my-complaints");
        model.addAttribute("citizen", citizen);   // <-- এই line-টা add করো

        model.addAttribute("complaints", result.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("status", status);
        model.addAttribute("activeCount", complaintService.countActive(citizen.getId()));
        model.addAttribute("resolvedCount", complaintService.countResolved(citizen.getId()));

        return "citizen/myComplains";
    }

    // Read-only details page. Citizen can only view their OWN complaint —
    // status/priority editing shudhu admin er kaj, tai kono edit form nai ekhane.
    @GetMapping("/complaints/{id}/view")
    public String viewComplaint(@PathVariable Long id, Authentication authentication, Model model) {

        Citizen citizen = citizenRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Logged-in citizen not found for email: " + authentication.getName()));

        Complaint complaint = complaintService.getComplaintById(id);

        // Onno kono citizen-er complaint URL diye direct access korte parbe na
        if (complaint.getCitizen() == null || !complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new AccessDeniedException("You are not allowed to view this complaint.");
        }

        model.addAttribute("activePage", "my-complaints");
        model.addAttribute("complaint", complaint);

        return "citizen/viewComplaint";
    }

    @GetMapping("/create-complaint")
    public String createComplaintForm(Authentication authentication, Model model) {

        Citizen citizen = citizenRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Logged-in citizen not found for email: " + authentication.getName()));

        model.addAttribute("activePage", "create-complaint");
        model.addAttribute("citizen", citizen);
        model.addAttribute("complaint", new Complaint());

        return "citizen/createComplaints";
    }

    @PostMapping("/complaints/save")
    public String saveComplaint(@Valid @ModelAttribute("complaint") Complaint complaint,
                                BindingResult result,
                                @RequestParam("image") MultipartFile file,
                                Authentication authentication) throws IOException {

        if (result.hasErrors()) {
            return "citizen/createComplaints";
        }

        // Logged-in citizen ke complaint er sathe attach kora hocche.
        Citizen citizen = citizenRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Logged-in citizen not found for email: " + authentication.getName()));
        complaint.setCitizen(citizen);

        if (file != null && !file.isEmpty()) {
            Files.createDirectories(Path.of(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.write(Path.of(UPLOAD_DIR + fileName), file.getBytes());
            complaint.setImagePath("/" + UPLOAD_DIR + fileName);
        }

        complaintService.saveComplaint(complaint);

        return "redirect:/citizen/create-complaint?success=true";
    }
}