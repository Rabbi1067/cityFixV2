package bd.cityv1.admin;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Value object — nijer kono identity/id nai, Admin-er shathe always bound thake.
// @Embedded diye Admin entity-r vitore "flatten" hoye jabe (same DB columns:
// phone, department — kono migration lagbe na).
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminContactInfo {
    private String phone;
    private String department;
}