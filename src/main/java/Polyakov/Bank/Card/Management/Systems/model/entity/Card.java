package Polyakov.Bank.Card.Management.Systems.model.entity;

import Polyakov.Bank.Card.Management.Systems.converter.CardNumberConverter;
import Polyakov.Bank.Card.Management.Systems.model.constant.CardStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"owner"})
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_number", nullable = false)
    @Convert(converter = CardNumberConverter.class)
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "expiry_date", nullable = false, length = 5)
    private String expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CardStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Card card = (Card) object;
        return Objects.equals(cardNumber, card.cardNumber) && Objects.equals(expiryDate, card.expiryDate) && status == card.status && Objects.equals(balance, card.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, expiryDate, status, balance);
    }
}
