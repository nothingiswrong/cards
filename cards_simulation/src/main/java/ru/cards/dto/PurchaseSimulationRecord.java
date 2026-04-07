package ru.cards.dto;

import ru.cards.models.PurchaseStatus;
import ru.cards.simulation.SimulationResultKind;

import java.math.BigDecimal;

public record PurchaseSimulationRecord(
        int purchaseId,
        BigDecimal initialAmount,
        BigDecimal remainingAmount,
        PurchaseStatus purchaseStatus,
        SimulationResultKind simulationResultKind,
        String stepsText) {
}
