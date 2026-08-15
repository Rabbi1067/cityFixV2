package bd.cityv1.superadmin;

// Read-only data carrier — record use kora hoyeche karon eita shudhu
// data hold kore, kono mutation/behavior lagbe na. Thymeleaf-e
// ${stats.total}, ${stats.active}, ${stats.blocked} diye porte pare,
// karon SpEL record component-ke property-r moto e access kore.
public record AdminStats(long total, long active, long blocked) {
}