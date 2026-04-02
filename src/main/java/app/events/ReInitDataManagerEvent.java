package app.events;

import app.models.managers.DataManager;

public class ReInitDataManagerEvent extends Event {

    private final DataManager<?> dataManager;

    public ReInitDataManagerEvent(DataManager<?> dataManager) {
        this.dataManager = dataManager;
    }

    public DataManager<?> getDataManager() {
        return this.dataManager;
    }

}
