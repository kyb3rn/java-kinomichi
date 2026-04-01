package app.utils.tarification;

import java.time.Duration;

public interface DurationBasedChargingElement extends ChargingElement {
    
    Duration getDuration();
    
    @Override
    default double getUndiscountedPrice() {
        double hours = this.getDuration().toNanos() / (double) Duration.ofHours(1).toNanos();
        return this.getBasePrice() * hours;
    }
    
}
