package app.utils;

import app.routing.RouteNotFoundException;
import app.routing.Router;

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

    public String goBackUntilDifferentRoute(Router router) {
        if (this.paths.size() < 2) {
            return this.goBack();
        }

        String currentPath = this.paths.getLast();
        String currentRouteName;

        try {
            currentRouteName = router.match(currentPath).getMatchedRoute().getName();
        } catch (RouteNotFoundException e) {
            return this.goBack();
        }

        for (int i = this.paths.size() - 2; i >= 0; i--) {
            try {
                String routeName = router.match(this.paths.get(i)).getMatchedRoute().getName();

                if (!routeName.equals(currentRouteName)) {
                    int steps = this.paths.size() - 1 - i;
                    return this.goBack(steps);
                }
            } catch (RouteNotFoundException e) {
                int steps = this.paths.size() - 1 - i;
                return this.goBack(steps);
            }
        }

        this.paths.clear();
        return null;
    }

}
