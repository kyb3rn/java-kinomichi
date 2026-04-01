package app.utils.tarification;

public interface ChargingElement {

    double getBasePrice();

    default double getUndiscountedPrice() {
        return this.getBasePrice();
    }

}
