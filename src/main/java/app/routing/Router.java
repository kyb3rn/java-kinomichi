package app.routing;

import app.events.CallUrlEvent;
import app.events.Event;
import app.middlewares.Middleware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class Router {

    // ─── Properties ─── //

    private final Map<String, Route> routesByName = new LinkedHashMap<>();

    // ─── Getters ─── //

    public Map<String, Route> getRoutesByName() {
        return Collections.unmodifiableMap(this.routesByName);
    }

    // ─── Special getters ─── //

    public Route getRouteByName(String name) {
        return this.routesByName.get(name);
    }

    // ─── Utility methods ─── //

    public void register(Route route) {
        if (this.routesByName.containsKey(route.getName())) {
            throw new IllegalArgumentException("Une route avec le nom '%s' est deja enregistrée".formatted(route.getName()));
        }

        this.routesByName.put(route.getName(), route);
    }

    public Request match(String path) throws RouteNotFoundException {
        if (path == null) {
            throw new RouteNotFoundException("Le chemin ne peut pas être nul");
        }

        for (Route route : this.routesByName.values()) {
            Matcher matcher = route.getPathPattern().matcher(path);

            if (matcher.matches()) {
                Map<String, String> namedParameters = new LinkedHashMap<>();
                List<String> indexedParameters = new ArrayList<>();

                Map<String, Integer> namedGroups = route.getPathPattern().namedGroups();
                for (Map.Entry<String, Integer> namedGroupEntry : namedGroups.entrySet()) {
                    namedParameters.put(namedGroupEntry.getKey(), matcher.group(namedGroupEntry.getValue()));
                }

                for (int groupIndex = 1; groupIndex <= matcher.groupCount(); groupIndex++) {
                    indexedParameters.add(matcher.group(groupIndex));
                }

                return new Request(route, namedParameters, indexedParameters);
            }
        }

        throw new RouteNotFoundException("Aucune route ne correspond au chemin '%s'".formatted(path));
    }

    public Event dispatch(String path) throws RouteNotFoundException {
        Request request = this.match(path);
        Route matchedRoute = request.getMatchedRoute();

        for (Middleware middleware : matchedRoute.getMiddlewares()) {
            CallUrlEvent middlewareEvent = middleware.verify();
            if (middlewareEvent != null) {
                return middlewareEvent;
            }
        }

        return matchedRoute.getControllerAction().execute(request);
    }

}
