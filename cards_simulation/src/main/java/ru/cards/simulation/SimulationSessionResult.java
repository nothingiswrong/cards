package ru.cards.simulation;

import ru.cards.models.Card;

import java.util.List;

public record SimulationSessionResult(List<Card> wallet, List<PurchaseResult> purchases) {
}
