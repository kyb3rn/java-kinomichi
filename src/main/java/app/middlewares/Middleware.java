package app.middlewares;

import app.events.CallUrlEvent;

public abstract class Middleware {

    public abstract CallUrlEvent verify();

}
