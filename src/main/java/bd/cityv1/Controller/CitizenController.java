package bd.cityv1.Controller;

import bd.cityv1.citizen.Citizen;
import bd.cityv1.citizen.CitizenRepository;
import bd.cityv1.complaint.Complaint;
import bd.cityv1.complaint.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/citizen")
@RequiredArgsConstructor
public class CitizenController {

    private final CitizenRepository citizenRepository;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        Citizen citizen = citizenRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Logged-in citizen not found for email: " + authentication.getName()));

        List<Complaint> complaints = citizen.getComplaints() != null ? citizen.getComplaints() : Collections.emptyList();

        model.addAttribute("activePage", "citizen-dashboard");
        model.addAttribute("citizen", citizen);
        model.addAllAttributes(buildStatusCounts(complaints));
        model.addAllAttributes(buildTrend(complaints));
        model.addAllAttributes(buildCategorySplit(complaints));
        model.addAttribute("recentActivities", buildRecentActivities(complaints));

        return "citizen/citizenDashboard";
    }

    // ---- Total / Pending / Working / Resolved (single pass) ----
    private Map<String, Object> buildStatusCounts(List<Complaint> complaints) {
        long pending = 0, working = 0, resolved = 0;
        for (Complaint c : complaints) {
            if (c.getStatus() == Status.PENDING) pending++;
            else if (c.getStatus() == Status.IN_PROGRESS) working++;
            else if (c.getStatus() == Status.RESOLVED) resolved++;
        }
        return Map.of(
                "totalCount", (long) complaints.size(),
                "pendingCount", pending,
                "workingCount", working,
                "resolvedCount", resolved
        );
    }

    // ---- Activity Trend: last 6 months, complaints filed by this citizen (single pass) ----
    private Map<String, Object> buildTrend(List<Complaint> complaints) {
        Map<YearMonth, Long> monthlyCounts = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            monthlyCounts.put(current.minusMonths(i), 0L);
        }
        for (Complaint c : complaints) {
            if (c.getCreatedAt() == null) continue;
            YearMonth ym = YearMonth.from(c.getCreatedAt());
            monthlyCounts.computeIfPresent(ym, (k, v) -> v + 1);
        }

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        monthlyCounts.forEach((ym, count) -> {
            labels.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            values.add(count);
        });

        return Map.of("trendLabels", labels, "trendValues", values);
    }

    // ---- Category distribution, used to drive the Hotspot Map (no lat/lng stored) ----
    private Map<String, Object> buildCategorySplit(List<Complaint> complaints) {
        Map<String, Long> counts = complaints.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        List<String> labels = new ArrayList<>(counts.keySet());
        List<Long> values = labels.stream().map(counts::get).collect(Collectors.toList());

        return Map.of("categoryLabels", labels, "categoryValues", values);
    }

    // ---- Latest 5 complaints, formatted for the Recent Activity timeline ----
    private List<Map<String, String>> buildRecentActivities(List<Complaint> complaints) {
        return complaints.stream()
                .sorted(Comparator.comparing(Complaint::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(c -> Map.of(
                        "title", c.getTitle() != null ? c.getTitle() : "Untitled Report",
                        "detail", (c.getCategory() != null ? c.getCategory() : "General") + " • "
                                + (c.getLocation() != null ? c.getLocation() : "Unknown location"),
                        "time", formatRelativeTime(c.getCreatedAt()),
                        "color", c.getStatus() == Status.RESOLVED ? "bg-secondary"
                                : c.getStatus() == Status.IN_PROGRESS ? "bg-primary" : "bg-outline"
                ))
                .collect(Collectors.toList());
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        long minutes = Math.max(Duration.between(dateTime, LocalDateTime.now()).toMinutes(), 0);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 7) return days + (days == 1 ? " day ago" : " days ago");
        long weeks = days / 7;
        if (weeks < 5) return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        long months = days / 30;
        if (months < 12) return months + (months == 1 ? " month ago" : " months ago");
        long years = days / 365;
        return years + (years == 1 ? " year ago" : " years ago");
    }
}