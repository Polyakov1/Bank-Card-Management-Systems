package Polyakov.Bank.Card.Management.Systems.model.dto.request;

import Polyakov.Bank.Card.Management.Systems.model.constant.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление статуса карты")
public class UpdateCardStatusRequest {

    @NotNull(message = "Статус не может быть пустым")
    @Schema(description = "Новый статус карты (ACTIVE, BLOCKED)", example = "BLOCKED")
    private CardStatus status;
}
