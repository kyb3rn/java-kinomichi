package app.utils.tarification;

import app.models.Person;

public enum ChargeableElementType {

    PERSON(Person.class);

    // ─── Properties ─── //

    private final Class<? extends ChargeableElement> chargeableElementClass;

    // ─── Constructors ─── //

    ChargeableElementType(Class<? extends ChargeableElement> chargeableElementClass) {
        this.chargeableElementClass = chargeableElementClass;
    }

    // ─── Getters ─── //

    public Class<? extends ChargeableElement> getChargeableElementClass() {
        return this.chargeableElementClass;
    }

}
