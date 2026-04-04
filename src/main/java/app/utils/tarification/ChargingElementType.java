package app.utils.tarification;

import app.models.Dinner;

public enum ChargingElementType {

    DINNER(Dinner.class);

    // ─── Properties ─── //

    private final Class<? extends ChargingElement> chargingElementClass;

    // ─── Constructors ─── //

    ChargingElementType(Class<? extends ChargingElement> chargingElementClass) {
        this.chargingElementClass = chargingElementClass;
    }

    // ─── Getters ─── //

    public Class<? extends ChargingElement> getChargingElementClass() {
        return this.chargingElementClass;
    }

}
