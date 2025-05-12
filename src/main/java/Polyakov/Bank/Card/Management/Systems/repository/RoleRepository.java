package Polyakov.Bank.Card.Management.Systems.repository;

import Polyakov.Bank.Card.Management.Systems.model.constant.RoleType;
import Polyakov.Bank.Card.Management.Systems.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Метод для поиска роли по её имени (enum)
    Optional<Role> findByName(RoleType name);
}