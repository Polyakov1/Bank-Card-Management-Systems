package Polyakov.Bank.Card.Management.Systems.service;

public interface CardNumberGeneratorService {
    /**
     * Генерирует уникальный и валидный по алгоритму Луна номер банковской карты.
     * @return Строка с номером карты (16 цифр без разделителей).
     */
    String generateUniqueCardNumber();
}
