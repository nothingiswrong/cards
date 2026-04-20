package ru.cards.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.cards.dto.CardStateRecord;
import ru.cards.models.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SimulationRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulationRunner.class);

    private static final int MAX_PAY_STEPS_PER_PURCHASE = 100;
    private static final int MAX_PURCHASES_PER_SESSION = 1000;


    public static SimulationSessionResult runSimulation() {
        ArrayList<PurchaseResult> results = new ArrayList<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<Card> cards = generateCards();
        List<CardStateRecord> initialCards = snapshotWallet(cards);
        int purchaseId = 0;

        while (results.size() < MAX_PURCHASES_PER_SESSION) {
            BigDecimal amount = BigDecimal.valueOf(rnd.nextInt(100, 15_000));
            Purchase purchase = new Purchase(++purchaseId, amount);
            PurchaseResult result = new PurchaseResult(purchase);
            List<Card> drawPile = new ArrayList<>(cards);
            List<DebetCard> deferredDebetCards = new ArrayList<>();
            Collections.shuffle(drawPile, rnd);

            for (int p = 0; p < MAX_PAY_STEPS_PER_PURCHASE && !drawPile.isEmpty(); p++) {
                Card card = drawPile.removeLast();
                if (card instanceof DebetCard debetCard) {
                    if (debetCard.getAmount().compareTo(purchase.getRemainingAmount()) >= 0) {
                        SimulationStep step = useSpecificCard(purchase, debetCard);
                        result.getCurrentSimulationSteps().add(step);
                        result.setResultKind(step.result() == StepResult.SUCCESS
                                ? SimulationResultKind.PURCHASE_MADE
                                : SimulationResultKind.IMPOSSIBLE_TO_MAKE_PURCHASE);
                        break;
                    }
                    deferredDebetCards.add(debetCard);
                    result.getCurrentSimulationSteps().add(new SimulationStep(
                            debetCard,
                            purchase,
                            BigDecimal.ZERO,
                            StepResult.FAILURE
                    ));
                } else {
                    SimulationStep step = useSpecificCard(purchase, card);
                    result.getCurrentSimulationSteps().add(step);
                }
                if (purchase.getStatus() == PurchaseStatus.PAID) {
                    result.setResultKind(SimulationResultKind.PURCHASE_MADE);
                    break;
                }

            }

            if (result.getResultKind() == SimulationResultKind.RUNNING) {
                if (deferredDebetCards.isEmpty()) {
                    result.setResultKind(SimulationResultKind.IMPOSSIBLE_TO_MAKE_PURCHASE);
                } else {
                    for (DebetCard debetCard : deferredDebetCards) {
                        SimulationStep debitStep = useSpecificCard(purchase, debetCard);
                        result.getCurrentSimulationSteps().add(debitStep);
                        if (purchase.getStatus() == PurchaseStatus.PAID) {
                            result.setResultKind(SimulationResultKind.PURCHASE_MADE);
                            break;
                        }
                    }
                    if (result.getResultKind() == SimulationResultKind.RUNNING) {
                        result.setResultKind(SimulationResultKind.IMPOSSIBLE_TO_MAKE_PURCHASE);
                    }
                }
            }
            results.add(result);
            if (result.getResultKind() == SimulationResultKind.IMPOSSIBLE_TO_MAKE_PURCHASE) {
                break;
            }
        }
        return new SimulationSessionResult(cards, results, initialCards);
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
        String details = switch (c) {
            case DebetCard d -> "баланс: " + d.getAmount();
            case DiscountCard d -> "размер скидки: " + d.getDiscountAmount();
            case GiftCard g -> "номинал: " + g.getDiscountAmount() + "; статус: " + g.getStatus();
            case AccumulativeDiscountCard a -> "текущая скидка: " + a.getCurrentDiscount()
                    + "; макс.: " + a.getMaxDiscount()
                    + "; шаг бонуса: " + a.getBonusStep()
                    + "; база: " + a.getBaseDiscount();
            default -> "";
        };
        return new CardStateRecord(type, name, number, details);
    }

    public static SimulationStep useCard(Purchase purchase, List<Card> cards) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int idx = rnd.nextInt(0, cards.size());
        Card card = cards.get(idx);
        return useSpecificCard(purchase, card);
    }

    private static SimulationStep useSpecificCard(Purchase purchase, Card card) {
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
