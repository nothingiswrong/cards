package ru.cards.simulation;

import ru.cards.models.Card;
import ru.cards.models.Purchase;

import java.util.ArrayList;
import java.util.List;

public class PurchaseResult {

    private final Purchase purchase;
    private final List<Card> currentSimulationCards;
    private final List<SimulationStep> currentSimulationSteps;
    private SimulationResultKind resultKind;

    public PurchaseResult(Purchase purchase) {
        this.purchase = purchase;
        this.currentSimulationCards = new ArrayList<>();
        this.currentSimulationSteps = new ArrayList<>();
        resultKind = SimulationResultKind.RUNNING;
    }

    public PurchaseResult(Purchase purchase, SimulationResultKind resultKind) {
        this.resultKind = resultKind;
        this.purchase = purchase;
        this.currentSimulationCards = new ArrayList<>();
        this.currentSimulationSteps = new ArrayList<>();
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public List<Card> getCurrentSimulationCards() {
        return currentSimulationCards;
    }

    public List<SimulationStep> getCurrentSimulationSteps() {
        return currentSimulationSteps;
    }

    public SimulationResultKind getResultKind() {
        return resultKind;
    }

    public void setResultKind(SimulationResultKind resultKind) {
        this.resultKind = resultKind;
    }
}