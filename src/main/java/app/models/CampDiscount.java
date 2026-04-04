package app.models;

import app.models.managers.CampDataManager;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.utils.tarification.ChargeableElement;
import app.utils.tarification.EChargeableCategory;
import app.utils.tarification.ChargeableElementType;
import app.utils.tarification.ChargingElementType;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import utils.data_management.converters.CustomSerializable;
import utils.data_management.converters.Hydratable;
import utils.data_management.converters.convertibles.JsonConvertible;
import utils.data_management.parsing.ParserException;
import utils.data_management.parsing.StringParserException;
import utils.helpers.validation.BlankOrNullValueValidatorException;
import utils.helpers.validation.ParsingValidatorException;
import utils.helpers.validation.Validators;

public class CampDiscount extends IdentifiedModel implements Hydratable<CampDiscount.Data> {

    // ─── Properties ─── //

    @ModelReference(manager = CampDataManager.class) private Camp camp;
    private int pendingCampPk = -1;
    private ChargeableElementType chargeableElementType;
    private EChargeableCategory chargeableCategory;
    private ChargingElementType chargingElementType;
    private double discountPercentage;

    // ─── Getters ─── //

    public Camp getCamp() {
        return this.camp;
    }

    public ChargeableElementType getChargeableElementType() {
        return this.chargeableElementType;
    }

    public EChargeableCategory getChargeableCategory() {
        return this.chargeableCategory;
    }

    public ChargingElementType getChargingElementType() {
        return this.chargingElementType;
    }

    public double getDiscountPercentage() {
        return this.discountPercentage;
    }

    // ─── Special getters ─── //

    public int getCampId() throws ModelException {
        if (this.camp == null) {
            throw new ModelException("Le stage de référence est nul");
        }

        return this.camp.getId();
    }

    // ─── Setters ─── //

    public void setCamp(Camp camp) throws ModelException {
        if (camp == null) {
            throw new ModelVerificationException("Le stage de référence ne peut pas être nul");
        }

        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(camp.getId());
        } catch (NoResultForPrimaryKeyException e) {
            throw new ModelVerificationException(e.getMessage(), e);
        } catch (DataManagerException | ModelException e) {
            throw new ModelVerificationException("Impossible de vérifier l'identifiant du stage '%d'".formatted(camp.getId()), e);
        }
    }

    public void setChargeableElementType(ChargeableElementType chargeableElementType) throws ModelException {
        if (chargeableElementType == null) {
            throw new ModelVerificationException("Le type d'élément facturable ne peut pas être nul");
        }

        this.chargeableElementType = chargeableElementType;
    }

    public void setChargeableCategory(EChargeableCategory chargeableCategory) throws ModelException {
        if (chargeableCategory == null) {
            throw new ModelVerificationException("La catégorie facturable ne peut pas être nulle");
        }

        this.chargeableCategory = chargeableCategory;
    }

    public void setChargingElementType(ChargingElementType chargingElementType) throws ModelException {
        if (chargingElementType == null) {
            throw new ModelVerificationException("Le type d'élément facturant ne peut pas être nul");
        }

        this.chargingElementType = chargingElementType;
    }

    public void setDiscountPercentage(double discountPercentage) throws ModelException {
        this.discountPercentage = verifyDiscountPercentage(String.valueOf(discountPercentage));
    }

    // ─── Special setters ─── //

    public void setCampFromPk(int campId) throws ModelException {
        try {
            this.camp = DataManagers.get(CampDataManager.class).getCampWithExceptions(campId);
        } catch (NoResultForPrimaryKeyException e) {
            throw e;
        } catch (DataManagerException | ModelException e) {
            throw new ModelException("Impossible de vérifier l'identifiant du stage '%d' (les stages n'ont pas pu être chargés dans l'application)".formatted(campId), e);
        }
    }

    public void setChargeableElementTypeFromName(String chargeableElementTypeName) throws ModelException {
        this.setChargeableElementType(verifyChargeableElementType(chargeableElementTypeName));
    }

    public void setChargeableCategoryFromName(String chargeableCategoryName) throws ModelException {
        this.setChargeableCategory(verifyEChargeableCategory(chargeableCategoryName, this.chargeableElementType));
    }

    public void setChargingElementTypeFromName(String chargingElementTypeName) throws ModelException {
        this.setChargingElementType(verifyChargingElementType(chargingElementTypeName));
    }

    // ─── Utility methods ─── //

    public static double verifyDiscountPercentage(String discountPercentage) throws ModelException {
        double discountPercentageAsDouble;
        try {
            discountPercentageAsDouble = Validators.validateDouble(discountPercentage, true);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le pourcentage de la réduction ne peut pas être vide ou nul", e);
        } catch (ParsingValidatorException e) {
            throw new ModelVerificationException("Le pourcentage de la réduction doit être un nombre compris entre 0 et 1 (inclus)", e);
        }

        if (discountPercentageAsDouble < 0 || discountPercentageAsDouble > 1) {
            throw new ModelVerificationException("Le pourcentage de la réduction doit être un nombre compris entre 0 et 1 (inclus)");
        }

        return discountPercentageAsDouble;
    }

    public static ChargeableElementType verifyChargeableElementType(String chargeableElementTypeName) throws ModelException {
        try {
            chargeableElementTypeName = Validators.validateNotNullOrEmpty(chargeableElementTypeName);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le nom du type d'élément facturable ne peut pas être vide ou nul", e);
        }

        try {
            return ChargeableElementType.valueOf(chargeableElementTypeName);
        } catch (IllegalArgumentException e) {
            throw new ModelVerificationException("Le type d'élément facturable '%s' est inconnu".formatted(chargeableElementTypeName), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static EChargeableCategory verifyEChargeableCategory(String chargeableCategoryName, ChargeableElementType chargeableElementType) throws ModelException {
        try {
            chargeableCategoryName = Validators.validateNotNullOrEmpty(chargeableCategoryName);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le nom de la catégorie facturable ne peut pas être vide ou nul", e);
        }

        if (chargeableElementType == null) {
            throw new ModelVerificationException("Le type d'élément facturable doit être défini avant de résoudre la catégorie facturable");
        }

        Class<? extends EChargeableCategory> chargeableCategoryEnumClass = getChargeableCategoryEnumClass(chargeableElementType);

        try {
            return (EChargeableCategory) Enum.valueOf((Class<? extends Enum>) chargeableCategoryEnumClass, chargeableCategoryName);
        } catch (IllegalArgumentException e) {
            throw new ModelVerificationException("La catégorie facturable '%s' est inconnue pour le type '%s'".formatted(chargeableCategoryName, chargeableElementType), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends EChargeableCategory> getChargeableCategoryEnumClass(ChargeableElementType chargeableElementType) throws ModelVerificationException {
        Class<? extends ChargeableElement> chargeableElementClass = chargeableElementType.getChargeableElementClass();
        Class<? extends EChargeableCategory> chargeableCategoryEnumClass = null;

        for (Class<?> declaredClass : chargeableElementClass.getDeclaredClasses()) {
            if (declaredClass.isEnum() && EChargeableCategory.class.isAssignableFrom(declaredClass)) {
                chargeableCategoryEnumClass = (Class<? extends EChargeableCategory>) declaredClass;
                break;
            }
        }

        if (chargeableCategoryEnumClass == null) {
            throw new ModelVerificationException("Aucune enum implémentant EChargeableCategory trouvée dans la classe '%s'".formatted(chargeableElementClass.getSimpleName()));
        }

        return chargeableCategoryEnumClass;
    }

    public static ChargingElementType verifyChargingElementType(String chargingElementTypeName) throws ModelException {
        try {
            chargingElementTypeName = Validators.validateNotNullOrEmpty(chargingElementTypeName);
        } catch (BlankOrNullValueValidatorException e) {
            throw new ModelVerificationException("Le nom du type d'élément facturant ne peut pas être vide ou nul", e);
        }

        try {
            return ChargingElementType.valueOf(chargingElementTypeName);
        } catch (IllegalArgumentException e) {
            throw new ModelVerificationException("Le type d'élément facturant '%s' est inconnu".formatted(chargingElementTypeName), e);
        }
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public String toString() {
        return "#%d -%s%% %s (%s, %s)".formatted(
            this.getId(),
            (int) (this.discountPercentage * 100),
            this.chargingElementType != null ? this.chargingElementType.name() : "?",
            this.chargeableElementType,
            this.chargeableCategory != null ? this.chargeableCategory.getLabel() : "?"
        );
    }

    @Override
    public boolean isValid() {
        return this.getId() > 0
            && this.camp != null
            && this.chargeableElementType != null
            && this.chargeableCategory != null
            && this.chargingElementType != null
            && this.discountPercentage >= 0
            && this.discountPercentage <= 1;
    }

    @Override
    public void hydrate(Data dataObject) throws ModelException {
        this.setId(dataObject.getId());
        this.pendingCampPk = dataObject.getCampId();
        this.setChargeableElementType(dataObject.getChargeableElementType());
        this.setChargeableCategory(dataObject.getChargeableCategory());
        this.setChargingElementType(dataObject.getChargingElementType());
        this.setDiscountPercentage(dataObject.getDiscountPercentage());
    }

    @Override
    public Data dehydrate() throws ModelException {
        return new Data(this);
    }

    // ─── Sub classes ─── //

    public static class Data extends IdentifiedModelData implements CustomSerializable, JsonConvertible {

        // ─── Properties ─── //

        private int campId = -1;
        private ChargeableElementType chargeableElementType;
        private EChargeableCategory chargeableCategory;
        private ChargingElementType chargingElementType;
        private double discountPercentage;

        // ─── Constructors ─── //

        public Data() {
        }

        public Data(CampDiscount campDiscount) throws ModelException {
            this.setId(campDiscount.getId());
            this.setCampId(campDiscount.getCamp().getId());
            this.setChargeableElementType(campDiscount.getChargeableElementType());
            this.setChargeableCategory(campDiscount.getChargeableCategory());
            this.setChargingElementType(campDiscount.getChargingElementType());
            this.setDiscountPercentage(campDiscount.getDiscountPercentage());
        }

        // ─── Getters ─── //

        public int getCampId() {
            return this.campId;
        }

        public ChargeableElementType getChargeableElementType() {
            return this.chargeableElementType;
        }

        public EChargeableCategory getChargeableCategory() {
            return this.chargeableCategory;
        }

        public ChargingElementType getChargingElementType() {
            return this.chargingElementType;
        }

        public double getDiscountPercentage() {
            return this.discountPercentage;
        }

        // ─── Setters ─── //

        public void setCampId(String campId) throws ModelException {
            this.campId = verifyId(campId);
        }

        public void setCampId(int campId) throws ModelException {
            this.setCampId(String.valueOf(campId));
        }

        public void setChargeableElementType(ChargeableElementType chargeableElementType) throws ModelException {
            if (chargeableElementType == null) {
                throw new ModelVerificationException("Le type d'élément facturable ne peut pas être nul");
            }

            this.chargeableElementType = chargeableElementType;
        }

        public void setChargeableElementType(String chargeableElementTypeName) throws ModelException {
            this.chargeableElementType = verifyChargeableElementType(chargeableElementTypeName);
        }

        public void setChargeableCategory(EChargeableCategory chargeableCategory) throws ModelException {
            if (chargeableCategory == null) {
                throw new ModelVerificationException("La catégorie facturable ne peut pas être nulle");
            }

            this.chargeableCategory = chargeableCategory;
        }

        public void setChargeableCategory(String chargeableCategoryName) throws ModelException {
            this.chargeableCategory = verifyEChargeableCategory(chargeableCategoryName, this.chargeableElementType);
        }

        public void setChargingElementType(ChargingElementType chargingElementType) throws ModelException {
            if (chargingElementType == null) {
                throw new ModelVerificationException("Le type d'élément facturant ne peut pas être nul");
            }

            this.chargingElementType = chargingElementType;
        }

        public void setChargingElementType(String chargingElementTypeName) throws ModelException {
            this.chargingElementType = verifyChargingElementType(chargingElementTypeName);
        }

        public void setDiscountPercentage(String discountPercentage) throws ModelException {
            this.discountPercentage = verifyDiscountPercentage(discountPercentage);
        }

        public void setDiscountPercentage(double discountPercentage) throws ModelException {
            this.setDiscountPercentage(String.valueOf(discountPercentage));
        }

        // ─── Overrides & inheritance ─── //

        @Override
        public void parseJson(String json) throws ParserException {
            JsonObject obj;
            try {
                obj = JsonParser.parseString(json).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                throw new StringParserException("Le JSON reçu n'est pas un objet valide (%s)".formatted(e.getMessage()), e);
            }

            if (!obj.has("id")) {
                throw new StringParserException("Le champ 'id' est manquant");
            }
            if (!obj.has("campId")) {
                throw new StringParserException("Le champ 'campId' est manquant");
            }
            if (!obj.has("chargeableElementTypeName")) {
                throw new StringParserException("Le champ 'chargeableElementTypeName' est manquant");
            }
            if (!obj.has("chargeableCategoryName")) {
                throw new StringParserException("Le champ 'chargeableCategoryName' est manquant");
            }
            if (!obj.has("chargingElementTypeName")) {
                throw new StringParserException("Le champ 'chargingElementTypeName' est manquant");
            }
            if (!obj.has("discountPercentage")) {
                throw new StringParserException("Le champ 'discountPercentage' est manquant");
            }

            try {
                this.setId(obj.get("id").getAsString());
                this.setCampId(obj.get("campId").getAsString());
                this.setChargeableElementType(obj.get("chargeableElementTypeName").getAsString());
                this.setChargeableCategory(obj.get("chargeableCategoryName").getAsString());
                this.setChargingElementType(obj.get("chargingElementTypeName").getAsString());
                this.setDiscountPercentage(obj.get("discountPercentage").getAsString());
            } catch (ModelException e) {
                throw new ParserException(e);
            }
        }

        @Override
        public String toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", this.getId());
            obj.addProperty("campId", this.campId);
            obj.addProperty("chargeableElementTypeName", this.chargeableElementType.name());
            obj.addProperty("chargeableCategoryName", ((Enum<?>) this.chargeableCategory).name());
            obj.addProperty("chargingElementTypeName", this.chargingElementType.name());
            obj.addProperty("discountPercentage", this.discountPercentage);
            return new GsonBuilder().setPrettyPrinting().create().toJson(obj);
        }

    }

}
