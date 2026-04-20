package ru.cards.simulation;

import ru.cards.dto.CardStateRecord;
import ru.cards.models.Card;

import java.util.List;

public record SimulationSessionResult(List<Card> wallet,
                                      List<PurchaseResult> purchases,
                                      List<CardStateRecord> initialCards) {
}
