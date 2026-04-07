package ru.cards.models;

public abstract class Card {
    private final String name;
    private final String  cardNumber;

    public Card(String name, String cardNumber) {
        this.name = name;
        this.cardNumber = cardNumber;
    }

    public String getName() {
        return name;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public boolean use(Purchase purchase) {
        return false;
    }
}
