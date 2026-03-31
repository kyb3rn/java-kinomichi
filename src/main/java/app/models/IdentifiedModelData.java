package app.models;

public abstract class IdentifiedModelData {

    // ─── Properties ─── //

    private int id = -1;

    // ─── Getters ─── //

    public int getId() {
        return this.id;
    }

    // ─── Setters ─── //

    public void setId(int id) throws ModelException {
        if (id <= 0 && id != -1) {
            throw new ModelException("L'identifiant doit être un entier strictement positif (ou -1)");
        }

        this.id = id;
    }

    public void setId(String id) throws ModelException {
        int idAsInt;
        try {
            idAsInt = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new ModelException("L'identifiant doit être un entier strictement positif (ou -1)", e);
        }

        this.setId(idAsInt);
    }

}
