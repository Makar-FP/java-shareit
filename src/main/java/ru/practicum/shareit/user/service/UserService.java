package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    public UserDto create(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(userDto.getEmail()).matches()) {
            throw new ValidationException("Invalid email format: " + userDto.getEmail());
        }
        if (emailExists(userDto, null)) {
            throw new ConflictException("This email is already in use");
        }

        User createdUser = userStorage.create(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(createdUser);
    }

    public UserDto update(UserDto userDto, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id must be specified");
        }

        User existingUser = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));

        if (userDto.getEmail() != null && emailExists(userDto, userId)) {
            throw new ConflictException("This email is already in use");
        }

        User updatedUser = UserMapper.toUser(userDto);
        updatedUser.setId(existingUser.getId());

        updatedUser = userStorage.update(updatedUser);
        return UserMapper.toUserDto(updatedUser);
    }

    public void delete(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));
        userStorage.delete(userId);
    }

    public List<UserDto> getAllUsers() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getById(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));
        return UserMapper.toUserDto(user);
    }

    private boolean emailExists(UserDto userDto, Long userId) {
        return userStorage.findAll().stream()
                .anyMatch(user -> user.getEmail() != null && user.getEmail().equals(userDto.getEmail()) &&
                        (userId == null || !user.getId().equals(userId)));
    }
}