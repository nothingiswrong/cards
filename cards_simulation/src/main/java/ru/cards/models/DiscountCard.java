package ru.cards.models;

import java.math.BigDecimal;

public class DiscountCard extends Card{
    private final BigDecimal discountAmount;

    public DiscountCard(String name, String cardNumber, BigDecimal discountAmount) {
        super(name, cardNumber);
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    @Override
    public boolean use(Purchase purchase) {
        if (!purchase.tryApplyDiscountCard(getCardNumber())) {
            return false;
        }
        BigDecimal minSum = discountAmount.compareTo(purchase.getRemainingAmount()) < 0
                ? discountAmount
                : purchase.getRemainingAmount();
        purchase.decreaseSum(minSum);
        return true;
    }
}
