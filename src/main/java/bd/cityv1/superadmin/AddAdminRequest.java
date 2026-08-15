package bd.cityv1.superadmin;

import bd.cityv1.superadmin.AdminPosition;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Add-admin form ekhon eita bind kore, Admin entity-ke shorashori na —
// controller-e explicit vabe Admin banano hoy eta theke.
// (Form-e password/confirmPassword thake, kintu Admin entity-te
// confirmPassword er kono field nai — DTO na thakle eita jorai lagto.)
//
// Shudhu creation-er jonno joruri fields eikhane — phone/department
// intentionally rakha hoyni, notun admin nijer profile page theke
// (Admin Profile -> Edit) pore nijei set korte parবে.
@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class AddAdminRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm the password")
    private String confirmPassword;

    @NotNull(message = "Please select a position")
    private AdminPosition position;
}