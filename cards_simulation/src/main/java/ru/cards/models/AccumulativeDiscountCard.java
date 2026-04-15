package ru.cards.models;

import java.math.BigDecimal;

public class AccumulativeDiscountCard extends Card {
    private BigDecimal baseDiscount;
    private BigDecimal bonusStep;
    private BigDecimal maxDiscount;
    private BigDecimal currentDiscount;

    public AccumulativeDiscountCard(String name, String cardNumber, BigDecimal baseDiscount, BigDecimal bonusStep, BigDecimal maxDiscount) {
        super(name, cardNumber);
        this.baseDiscount = baseDiscount;
        this.bonusStep = bonusStep;
        this.maxDiscount = maxDiscount;
        this.currentDiscount = baseDiscount;
    }

    public BigDecimal getBaseDiscount() {
        return baseDiscount;
    }

    public BigDecimal getBonusStep() {
        return bonusStep;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public BigDecimal getCurrentDiscount() {
        return currentDiscount;
    }

    @Override
    public boolean use(Purchase purchase) {
        if (!purchase.tryApplyDiscountCard(getCardNumber())) {
            return false;
        }
        BigDecimal remaining = purchase.getRemainingAmount();
        BigDecimal apply = currentDiscount.compareTo(remaining) <= 0 ? currentDiscount : remaining;
        purchase.decreaseSum(apply);
        if (currentDiscount.add(bonusStep).compareTo(maxDiscount) <= 0) {
            currentDiscount = currentDiscount.add(bonusStep);
        }
        return true;
    }
}
