package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public interface UserStorage extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
