package bd.cityv1.adminProfile;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAdminPersonalInfoDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Department is required")
    private String department;
}