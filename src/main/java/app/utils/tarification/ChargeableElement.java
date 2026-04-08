package app.utils.tarification;

import java.util.Set;

public interface ChargeableElement {

    Set<EChargeableCategory> getChargeableCategories(ChargingElement chargingElement) throws Exception;

}
