package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDto {
    Long id;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must match the pattern name@domain.xx")
    String email;

    @NotBlank(message = "Name must not be blank")
    String name;
}
