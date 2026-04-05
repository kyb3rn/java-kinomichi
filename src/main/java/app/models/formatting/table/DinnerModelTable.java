package app.models.formatting.table;

import app.models.Dinner;
import app.models.ModelException;
import app.models.formatting.ModelKeyTextFormattingPreset;
import app.models.managers.DataManagerException;
import app.models.managers.DataManagers;
import app.models.managers.DinnerReservationDataManager;
import app.utils.elements.money.Price;
import app.utils.elements.time.TimeSlot;
import utils.io.tables.ModelTableDisplay;
import utils.io.tables.TableDisplayFormattingOptions;
import utils.io.text_formatting.TextAlignment;
import utils.io.text_formatting.TextStyle;

public class DinnerModelTable extends IdentifiedModelTable<Dinner> {

    // ─── Constructors ─── //

    public DinnerModelTable(Dinner dinner) throws ModelTableException {
        super(dinner);
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
            DinnerReservationDataManager dinnerReservationDataManager = DataManagers.get(DinnerReservationDataManager.class);
            int reservationCount = dinnerReservationDataManager.getDinnerDinnerReservations(this.getModel()).size();
            return String.valueOf(reservationCount);
        } catch (DataManagerException | ModelException e) {
            return ModelTable.getQuestionMarkFormattedText().toString();
        }
    }

    @ModelTableDisplay(name = "Prix", order = 6)
    public String getPrice() {
        if (this.getModel().getPrice() == null) {
            return ModelTable.getNullFormattedText().toString();
        }

        Price price = this.getModel().getPrice();
        return "%.2f %s".formatted(price.getAmount(), price.getCurrency().getSymbol());
    }

}
