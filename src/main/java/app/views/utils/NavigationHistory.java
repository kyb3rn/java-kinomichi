package app.views.utils;

import utils.io.menus.MenuLeadTo;

import java.util.ArrayList;
import java.util.Objects;

public class NavigationHistory {

    // ─── Properties ─── //

    private final ArrayList<MenuLeadTo> entries = new ArrayList<>();

    // ─── Utility methods ─── //

    public int size() {
        return this.entries.size();
    }

    public MenuLeadTo getLast() {
        if (this.entries.isEmpty()) {
            return null;
        }

        return this.entries.getLast();
    }

    public void push(MenuLeadTo menuLeadTo) {
        if (!this.entries.isEmpty() && Objects.equals(this.entries.getLast().getLeadTo(), menuLeadTo.getLeadTo())) {
            return;
        }

        this.entries.add(menuLeadTo);
    }

    public MenuLeadTo goBack() {
        return this.goBack(1);
    }

    public MenuLeadTo goBack(int steps) {
        int targetIndex = this.entries.size() - 1 - steps;

        if (targetIndex < 0) {
            this.entries.clear();
            return null;
        }

        MenuLeadTo target = this.entries.get(targetIndex);
        this.entries.subList(targetIndex, this.entries.size()).clear();

        return target;
    }

}
