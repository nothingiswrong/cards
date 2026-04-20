package ru.cards.dto;

import java.util.List;

public record SimulationRunResponse(
        List<CardStateRecord> initialCards,
        List<CardStateRecord> cards,
        List<PurchaseSimulationRecord> purchases) {
}
