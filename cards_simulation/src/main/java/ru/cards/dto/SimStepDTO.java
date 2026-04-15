package ru.cards.dto;

import ru.cards.simulation.StepResult;

import java.math.BigDecimal;

public record SimStepDTO(String cardName, String cardNumber, StepResult result, BigDecimal sum) {
}
