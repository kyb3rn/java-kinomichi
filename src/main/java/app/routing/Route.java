package app.routing;

import app.middlewares.Middleware;

import java.util.List;
import java.util.regex.Pattern;

public class Route {

    // ─── Properties ─── //

    private static final Pattern ROUTE_NAME_VALIDATION_PATTERN = Pattern.compile("[a-z_]+(\\.[a-z_]+)*");

    private final String name;
    private final Pattern pathPattern;
    private final ControllerAction controllerAction;
    private final List<Middleware> middlewares;

    // ─── Constructors ─── //

    public Route(String name, String pathRegex, ControllerAction controllerAction) {
        this(name, pathRegex, controllerAction, List.of());
    }

    public Route(String name, String pathRegex, ControllerAction controllerAction, Middleware middleware) {
        this(name, pathRegex, controllerAction, List.of(middleware));
    }

    public Route(String name, String pathRegex, ControllerAction controllerAction, List<Middleware> middlewares) {
        if (name == null || !ROUTE_NAME_VALIDATION_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Le nom de route '%s' ne respecte pas le format requis ([a-z_]+(\\.[a-z_]+)*)".formatted(name));
        }

        if (pathRegex == null || pathRegex.isBlank()) {
            throw new IllegalArgumentException("Le pattern de chemin de la route '%s' ne peut pas être vide".formatted(name));
        }

        if (controllerAction == null) {
            throw new IllegalArgumentException("L'action du controller de la route '%s' ne peut pas être nulle".formatted(name));
        }

        if (middlewares == null) {
            middlewares = List.of();
        }

        this.name = name;
        this.pathPattern = Pattern.compile(pathRegex);
        this.controllerAction = controllerAction;
        this.middlewares = middlewares;
    }

    // ─── Getters ─── //

    public String getName() {
        return this.name;
    }

    public Pattern getPathPattern() {
        return this.pathPattern;
    }

    public ControllerAction getControllerAction() {
        return this.controllerAction;
    }

    public List<Middleware> getMiddlewares() {
        return this.middlewares;
    }

}
