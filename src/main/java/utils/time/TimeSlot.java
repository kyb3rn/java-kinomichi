package utils.time;

import java.time.Duration;
import java.time.Instant;
import java.time.DateTimeException;

public class TimeSlot {

    // ─── Properties ─── //

    private Instant start;
    private Instant end;

    // ─── Constructors ─── //

    public TimeSlot(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Les dates de début et de fin ne peuvent pas être nulles");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
        }

        this.start = start;
        this.end = end;
    }

    public TimeSlot(String timeSlot) {
        String[] split = timeSlot.split(" ");

        if (split.length != 2) {
            throw new IllegalArgumentException("Format de période de temps invalide, attendu: \"yyyy-MM-ddTHH:mm:ssZ yyyy-MM-ddTHH:mm:ssZ\" (ex: \"2026-04-14T09:00:00Z 2026-04-16T18:30:00Z\")");
        }

        this(parseInstant(split[0]), parseInstant(split[1]));
    }

    public TimeSlot(String start, String end) {
        this(parseInstant(start), parseInstant(end));
    }

    // ─── Getters ─── //

    public Instant getStart() {
        return this.start;
    }

    public Instant getEnd() {
        return this.end;
    }

    // ─── Special getters ─── //

    public Duration getDuration() {
        return Duration.between(this.start, this.end);
    }

    public long getDurationInMinutes() {
        return this.getDuration().toMinutes();
    }

    public long getDurationInHours() {
        return this.getDuration().toHours();
    }

    public long getDurationInDays() {
        return this.getDuration().toDays();
    }

    public String getFormattedStart() {
        return this.start.toString();
    }

    public String getFormattedEnd() {
        return this.end.toString();
    }

    // ─── Setters ─── //

    public void setStart(Instant start) {
        if (start == null) {
            throw new IllegalArgumentException("La date de début ne peut pas être nulle");
        }

        if (!this.end.isAfter(start)) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        this.start = start;
    }

    public void setEnd(Instant end) {
        if (end == null) {
            throw new IllegalArgumentException("La date de fin ne peut pas être nulle");
        }

        if (!end.isAfter(this.start)) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
        }

        this.end = end;
    }

    // ─── Utility methods ─── //

    public boolean overlaps(TimeSlot other) {
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    public boolean contains(Instant instant) {
        return !instant.isBefore(this.start) && instant.isBefore(this.end);
    }

    public boolean contains(TimeSlot other) {
        return !other.start.isBefore(this.start) && !other.end.isAfter(this.end);
    }

    public boolean isBefore(TimeSlot other) {
        return !this.end.isAfter(other.start);
    }

    public boolean isAfter(TimeSlot other) {
        return !this.start.isBefore(other.end);
    }

    private static Instant parseInstant(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            throw new IllegalArgumentException("La date ne peut pas être vide");
        }

        try {
            return Instant.parse(dateTime.strip());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Format de date invalide, attendu: yyyy-MM-ddTHH:mm:ssZ (ex: 2026-04-14T09:00:00Z)", e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        return "%s → %s".formatted(this.getFormattedStart(), this.getFormattedEnd());
    }

}
