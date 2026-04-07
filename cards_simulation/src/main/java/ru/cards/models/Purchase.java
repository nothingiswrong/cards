package ru.cards.models;

import java.math.BigDecimal;

public class Purchase {
    private final int id;
    private final BigDecimal initialAmount;
    private BigDecimal remainingAmount;
    private PurchaseStatus status = PurchaseStatus.UNPAID;

    public Purchase(int id, BigDecimal initialAmount) {
        this.id = id;
        this.initialAmount = initialAmount;
        remainingAmount = initialAmount;
    }

    public int getId() {
        return id;
    }

    public BigDecimal getInitialAmount() {
        return initialAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public void decreaseSum(BigDecimal sum) {
        if (status == PurchaseStatus.PAID) {
            throw new IllegalStateException("Нельзя уменьшить сумму оплаченной покупки");
        }
        if (sum.compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    String.format("Сумма списания %s выше не может превышать сумму покупки %s",
                            sum.toString(), remainingAmount.toString()));
        }
        remainingAmount = remainingAmount.subtract(sum);
        if (remainingAmount.compareTo(new BigDecimal("0")) == 0) {
            status = PurchaseStatus.PAID;
        }
    }
}
