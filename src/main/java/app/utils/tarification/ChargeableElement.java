package app.utils.tarification;

import java.util.Set;

public interface ChargeableElement {

    Set<EChargeableCategory> getChargeableCategory(ChargingElement chargingElement) throws Exception;

}
