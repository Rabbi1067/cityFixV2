package bd.cityv1.adminProfile;

import bd.cityv1.admin.Admin;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        Admin admin = adminProfileService.getAdmin(principal.getName());
        model.addAttribute("admin", admin);
        model.addAttribute("activePage", "profile");
        return "admin/profile";
    }

    @PostMapping("/profile/update-personal")
    @ResponseBody
    public ResponseEntity<?> updatePersonal(@Valid @RequestBody UpdateAdminPersonalInfoDto dto,
                                            BindingResult result,
                                            Principal principal) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("error", errors));
        }

        try {
            Admin updated = adminProfileService.updatePersonalInfo(principal.getName(), dto);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Profile updated successfully!",
                    "name", updated.getName()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto dto,
                                            BindingResult result,
                                            Principal principal) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("error", errors));
        }

        try {
            adminProfileService.changePassword(principal.getName(), dto);
            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}