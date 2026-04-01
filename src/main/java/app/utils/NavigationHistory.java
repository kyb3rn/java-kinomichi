package app.utils;

import java.util.ArrayList;

public class NavigationHistory {

    // ─── Properties ─── //

    private final ArrayList<String> paths = new ArrayList<>();

    // ─── Utility methods ─── //

    public int size() {
        return this.paths.size();
    }

    public String getLast() {
        if (this.paths.isEmpty()) {
            return null;
        }

        return this.paths.getLast();
    }


    public void push(String path) {
        if (!this.paths.isEmpty() && this.paths.getLast().equals(path)) {
            return;
        }

        this.paths.add(path);
    }

    public String goBack() {
        return this.goBack(1);
    }

    public String goBack(int steps) {
        int targetIndex = this.paths.size() - 1 - steps;

        if (targetIndex < 0) {
            this.paths.clear();
            return null;
        }

        String targetPath = this.paths.get(targetIndex);
        this.paths.subList(targetIndex, this.paths.size()).clear();

        return targetPath;
    }

}
