package app.utils.tarification;

import app.utils.elements.time.TimeSlot;

public interface ChargeableElement {

    EChargeableCategory getChargeableCategory(TimeSlot timeSlot);

}
