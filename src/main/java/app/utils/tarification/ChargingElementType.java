package app.utils.tarification;

import app.models.DinnerReservation;
import app.models.LodgingReservation;

public enum ChargingElementType {

    DINNER(DinnerReservation.class),
    LODGING(LodgingReservation .class);

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
