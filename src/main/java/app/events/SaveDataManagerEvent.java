package app.events;

import app.models.managers.DataManager;

public class SaveDataManagerEvent extends Event {

    private final DataManager<?> dataManager;

    public SaveDataManagerEvent(DataManager<?> dataManager) {
        this.dataManager = dataManager;
    }

    public DataManager<?> getDataManager() {
        return this.dataManager;
    }

}
