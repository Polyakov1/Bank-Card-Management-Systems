package Polyakov.Bank.Card.Management.Systems.service;

import Polyakov.Bank.Card.Management.Systems.model.dto.request.RegisterRequest;

public interface AuthService {

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param registerRequest DTO с данными для регистрации.
     * @throws Polyakov.Bank.Card.Management.Systems.exception.BadRequestException если пользователь с таким email уже существует.
     * @throws Polyakov.Bank.Card.Management.Systems.exception.ResourceNotFoundException если роль ROLE_USER не найдена в БД.
     */
    void registerUser(RegisterRequest registerRequest);
}
