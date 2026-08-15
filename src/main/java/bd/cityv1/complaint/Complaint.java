package bd.cityv1.complaint;

import bd.cityv1.citizen.Citizen;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Priority level is required")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @NotBlank(message = "Location is required")
    private String location;

    private String imagePath;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING; // default, admin ei field change korbe

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = true)
    private Citizen citizen;

    private LocalDateTime createdAt = LocalDateTime.now();
}