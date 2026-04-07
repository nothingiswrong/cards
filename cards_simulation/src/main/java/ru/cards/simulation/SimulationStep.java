package ru.cards.simulation;

import ru.cards.models.Card;
import ru.cards.models.Purchase;

import java.math.BigDecimal;

public record SimulationStep(Card card, Purchase purchase, BigDecimal sum, StepResult result) {
}
