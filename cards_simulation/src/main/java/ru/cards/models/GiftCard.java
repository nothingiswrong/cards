package ru.cards.models;

import java.math.BigDecimal;

public class GiftCard extends Card {
    private final BigDecimal discountAmount;
    private GiftCardStatus status = GiftCardStatus.VALID;

    public GiftCard(String name, String cardNumber, BigDecimal discountAmount) {
        super(name, cardNumber);
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public GiftCardStatus getStatus() {
        return status;
    }

    @Override
    public boolean use(Purchase purchase) {
        if (status == GiftCardStatus.VALID) {
            BigDecimal minSum = discountAmount.compareTo(purchase.getRemainingAmount()) < 0
                    ? discountAmount
                    : purchase.getRemainingAmount();
            purchase.decreaseSum(minSum);
            status = GiftCardStatus.INVALID;
            return true;
        }
        return false;
    }
}
