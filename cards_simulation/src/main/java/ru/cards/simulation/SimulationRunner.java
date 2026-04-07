package ru.cards.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.cards.dto.CardStateRecord;
import ru.cards.models.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SimulationRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulationRunner.class);

    private static final int MAX_PAY_STEPS_PER_PURCHASE = 100;
    private static final int MAX_PURCHASES_PER_SESSION = 20;


    public static SimulationSessionResult runSimulation() {
        ArrayList<PurchaseResult> results = new ArrayList<PurchaseResult>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<Card> cards = generateCards();
        int purchaseId = 0;

        for (int i = 0; i < MAX_PURCHASES_PER_SESSION; i++) {
            BigDecimal amount = BigDecimal.valueOf(rnd.nextInt(100, 15_000));
            Purchase purchase = new Purchase(++purchaseId, amount);
            if (!checkIfPurchasePossible(cards, purchase.getRemainingAmount())) {
                results.add(new PurchaseResult(purchase, SimulationResultKind.IMPOSSIBLE_TO_MAKE_PURCHASE));
                return new SimulationSessionResult(cards, results);
            }
            PurchaseResult result = new PurchaseResult(purchase);

            for (int p = 0; p < MAX_PAY_STEPS_PER_PURCHASE; p++) {
                SimulationStep step = useCard(purchase, cards);
                result.getCurrentSimulationSteps().add(step);
                if (purchase.getStatus() == PurchaseStatus.PAID) {
                    result.setResultKind(SimulationResultKind.PURCHASE_MADE);
                    break;
                }
            }
            if (result.getResultKind() != SimulationResultKind.PURCHASE_MADE) {
                result.setResultKind(SimulationResultKind.EXCEEDED_MAX_NUMBER_OF_PURCHASES);
            }
            results.add(result);
        }
        return new SimulationSessionResult(cards, results);
    }


    public static List<CardStateRecord> snapshotWallet(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return List.of();
        }
        ArrayList<CardStateRecord> out = new ArrayList<>(cards.size());
        for (Card c : cards) {
            out.add(toCardState(c));
        }
        return List.copyOf(out);
    }

    private static CardStateRecord toCardState(Card c) {
        String type = c.getClass().getSimpleName();
        String name = c.getName();
        String number = c.getCardNumber();
        String details;
        if (c instanceof DebetCard d) {
            details = "баланс: " + d.getAmount();
        } else if (c instanceof DiscountCard d) {
            details = "размер скидки: " + d.getDiscountAmount();
        } else if (c instanceof GiftCard g) {
            details = "номинал: " + g.getDiscountAmount() + "; статус: " + g.getStatus();
        } else if (c instanceof AccumulativeDiscountCard a) {
            details = "текущая скидка: " + a.getCurrentDiscount()
                    + "; макс.: " + a.getMaxDiscount()
                    + "; шаг бонуса: " + a.getBonusStep()
                    + "; база: " + a.getBaseDiscount();
        } else {
            details = "";
        }
        return new CardStateRecord(type, name, number, details);
    }

    public static SimulationStep useCard(Purchase purchase, List<Card> cards) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int idx = rnd.nextInt(0, cards.size());
        Card card = cards.get(idx);
        BigDecimal before = purchase.getRemainingAmount();
        boolean ok = false;
        try {
            ok = card.use(purchase);
        } catch (Exception e) {
            log.error(e.toString());
        }
        BigDecimal delta = before.subtract(purchase.getRemainingAmount());
        StepResult stepResult = ok ? StepResult.SUCCESS : StepResult.FAILURE;
        return new SimulationStep(card, purchase, delta, stepResult);
    }

    public static boolean checkIfPurchasePossible(List<Card> cards, BigDecimal purchaseAmount) {
        boolean containsDiscount = cards.stream().anyMatch(c -> c instanceof DiscountCard
                || c instanceof AccumulativeDiscountCard);
        if (containsDiscount) {
            return true;
        }
        BigDecimal giftCardSum = BigDecimal.ZERO;
        for (Card card : cards) {
            if (card instanceof GiftCard gift && gift.getStatus() == GiftCardStatus.VALID) {
                giftCardSum = giftCardSum.add(gift.getDiscountAmount());
            }
        }
        BigDecimal maxDebetSum = BigDecimal.ZERO;
        for (Card card : cards) {
            if (card instanceof DebetCard debetCard) {
                maxDebetSum = debetCard.getAmount().max(maxDebetSum);
            }
        }
        BigDecimal maxSum = giftCardSum.add(maxDebetSum);
        return maxSum.compareTo(purchaseAmount) >= 0;
    }


    public static String formatStepsAsText(List<SimulationStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int stepNo = 1;
        for (SimulationStep step : steps) {
            Card c = step.card();
            sb.append("    ")
                    .append(stepNo++).append(". ")
                    .append(c.getClass().getSimpleName())
                    .append(" «").append(c.getName()).append("»")
                    .append(" | № ").append(c.getCardNumber())
                    .append(" | списано: ").append(step.sum())
                    .append(" | исход: ").append(step.result())
                    .append("\n");
        }
        return sb.toString();
    }


    private static List<Card> generateCards() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int count = rnd.nextInt(6, 21);
        ArrayList<Card> cards = new ArrayList<Card>(count);
        for (int i = 0; i < count; i++) {
            String name = "Карта " + (i + 1);
            String cardNumber = String.format("%016d",
                    rnd.nextLong(1_000_000_000_000_000L, 9_000_000_000_000_000L));
            switch (rnd.nextInt(4)) {
                case 0 -> cards.add(new DiscountCard(name, cardNumber,
                        BigDecimal.valueOf(rnd.nextInt(100, 2000))));
                case 1 -> cards.add(new DebetCard(name, cardNumber,
                        BigDecimal.valueOf(rnd.nextInt(500, 20_000))));
                case 2 -> cards.add(new GiftCard(name, cardNumber,
                        BigDecimal.valueOf(rnd.nextInt(200, 3000))));
                case 3 -> {
                    BigDecimal base = BigDecimal.valueOf(rnd.nextInt(50, 300));
                    BigDecimal step = BigDecimal.valueOf(rnd.nextInt(10, 80));
                    BigDecimal maxDiscount = BigDecimal.valueOf(rnd.nextInt(300, 1000));
                    if (maxDiscount.compareTo(base) < 0) {
                        maxDiscount = base;
                    }
                    cards.add(new AccumulativeDiscountCard(name, cardNumber, base, step, maxDiscount));
                }
                default -> throw new IllegalStateException();
            }
        }
        return cards;
    }
}
