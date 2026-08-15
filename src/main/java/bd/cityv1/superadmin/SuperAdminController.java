package bd.cityv1.superadmin;

import bd.cityv1.admin.Admin;
import bd.cityv1.admin.AdminRepository;
import bd.cityv1.superadmin.AddAdminRequest;
import bd.cityv1.superadmin.AdminPosition;
import bd.cityv1.superadmin.AdminStats;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Route-level security already /admin/admins/** -> hasRole("SUPER_ADMIN")
// diye CitizenSecurityConfig-e protect kora — tai eikhane extra check lagbe na.
@Controller
@RequestMapping("/admin/admins")
@RequiredArgsConstructor
public class SuperAdminController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- List + search (search client-side-o kaj kore, ei query param diye
    // server-side-o filter kora jay — page reload diye search korte chaile) ----
    @GetMapping
    public String listAdmins(@RequestParam(value = "query", required = false) String query,
                             Model model, Authentication authentication) {

        Admin currentAdmin = currentAdmin(authentication);

        List<Admin> admins = (query == null || query.isBlank())
                ? adminRepository.findAll()
                : adminRepository.searchByNameOrEmail(query.trim());

        long total = adminRepository.count();
        long active = adminRepository.findAll().stream().filter(Admin::isEnabled).count();
        AdminStats stats = new AdminStats(total, active, total - active);

        model.addAttribute("activePage", "manage-admins");
        model.addAttribute("admin", currentAdmin);
        model.addAttribute("admins", admins);
        model.addAttribute("stats", stats);
        model.addAttribute("currentAdminId", currentAdmin.getId());
        model.addAttribute("query", query);
        model.addAttribute("positions", AdminPosition.values());

        return "admin/admins";
    }

    // ---- Add-admin form ----
    @GetMapping("/add")
    public String addAdminForm(Model model, Authentication authentication) {
        model.addAttribute("activePage", "manage-admins");
        model.addAttribute("admin", currentAdmin(authentication));
        model.addAttribute("addAdminRequest", new AddAdminRequest());
        model.addAttribute("positions", AdminPosition.values());
        return "admin/addAdmin";
    }

    // ---- Save new admin: email, password, position — always role ADMIN, never SUPER_ADMIN ----
    @PostMapping("/add")
    public String saveAdmin(@Valid @ModelAttribute("addAdminRequest") AddAdminRequest request,
                            BindingResult result,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("admin", currentAdmin(authentication));
            model.addAttribute("positions", AdminPosition.values());
            return "admin/addAdmin";
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("admin", currentAdmin(authentication));
            model.addAttribute("positions", AdminPosition.values());
            return "admin/addAdmin";
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            model.addAttribute("error", "This email is already registered");
            model.addAttribute("admin", currentAdmin(authentication));
            model.addAttribute("positions", AdminPosition.values());
            return "admin/addAdmin";
        }

        Admin admin = new Admin();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPosition(request.getPosition());
        admin.setRoles(new ArrayList<>(List.of("ADMIN"))); // normal admin — never SUPER_ADMIN via this form
        admin.setEnabled(true);

        adminRepository.save(admin);

        redirectAttributes.addFlashAttribute("success", "New admin added successfully!");
        return "redirect:/admin/admins";
    }

    // ---- Block permanently ----
    @PostMapping("/{id}/block")
    public String blockPermanently(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        return applyBlock(id, authentication, redirectAttributes, null);
    }

    // ---- Block temporarily (N days) — auto-unblocks itself on next login attempt after expiry ----
    @PostMapping("/{id}/block-temp")
    public String blockTemporarily(@PathVariable Long id,
                                   @RequestParam("days") int days,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {

        if (days < 1) {
            redirectAttributes.addFlashAttribute("error", "Enter at least 1 day for a temporary block.");
            return "redirect:/admin/admins";
        }

        return applyBlock(id, authentication, redirectAttributes, LocalDateTime.now().plusDays(days));
    }

    // ---- Unblock ----
    @PostMapping("/{id}/unblock")
    public String unblock(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {

        Admin target = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));

        target.setEnabled(true);
        target.setBlockedUntil(null);
        adminRepository.save(target);

        redirectAttributes.addFlashAttribute("success", "Admin unblocked.");
        return "redirect:/admin/admins";
    }

    // ---- Delete admin ----
    @PostMapping("/{id}/delete")
    public String deleteAdmin(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {

        Admin target = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));
        Admin currentAdmin = currentAdmin(authentication);

        if (target.getId().equals(currentAdmin.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can't delete your own account.");
            return "redirect:/admin/admins";
        }

        // Last SUPER_ADMIN ke delete korte dewa jabe na — system lockout theke bachanor jonno.
        boolean isTargetSuperAdmin = target.getRoles().contains("SUPER_ADMIN");
        long superAdminCount = adminRepository.findAll().stream()
                .filter(a -> a.getRoles().contains("SUPER_ADMIN"))
                .count();

        if (isTargetSuperAdmin && superAdminCount <= 1) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete the last SUPER_ADMIN.");
            return "redirect:/admin/admins";
        }

        adminRepository.delete(target);

        redirectAttributes.addFlashAttribute("success", "Admin deleted.");
        return "redirect:/admin/admins";
    }

    // ---- Shared block logic (permanent when blockedUntil == null, temp otherwise) ----
    private String applyBlock(Long id, Authentication authentication, RedirectAttributes redirectAttributes, LocalDateTime blockedUntil) {

        Admin target = adminRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));
        Admin currentAdmin = currentAdmin(authentication);

        if (target.getId().equals(currentAdmin.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can't block your own account.");
            return "redirect:/admin/admins";
        }

        target.setEnabled(false);
        target.setBlockedUntil(blockedUntil);
        adminRepository.save(target);

        redirectAttributes.addFlashAttribute("success",
                blockedUntil == null ? "Admin blocked permanently." : "Admin blocked until " + blockedUntil.toLocalDate() + ".");
        return "redirect:/admin/admins";
    }

    private Admin currentAdmin(Authentication authentication) {
        return adminRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
    }
}