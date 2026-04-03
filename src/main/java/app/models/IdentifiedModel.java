package app.models;

public abstract class IdentifiedModel extends Model {

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

}
