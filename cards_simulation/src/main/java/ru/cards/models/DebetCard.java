package ru.cards.models;

import java.math.BigDecimal;

public class DebetCard extends Card {
    private BigDecimal amount;

    public DebetCard(String name, String cardNumber, BigDecimal amount) {
        super(name, cardNumber);
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean use(Purchase purchase) {
        BigDecimal remaining = purchase.getRemainingAmount();
        if (amount.compareTo(remaining) < 0) {
            return false;
        }
        amount = amount.subtract(remaining);
        purchase.decreaseSum(remaining);
        purchase.markAsPaidByDebetCard();
        return true;
    }
}
