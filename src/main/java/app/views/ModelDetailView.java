package app.views;

import app.events.CallUrlEvent;
import app.events.Event;
import app.models.Model;
import app.models.formatting.table.UnimplementedModelTableException;
import app.utils.menus.ModelDetailMenu;
import utils.helpers.Functions;
import utils.io.menus.MenuResponse;
import utils.io.menus.UnhandledMenuResponseType;

public class ModelDetailView<M extends Model> extends ModelView {

    // ─── Properties ─── //

    private final M model;

    // ─── Constructors ─── //

    public ModelDetailView(M model, String errorBackUrl) {
        super(errorBackUrl);
        this.model = model;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        ModelDetailMenu<M> modelDetailMenu;
        try {
            modelDetailMenu = new ModelDetailMenu<>(this.model);
        } catch (UnimplementedModelTableException e) {
            System.out.println(Functions.styleAsErrorMessage(e.getMessage()));
            return new CallUrlEvent(this.getErrorBackUrl());
        }

        if (!modelDetailMenu.generateTable()) {
            return new CallUrlEvent(this.getErrorBackUrl());
        }

        MenuResponse menuResponse = modelDetailMenu.use();
        Object response = menuResponse.getResponse();

        if (response instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
