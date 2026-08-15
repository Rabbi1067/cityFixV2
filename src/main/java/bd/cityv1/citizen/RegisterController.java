package bd.cityv1.citizen;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final CitizenService citizenService;

    @PostMapping("/register/save")
    public String register(@Valid @ModelAttribute("citizen") Citizen citizen,
                           BindingResult result,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        if (!citizen.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }

        if (citizenService.isEmailTaken(citizen.getEmail())) {
            model.addAttribute("error", "Email already registered");
            return "register";
        }

        if (citizenService.isPhoneTaken(citizen.getPhone())) {
            model.addAttribute("error", "Phone number already registered");
            return "register";
        }

        citizenService.registerCitizen(citizen);

        return "redirect:/login";
    }
}