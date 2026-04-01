package app.rooting;

import java.util.List;
import java.util.Map;

public class Request {

    // ─── Properties ─── //

    private final Route matchedRoute;
    private final Map<String, String> namedParameters;
    private final List<String> indexedParameters;

    // ─── Constructors ─── //

    public Request(Route matchedRoute, Map<String, String> namedParameters, List<String> indexedParameters) {
        this.matchedRoute = matchedRoute;
        this.namedParameters = namedParameters;
        this.indexedParameters = indexedParameters;
    }

    // ─── Getters ─── //

    public Route getMatchedRoute() {
        return this.matchedRoute;
    }

    public Map<String, String> getNamedParameters() {
        return this.namedParameters;
    }

    public List<String> getIndexedParameters() {
        return this.indexedParameters;
    }

    // ─── Special getters ─── //

    public String getParameter(String name) {
        return this.namedParameters.get(name);
    }

    public String getParameter(int index) {
        if (index < 0 || index >= this.indexedParameters.size()) {
            return null;
        }

        return this.indexedParameters.get(index);
    }

}
