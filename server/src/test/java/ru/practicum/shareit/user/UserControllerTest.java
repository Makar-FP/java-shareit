package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    UserServiceImpl userService;

    private MockMvc mvc;

    @InjectMocks
    private UserController controller;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();

        userDto = new UserDto(
                1L,
                "test",
                "test@test.com");
    }

    @Test
    void create_shouldCreateUser() throws Exception {
        when(userService.create(any()))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void create_shouldReturnBadRequest_whenBodyIsEmpty() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldUpdateUser() throws Exception {
        when(userService.update(any(), anyLong()))
                .thenReturn(userDto);

        mvc.perform(patch("/users/" + userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void update_shouldReturnBadRequest_whenBodyIsEmpty() throws Exception {
        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldDeleteUser() throws Exception {
        mvc.perform(delete("/users/" + userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1))
                .delete(anyLong());
    }

    @Test
    void findById_shouldFindUserById() throws Exception {
        when(userService.findById(anyLong()))
                .thenReturn(userDto);

        mvc.perform(get("/users/" + userDto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void findAll_shouldFindAllUsers() throws Exception {
        when(userService.findAll())
                .thenReturn(List.of(userDto));

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("test"))
                .andExpect(jsonPath("$[0].email").value("test@test.com"));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoUsers() throws Exception {
        when(userService.findAll())
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}