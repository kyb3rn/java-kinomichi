package app.models;

import app.utils.elements.time.TimeSlot;

import java.time.Instant;

public interface CampScheduledItem {

    TimeSlot getTimeSlot();

    Camp getCamp();

    default void validateTimeSlotWithinCampBounds() throws ModelVerificationException {
        CampScheduledItem.validateTimeSlotWithinCampBounds(this.getCamp(), this.getTimeSlot());
    }

    static void validateTimeSlotWithinCampBounds(Camp camp, TimeSlot itemTimeSlot) throws ModelVerificationException {
        if (camp == null || camp.getTimeSlot() == null || itemTimeSlot == null) {
            return;
        }

        if (!camp.getTimeSlot().contains(itemTimeSlot)) {
            throw new ModelVerificationException("Le créneau horaire doit être entièrement inclus dans celui du stage (%s)".formatted(camp.getTimeSlot().toPrettyStringFormat()));
        }
    }

    static void validateInstantWithinCampBounds(Camp camp, Instant instant, String instantLabel) throws ModelVerificationException {
        if (camp == null || camp.getTimeSlot() == null || instant == null) {
            return;
        }

        TimeSlot campTimeSlot = camp.getTimeSlot();

        if (instant.isBefore(campTimeSlot.getStart()) || instant.isAfter(campTimeSlot.getEnd())) {
            throw new ModelVerificationException("%s doit être comprise dans la période du stage (%s)".formatted(instantLabel, campTimeSlot.toPrettyStringFormat()));
        }
    }

}
