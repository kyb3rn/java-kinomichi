package app.views;

import app.events.Event;
import app.models.Model;
import app.utils.menus.ModelDetailMenu;
import utils.io.menus.MenuResponse;
import utils.io.menus.UnhandledMenuResponseType;

public class ModelView<M extends Model> extends View {

    // ─── Properties ─── //

    private final M model;

    // ─── Constructors ─── //

    public ModelView(M model) {
        this.model = model;
    }

    // ─── Overrides & inheritance ─── //

    @Override
    public Event render() {
        ModelDetailMenu<M> ModelDetailMenu = new ModelDetailMenu<>(this.model);

        MenuResponse menuResponse = ModelDetailMenu.use();
        Object response = menuResponse.getResponse();

        if (response instanceof Event event) {
            return event;
        } else {
            throw new UnhandledMenuResponseType();
        }
    }

}
