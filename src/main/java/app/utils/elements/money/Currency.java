package app.utils.elements.money;

import app.utils.elements.money.exceptions.NotACurrencyException;
import app.utils.elements.money.exceptions.UnknownCurrencyException;

public enum Currency {

    EURO("€", Placement.AFTER);

    private final String symbol;
    private final Placement placement;

    Currency(String symbol, Placement placement) {
        this.symbol = symbol;
        this.placement = placement;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public Placement getPlacement() {
        return this.placement;
    }

    public static Currency convert(String name) throws UnknownCurrencyException, NotACurrencyException {
        if (name == null) {
            throw new NotACurrencyException();
        }

        for (Currency c : Currency.values()) {
            if (name.equalsIgnoreCase(c.name())) {
                return c;
            }
        }

        throw new UnknownCurrencyException();
    }

    public enum Placement {

        BEFORE, AFTER

    }

}
