package bd.cityv1.Controller;

import bd.cityv1.admin.Admin;
import bd.cityv1.admin.AdminRepository;
import bd.cityv1.citizen.CitizenRepository;
import bd.cityv1.complaint.Complaint;
import bd.cityv1.complaint.ComplaintRepository;
import bd.cityv1.complaint.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminRepository adminRepository;
    private final CitizenRepository citizenRepository;
    private final ComplaintRepository complaintRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        Admin admin = adminRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        List<Complaint> allComplaints = complaintRepository.findAllByOrderByCreatedAtDesc();

        // ---- Top stat cards ----
        long totalCitizens = citizenRepository.count();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long todaysReports = allComplaints.stream()
                .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(startOfToday))
                .count();

        long resolvedCount = allComplaints.stream()
                .filter(c -> c.getStatus() == Status.RESOLVED)
                .count();
        long totalComplaints = allComplaints.size();
        double resolutionRate = totalComplaints == 0 ? 0 : (resolvedCount * 100.0 / totalComplaints);

        long pendingReview = allComplaints.stream()
                .filter(c -> c.getStatus() != Status.RESOLVED)
                .count();

        // ---- Resolution Trends (last 6 months: reported vs resolved) ----
        List<String> trendLabels = new ArrayList<>();
        List<Long> trendReported = new ArrayList<>();
        List<Long> trendResolved = new ArrayList<>();

        YearMonth current = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            trendLabels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

            long reportedInMonth = allComplaints.stream()
                    .filter(c -> c.getCreatedAt() != null && YearMonth.from(c.getCreatedAt()).equals(ym))
                    .count();
            long resolvedInMonth = allComplaints.stream()
                    .filter(c -> c.getCreatedAt() != null && YearMonth.from(c.getCreatedAt()).equals(ym)
                            && c.getStatus() == Status.RESOLVED)
                    .count();

            trendReported.add(reportedInMonth);
            trendResolved.add(resolvedInMonth);
        }

        // ---- Category Split (group by category, count) ----
        Map<String, Long> categoryCounts = allComplaints.stream()
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        List<String> categoryLabels = new ArrayList<>(categoryCounts.keySet());
        List<Long> categoryValues = categoryLabels.stream()
                .map(categoryCounts::get)
                .collect(Collectors.toList());

        // ---- Recent Urgent Complaints (HIGH/CRITICAL, latest 3) ----
        List<Complaint> urgentComplaints = allComplaints.stream()
                .filter(c -> c.getPriority() != null &&
                        (c.getPriority().name().equals("HIGH") || c.getPriority().name().equals("CRITICAL")))
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("admin", admin);

        model.addAttribute("totalCitizens", totalCitizens);
        model.addAttribute("todaysReports", todaysReports);
        model.addAttribute("resolutionRate", Math.round(resolutionRate * 10) / 10.0);
        model.addAttribute("pendingReview", pendingReview);

        model.addAttribute("trendLabels", trendLabels);
        model.addAttribute("trendReported", trendReported);
        model.addAttribute("trendResolved", trendResolved);

        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryValues", categoryValues);
        model.addAttribute("categoryTotal", totalComplaints);

        model.addAttribute("urgentComplaints", urgentComplaints);

        return "admin/adminDashboard";
    }
}

//    @GetMapping("/complaints")
//    public String manageComplaints(Model model) {
//        model.addAttribute("activePage", "manage-complaints");
//        return "admin/complaints";
//    }

//    @GetMapping("/users")
//    public String manageUsers(Model model) {
//        model.addAttribute("activePage", "manage-users");
//        return "admin/users";
//    }

//    @GetMapping("/profile")
//    public String profile(Model model) {
//        model.addAttribute("activePage", "profile");
//        return "admin/profile";
//    }
