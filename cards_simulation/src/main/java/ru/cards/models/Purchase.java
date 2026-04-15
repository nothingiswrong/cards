package ru.cards.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Purchase {
    private final int id;
    private final BigDecimal initialAmount;
    private BigDecimal remainingAmount;
    private final List<String> appliedDiscountCardNumbers = new ArrayList<>();
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

    public List<String> getAppliedDiscountCardNumbers() {
        return List.copyOf(appliedDiscountCardNumbers);
    }

    public boolean tryApplyDiscountCard(String cardNumber) {
        if (appliedDiscountCardNumbers.contains(cardNumber)) {
            return false;
        }
        appliedDiscountCardNumbers.add(cardNumber);
        return true;
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
    }

    public void markAsPaidByDebetCard() {
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Нельзя закрыть покупку дебетовой картой, пока есть остаток");
        }
        status = PurchaseStatus.PAID;
    }
}
