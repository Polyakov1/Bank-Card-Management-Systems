package Polyakov.Bank.Card.Management.Systems.service.impl;

import Polyakov.Bank.Card.Management.Systems.exception.BadRequestException;
import Polyakov.Bank.Card.Management.Systems.exception.CardOperationException;
import Polyakov.Bank.Card.Management.Systems.exception.InsufficientFundsException;
import Polyakov.Bank.Card.Management.Systems.exception.ResourceNotFoundException;
import Polyakov.Bank.Card.Management.Systems.mapper.CardMapper;
import Polyakov.Bank.Card.Management.Systems.model.constant.CardStatus;
import Polyakov.Bank.Card.Management.Systems.model.dto.BalanceDto;
import Polyakov.Bank.Card.Management.Systems.model.dto.CardDto;
import Polyakov.Bank.Card.Management.Systems.model.dto.request.CreateCardRequest;
import Polyakov.Bank.Card.Management.Systems.model.dto.request.TransferRequest;
import Polyakov.Bank.Card.Management.Systems.model.entity.Card;
import Polyakov.Bank.Card.Management.Systems.model.entity.User;
import Polyakov.Bank.Card.Management.Systems.repository.CardRepository;
import Polyakov.Bank.Card.Management.Systems.repository.UserRepository;
import Polyakov.Bank.Card.Management.Systems.repository.specification.CardSpecification;
import Polyakov.Bank.Card.Management.Systems.service.CardNumberGeneratorService;
import Polyakov.Bank.Card.Management.Systems.service.CardService;
import Polyakov.Bank.Card.Management.Systems.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static Polyakov.Bank.Card.Management.Systems.util.ServiceMessagesUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final AuthenticationHelper authenticationHelper;
    private final CardNumberGeneratorService cardNumberGeneratorService;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        log.info("Admin request to create card for user ID: {}", request.getUserId());
        User owner = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Card card = new Card();
        card.setOwner(owner);
        String generatedCardNumber = cardNumberGeneratorService.generateUniqueCardNumber();
        card.setCardNumber(generatedCardNumber);
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        Card savedCard = cardRepository.save(card);
        log.info("Card created successfully with ID: {} for user ID: {}", savedCard.getId(), owner.getId());
        return cardMapper.toDto(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getAllCardsFiltered(CardStatus status, String ownerEmail, BigDecimal minBalance, BigDecimal maxBalance, Pageable pageable) {
        log.debug("Admin request to get filtered cards. Filters: status={}, ownerEmail={}, minBalance={}, maxBalance={}. Pageable: {}",
                status, ownerEmail, minBalance, maxBalance, pageable);
        Specification<Card> spec = CardSpecification.filterBy(status, ownerEmail, minBalance, maxBalance);
        Page<Card> cardPage = cardRepository.findAll(spec, pageable);
        return cardPage.map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardByIdAsAdmin(UUID id) {
        log.debug("Admin request to get card by ID: {}", id);
        Card card = findCardByIdOrThrow(id);
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public CardDto updateCardStatusAsAdmin(UUID id, CardStatus newStatus) {
        log.info("Admin request to update status for card ID: {} to {}", id, newStatus);
        if (newStatus == CardStatus.EXPIRED) {
            throw new BadRequestException(ADMIN_CANNOT_MANUALLY_SET_STATUS_EXPIRED);
        }
        Card card = findCardByIdOrThrow(id);

        if (card.getStatus() == newStatus) {
            log.warn("Card ID: {} already has status {}. No update performed.", id, newStatus);
            return cardMapper.toDto(card);
        }

        if (card.getStatus() == CardStatus.EXPIRED && newStatus == CardStatus.ACTIVE) {
            throw new CardOperationException(CANNOT_ACTIVATE_EXPIRED_CARD_ID + id);
        }


        card.setStatus(newStatus);
        Card updatedCard = cardRepository.save(card);
        log.info("Card ID: {} status updated to {} by admin", updatedCard.getId(), newStatus);
        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCardAsAdmin(UUID id) {
        log.warn("Admin request to delete card ID: {}", id);
        if (!cardRepository.existsById(id)) {
            throw new ResourceNotFoundException(CARD_NOT_FOUND + id);
        }
        cardRepository.deleteById(id);
        log.info("Card ID: {} deleted successfully by admin", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getCurrentUserCardsFiltered(CardStatus status, BigDecimal minBalance, BigDecimal maxBalance, Pageable pageable) {
        User currentUser = authenticationHelper.getCurrentUser();
        log.debug("User {} request to get own filtered cards. Filters: status={}, minBalance={}, maxBalance={}. Pageable: {}",
                currentUser.getEmail(), status, minBalance, maxBalance, pageable);
        Specification<Card> spec = CardSpecification.filterBy(status, null, minBalance, maxBalance, currentUser);
        Page<Card> cardPage = cardRepository.findAll(spec, pageable);
        return cardPage.map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCurrentUserCardById(UUID id) {
        User currentUser = authenticationHelper.getCurrentUser();
        log.debug("User {} request to get own card by ID: {}", currentUser.getEmail(), id);
        Card card = findCardByIdAndOwnerOrThrow(id, currentUser);
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public CardDto requestBlockCard(UUID id) {
        User currentUser = authenticationHelper.getCurrentUser();
        log.info("User {} request to block own card ID: {}", currentUser.getEmail(), id);
        Card card = findCardByIdAndOwnerOrThrow(id, currentUser);

        if (card.getStatus() == CardStatus.BLOCKED) {
            log.warn("User {} requested to block card ID: {}, but it's already blocked.", currentUser.getEmail(), id);
            return cardMapper.toDto(card);
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Cannot block card ID: " + id + " because its status is not ACTIVE (current: " + card.getStatus() + ")");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        log.info("Card ID: {} blocked successfully upon user {} request", updatedCard.getId(), currentUser.getEmail());
        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public void transferFunds(TransferRequest request) {
        User currentUser = authenticationHelper.getCurrentUser();
        UUID fromCardId = request.getFromCardId();
        UUID toCardId = request.getToCardId();
        BigDecimal amount = request.getAmount();

        log.info("User {} request to transfer {} from card ID: {} to card ID: {}",
                currentUser.getEmail(), amount, fromCardId, toCardId);

        Objects.requireNonNull(fromCardId, "From card ID cannot be null");
        Objects.requireNonNull(toCardId, "To card ID cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");

        if (fromCardId.equals(toCardId)) {
            throw new BadRequestException("Cannot transfer funds to the same card.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be positive.");
        }

        Card fromCard = findCardByIdAndOwnerOrThrow(fromCardId, currentUser);
        Card toCard = findCardByIdAndOwnerOrThrow(toCardId, currentUser);


        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Source card ID: " + fromCardId + " is not active.");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new CardOperationException("Destination card ID: " + toCardId + " is not active.");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card ID: " + fromCardId);
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.saveAll(List.of(fromCard, toCard));

        log.info("Transfer successful for user {}: {} transferred from card {} to card {}",
                currentUser.getEmail(), amount, fromCardId, toCardId);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceDto getCurrentUserCardBalance(UUID id) {
        User currentUser = authenticationHelper.getCurrentUser();
        log.debug("User {} request for balance of card ID: {}", currentUser.getEmail(), id);
        Card card = findCardByIdAndOwnerOrThrow(id, currentUser);
        return new BalanceDto(card.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceDto getCurrentUserTotalBalance() {
        User currentUser = authenticationHelper.getCurrentUser();
        BigDecimal totalBalance = cardRepository.getSumBalanceByOwner(currentUser);
        return new BalanceDto(totalBalance);
    }

    private Card findCardByIdOrThrow(UUID id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id));
    }

    private Card findCardByIdAndOwnerOrThrow(UUID id, User owner) {
        return cardRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + id + " for owner " + owner.getEmail()));
    }
}
