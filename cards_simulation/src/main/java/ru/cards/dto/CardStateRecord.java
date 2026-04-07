package ru.cards.dto;

public record CardStateRecord(
        String cardType,
        String name,
        String cardNumber,
        String details
) {
}
