package Polyakov.Bank.Card.Management.Systems.model.dto.response;

public record ErrorResponse (
     int status,
     String error,
     String message,
     String path
) {
}
