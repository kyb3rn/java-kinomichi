package app.models;

public abstract class IdentifiedModelData {

    // ─── Properties ─── //

    private int id = -1;

    // ─── Getters ─── //

    public int getId() {
        return this.id;
    }

    // ─── Setters ─── //

    public void setId(String id) throws ModelException {
        this.id = IdentifiedModel.verifyId(id);
    }

    public void setId(int id) throws ModelException {
        this.setId(String.valueOf(id));
    }

}
