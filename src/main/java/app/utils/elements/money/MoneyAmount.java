package app.utils.elements.money;

import app.utils.elements.money.exceptions.NotACurrencyException;

public class MoneyAmount {

    protected Currency currency;
    protected double amount = 0;

    public MoneyAmount(Currency currency) throws NotACurrencyException {
        this.setCurrency(currency);
    }

    public MoneyAmount(Currency currency, double amount) throws NotACurrencyException, MoneyAmountException {
        this.setCurrency(currency);
        this.setAmount(amount);
    }

    public Currency getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setCurrency(Currency currency) throws NotACurrencyException {
        if (currency == null) {
            throw new NotACurrencyException();
        }

        this.currency = currency;
    }

    public void setAmount(double amount) throws MoneyAmountException {
        this.amount = amount;
    }

}
