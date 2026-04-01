package app.utils.tarification;

public class ChargeElement {

    private ChargeableElement chargeableElement;
    private ChargingElement chargingElement;

    public double getDiscountedPrice() {
        return this.chargingElement.getUndiscountedPrice();
    }

}
