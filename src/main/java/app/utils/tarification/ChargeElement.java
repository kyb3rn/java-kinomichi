package app.utils.tarification;

import app.models.Camp;
import app.models.CampDiscount;
import app.models.managers.CampDiscountDataManager;
import app.models.managers.DataManagerException;
import app.models.ModelException;
import app.models.managers.DataManagers;

import java.util.Optional;
import java.util.Set;

public class ChargeElement {

    // ─── Properties ─── //

    private final ChargeableElement chargeableElement;
    private final ChargingElement chargingElement;
    private final Camp camp;

    // ─── Constructors ─── //

    public ChargeElement(ChargeableElement chargeableElement, ChargingElement chargingElement, Camp camp) {
        this.chargeableElement = chargeableElement;
        this.chargingElement = chargingElement;
        this.camp = camp;
    }

    // ─── Getters ─── //

    public ChargeableElement getChargeableElement() {
        return this.chargeableElement;
    }

    public ChargingElement getChargingElement() {
        return this.chargingElement;
    }

    public Camp getCamp() {
        return this.camp;
    }

    // ─── Utility methods ─── //

    public double getUndiscountedPrice() {
        return this.chargingElement.getUndiscountedPrice();
    }

    public double getBestDiscountPercentage() throws TarificationException {
        ChargingElementType chargingElementType = resolveChargingElementType(this.chargingElement);
        ChargeableElementType chargeableElementType = resolveChargeableElementType(this.chargeableElement);

        CampDiscountDataManager campDiscountDataManager;
        try {
            campDiscountDataManager = DataManagers.get(CampDiscountDataManager.class);
        } catch (DataManagerException | ModelException e) {
            throw new TarificationException("Impossible de charger les réductions de stage", e);
        }

        Optional<CampDiscount> optionalCampDiscount = campDiscountDataManager.getCampDiscount(
                this.camp.getId(), chargeableElementType, chargingElementType
        );

        if (optionalCampDiscount.isEmpty()) {
            return 0;
        }

        CampDiscount campDiscount = optionalCampDiscount.get();

        Set<EChargeableCategory> categories;
        try {
            categories = this.chargeableElement.getChargeableCategories(this.chargingElement);
        } catch (Exception e) {
            throw new TarificationException("Impossible de déterminer les catégories facturables de l'élément", e);
        }

        if (!categories.contains(campDiscount.getChargeableCategory())) {
            return 0;
        }

        return campDiscount.getDiscountPercentage();
    }

    public double getDiscountedPrice() throws TarificationException {
        double undiscountedPrice = this.getUndiscountedPrice();
        double bestDiscountPercentage = this.getBestDiscountPercentage();
        return undiscountedPrice * (1 - bestDiscountPercentage);
    }

    private static ChargingElementType resolveChargingElementType(ChargingElement chargingElement) throws TarificationException {
        for (ChargingElementType type : ChargingElementType.values()) {
            if (type.getChargingElementClass().isInstance(chargingElement)) {
                return type;
            }
        }

        throw new TarificationException("Aucun type d'élément facturant ne correspond à la classe '%s'".formatted(chargingElement.getClass().getSimpleName()));
    }

    private static ChargeableElementType resolveChargeableElementType(ChargeableElement chargeableElement) throws TarificationException {
        for (ChargeableElementType type : ChargeableElementType.values()) {
            if (type.getChargeableElementClass().isInstance(chargeableElement)) {
                return type;
            }
        }

        throw new TarificationException("Aucun type d'élément facturable ne correspond à la classe '%s'".formatted(chargeableElement.getClass().getSimpleName()));
    }

}
