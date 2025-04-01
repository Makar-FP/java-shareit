package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
public class User {

    private Long id;

    @NotNull
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    private String name;

}
