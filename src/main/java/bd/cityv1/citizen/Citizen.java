package bd.cityv1.citizen;

import bd.cityv1.complaint.Complaint;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "citizens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String citizenId;

    @NotBlank
    private String name;

    @Column(unique = true, nullable = false)
    @NotBlank
    private String phone;

    @Email
    @NotBlank
    private String email;

    private String nationalId;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String occupation;

    @Embedded
    private CitizenAddress address;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String profileImagePath;

    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL)
    private List<Complaint> complaints = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}