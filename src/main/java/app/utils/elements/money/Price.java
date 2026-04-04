package app.utils.elements.money;

import app.utils.elements.money.exceptions.NotACurrencyException;

public class Price extends MoneyAmount {

    public Price(Currency currency) throws NotACurrencyException {
        super(currency);
    }

    public Price(Currency currency, double amount) throws NotACurrencyException, PriceException {
        super(currency);
        this.setAmount(amount);
    }

    @Override
    public void setAmount(double amount) throws PriceException {
        if (amount < 0) {
            throw new PriceException("Le montant d'un prix doit être un nombre entier positif");
        }

        this.amount = amount;
    }

}
