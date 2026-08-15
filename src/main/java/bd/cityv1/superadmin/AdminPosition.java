package bd.cityv1.superadmin;

// Admin toiri korar shomoy "kon category" select korte hoy —
// enum use kora hoyeche jate shudhu fixed, valid value-i thakte pare
// (free-text department field theke alada — eita structured category).
public enum AdminPosition {
    GENERAL_ADMIN("General Admin"),
    COMPLAINT_MANAGER("Complaint Manager"),
    USER_MANAGER("User Manager"),
    IT_ADMIN("IT & Technical"),
    PUBLIC_RELATIONS("Public Relations");

    private final String label;

    AdminPosition(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}