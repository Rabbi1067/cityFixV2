package bd.cityv1.admin;

import bd.cityv1.superadmin.AdminPosition;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Column(unique = true, nullable = false)
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // JPA relation: contact info grouped into its own @Embeddable value object,
    // flattened into the same "admins" table (same column names as before,
    // so no DB migration needed).
    @Embedded
    private AdminContactInfo contactInfo = new AdminContactInfo();

    private String profileImagePath;

    // Admin category — kon dayitte ache (dropdown theke select kora hoy Add Admin form-e).
    @Enumerated(EnumType.STRING)
    private AdminPosition position = AdminPosition.GENERAL_ADMIN;

    // "SUPER_ADMIN" -> full access (e.g. can manage other admins).
    // "ADMIN"       -> normal admin access.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "admin_roles", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    // false hole login korte parbe na — SUPER_ADMIN ei flag toggle kore
    // kono admin ke block/unblock korte pare (delete na kore).
    @Column(nullable = false)
    private boolean enabled = true;

    // null = permanent block (jokhon enabled=false)
    // set thakle = temp block, oi shomoy porjonto — CustomAuthenticationProvider
    // eita check kore, shomoy pero gele nijei abar enable kore dey.
    private LocalDateTime blockedUntil;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Hibernate quirk: jodi @Embedded object-er SHOB field (phone, department)
    // DB-te NULL thake, Hibernate contactInfo-take-i null kore dey (empty
    // object banায় na)। Row load howar por eita check kore null hole
    // notun empty AdminContactInfo boshiye dey — tai kono jaygay
    // admin.getContactInfo().getPhone() call korলে r NullPointerException hobe na.
    @PostLoad
    private void ensureContactInfoNotNull() {
        if (contactInfo == null) {
            contactInfo = new AdminContactInfo();
        }
    }
}