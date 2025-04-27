package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final UserStorage userStorage;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        validateEmail(userDto.getEmail());
        checkEmailConflict(userDto.getEmail(), userDto.getId());

        User createdUser = userStorage.save(UserMapper.mapUserDtoToUser(userDto));
        return UserMapper.mapUserToUserDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id must be specified");
        }

        User user = getUserById(userId);

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            validateEmail(userDto.getEmail());
            checkEmailConflict(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }

        userStorage.save(user);
        return UserMapper.mapUserToUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        getUserById(userId); // Проверка на наличие
        User user = userStorage.getReferenceById(userId);
        userStorage.delete(user);
    }

    @Override
    @Transactional
    public List<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::mapUserToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto findById(Long userId) {
        User user = getUserById(userId);
        return UserMapper.mapUserToUserDto(user);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    private void checkEmailConflict(String email, Long currentUserId) {
        Optional<User> existing = userStorage.findByEmail(email);
        if (existing.isPresent() && !existing.get().getId().equals(currentUserId)) {
            throw new ConflictException("This email is already in use");
        }
    }

    private User getUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User was not found"));
    }
}