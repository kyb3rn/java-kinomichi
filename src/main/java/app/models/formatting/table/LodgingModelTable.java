package app.models.formatting.table;

import app.models.Lodging;
import app.models.ModelException;
import app.models.formatting.ModelKeyTextFormattingPreset;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.LodgingReservationDataManager;
import app.utils.elements.money.Price;
import app.utils.elements.time.TimeSlot;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

public class LodgingModelTable extends IdentifiedModelTable<Lodging> {

    // ─── Constructors ─── //

    public LodgingModelTable(Lodging lodging) throws ModelTableException {
        super(lodging);
    }

    // ─── Utility methods ─── //

    @ModelTableDisplay(name = "#& (stage)", format = @TableDisplayFormattingOptions(preset = ModelKeyTextFormattingPreset.class, alignment = TextAlignment.CENTER), order = 2)
    public String getCampId() {
        try {
            return String.valueOf(this.getModel().getCampId());
        } catch (ModelException e) {
            return ModelTable.getNullFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Nom", format = @TableDisplayFormattingOptions(styles = {TextStyle.ITALIC}), order = 3)
    public String getLabel() {
        return this.getModel().getLabel();
    }

    @ModelTableDisplay(name = "Horaire", order = 4)
    public String getTimeSlot() {
        TimeSlot timeSlot = this.getModel().getTimeSlot();
        return timeSlot != null ? timeSlot.toString() : ModelTable.getNullFormattedText().toString();
    }

    @ModelTableDisplay(name = "Réservations", format = @TableDisplayFormattingOptions(alignment = TextAlignment.CENTER), order = 5)
    public String getReservationCount() {
        try {
            LodgingReservationDataManager lodgingReservationDataManager = DataManagers.get(LodgingReservationDataManager.class);
            int reservationCount = lodgingReservationDataManager.getLodgingLodgingReservations(this.getModel()).size();
            return String.valueOf(reservationCount);
        } catch (DataManagerException | ModelException e) {
            return ModelTable.getQuestionMarkFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Prix chambre partagée", order = 6)
    public String getSharedRoomPrice() {
        if (this.getModel().getSharedRoomPrice() == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        Price price = this.getModel().getSharedRoomPrice();
        return "%.2f %s".formatted(price.getAmount(), price.getCurrency().getSymbol());
    }

    @ModelTableDisplay(name = "Prix chambre individuelle", order = 7)
    public String getSingleRoomPrice() {
        if (this.getModel().getSingleRoomPrice() == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        Price price = this.getModel().getSingleRoomPrice();
        return "%.2f %s".formatted(price.getAmount(), price.getCurrency().getSymbol());
    }

}
