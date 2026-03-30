package utils.io.menus;

public class MenuLeadTo {

    // ─── Properties ─── //

    private final String leadTo;

    // ─── Constructors ─── //

    public MenuLeadTo() {
        this.leadTo = null;
    }

    public MenuLeadTo(MenuLeadTo menuLeadTo) {
        this.leadTo = menuLeadTo == null ? null : menuLeadTo.getLeadTo();
    }

    public MenuLeadTo(String leadTo) {
        if (leadTo != null) {
            if (leadTo.isBlank()) {
                throw new IllegalArgumentException("Le paramètre leadTo ne peut pas être vide");
            } else {
                leadTo = leadTo.trim();
            }
        }

        this.leadTo = leadTo;
    }

    // ─── Getters ─── //

    public String getLeadTo() {
        return this.leadTo;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        return this.leadTo;
    }

}
