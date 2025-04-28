package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@SpringBootTest(classes = ShareItApp.class)
@ActiveProfiles("test")
@Transactional
public class UserServiceImplTest {

    @Autowired
    UserService userService;

    UserDto userDtoRequest1;
    UserDto userDtoRequest2;

    @BeforeEach
    void setUp() {
        userDtoRequest1 = new UserDto(null, "user1", "user1@email.com");
        userDtoRequest2 = new UserDto(null, "user2", "user2@email.com");
    }

    @Test
    void create_shouldCreateUser() {
        UserDto createdUser = userService.create(userDtoRequest1);

        Assertions.assertThat(createdUser.getId()).isNotNull();
        Assertions.assertThat(createdUser.getEmail()).isEqualTo(userDtoRequest1.getEmail());
        Assertions.assertThat(createdUser.getName()).isEqualTo(userDtoRequest1.getName());
    }

    @Test
    void update_shouldUpdateUser() {
        UserDto createdUser = userService.create(userDtoRequest1);

        UserDto updateRequest = new UserDto(null, "updatedName", "updateduser@test.com");
        UserDto updatedUser = userService.update(updateRequest, createdUser.getId());

        Assertions.assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("updateduser@test.com");
        Assertions.assertThat(updatedUser.getName()).isEqualTo("updatedName");
    }

    @Test
    void update_shouldNotUpdateUserEmailConflict() {
        userService.create(userDtoRequest1);
        UserDto createdUser2 = userService.create(userDtoRequest2);

        UserDto conflictUpdate = new UserDto(null, "newName", userDtoRequest1.getEmail());

        Assertions.assertThatThrownBy(() -> userService.update(conflictUpdate, createdUser2.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_shouldNotUpdateNotUser() {
        UserDto updateDto = new UserDto(null, "test", "nonexistent@test.com");

        Assertions.assertThatThrownBy(() -> userService.update(updateDto, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldNotUpdateNameIfNullName() {
        UserDto createdUser = userService.create(userDtoRequest1);

        UserDto updateDto = new UserDto(null, null, "user2@test.com");
        UserDto updatedUser = userService.update(updateDto, createdUser.getId());

        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("user2@test.com");
        Assertions.assertThat(updatedUser.getName()).isEqualTo(userDtoRequest1.getName());
    }

    @Test
    void update_shouldNotUpdateEmailIfNullEmail() {
        UserDto createdUser = userService.create(userDtoRequest1);

        UserDto updateDto = new UserDto(null, "newUserName", null);
        UserDto updatedUser = userService.update(updateDto, createdUser.getId());

        Assertions.assertThat(updatedUser.getEmail()).isEqualTo(userDtoRequest1.getEmail());
        Assertions.assertThat(updatedUser.getName()).isEqualTo("newUserName");
    }

    @Test
    void delete_shouldDeleteUser() {
        UserDto createdUser = userService.create(userDtoRequest1);
        Long userId = createdUser.getId();

        userService.delete(userId);

        Assertions.assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_shouldFindUserById() {
        UserDto createdUser = userService.create(userDtoRequest1);

        UserDto foundUser = userService.findById(createdUser.getId());

        Assertions.assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        Assertions.assertThat(foundUser.getEmail()).isEqualTo(createdUser.getEmail());
        Assertions.assertThat(foundUser.getName()).isEqualTo(createdUser.getName());
    }

    @Test
    void findAll_shouldFindAllUsers() {
        userService.create(userDtoRequest1);
        userService.create(userDtoRequest2);

        List<UserDto> users = userService.findAll();

        Assertions.assertThat(users).hasSize(2);
    }

    @Test
    void create_shouldThrowValidationException_whenEmailIsNull() {
        userDtoRequest1.setEmail(null);

        Assertions.assertThatThrownBy(() -> userService.create(userDtoRequest1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email cannot be null or empty");
    }

    @Test
    void create_shouldThrowValidationException_whenEmailIsBlank() {
        userDtoRequest1.setEmail("  ");

        Assertions.assertThatThrownBy(() -> userService.create(userDtoRequest1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email cannot be null or empty");
    }

    @Test
    void create_shouldThrowValidationException_whenEmailFormatIsInvalid() {
        userDtoRequest1.setEmail("invalid-email");

        Assertions.assertThatThrownBy(() -> userService.create(userDtoRequest1))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    void update_shouldThrowValidationException_whenUserIdIsNull() {
        UserDto updateDto = new UserDto(null, "TestName", "test@test.com");

        Assertions.assertThatThrownBy(() -> userService.update(updateDto, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Id must be specified");
    }
}
