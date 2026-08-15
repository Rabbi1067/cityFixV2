package bd.cityv1.profile;

import bd.cityv1.citizen.Citizen;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/citizen")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        Citizen citizen = profileService.getCitizen(principal.getName());
        model.addAttribute("citizen", citizen);
        model.addAttribute("activePage", "profile");
        return "citizen/profile";
    }

    @PostMapping("/profile/update-personal")
    @ResponseBody
    public ResponseEntity<?> updatePersonal(@Valid @RequestBody UpdatePersonalInfoDto dto,
                                            BindingResult result,
                                            Principal principal) {
        if (result.hasErrors()) {
            String errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of("error", errors));
        }

        try {
            Citizen updated = profileService.updatePersonalInfo(principal.getName(), dto);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Profile updated successfully!",
                    "name", updated.getName(),
                    "city", updated.getAddress().getCity()
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
            profileService.changePassword(principal.getName(), dto);
            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(@RequestParam("image") MultipartFile image, Principal principal) {
        try {
            Citizen updated = profileService.updateAvatar(principal.getName(), image);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Profile photo updated!",
                    "imagePath", updated.getProfileImagePath()
            ));
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}