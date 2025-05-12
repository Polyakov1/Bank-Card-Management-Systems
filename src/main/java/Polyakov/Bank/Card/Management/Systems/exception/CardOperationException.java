package Polyakov.Bank.Card.Management.Systems.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CardOperationException extends RuntimeException {
    public CardOperationException(String message) {
        super(message);
    }
}