package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.type.TypeReference;
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
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mvc;
    private UserDto userDto;
    private ItemDtoResponse itemDtoResponse;
    private ItemDtoRequest itemDtoRequest;
    private Item item;
    private CommentDtoResponse commentDtoResponse;
    private CommentDtoRequest commentDtoRequest;


    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        mvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();

        userDto = new UserDto(1L, "test", "test@test.com");

        itemDtoRequest = new ItemDtoRequest("name", "desc", true, null);

        itemDtoResponse = new ItemDtoResponse(1L, "name", "desc", true,
                null, null, null);

        User user = new User(1L, "email@mail.com", "name");
        item = new Item(1L, "name", "desc", true, user, null);

        commentDtoRequest = new CommentDtoRequest("desc");

        commentDtoResponse = new CommentDtoResponse(1L, "text", item, "name", now);
    }

    @Test
    void create_shouldCreateItem() throws Exception {
        when(itemService.create(any(), anyLong()))
                .thenReturn(itemDtoResponse);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoResponse))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.description").value("desc"));
    }

    @Test
    void update_shouldUpdateItem() throws Exception {
        when(itemService.update(any(), anyLong(), anyLong()))
                .thenReturn(itemDtoResponse);

        mvc.perform(patch("/items/" + itemDtoResponse.getId())
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.description").value("desc"));
    }

    @Test
    void findById_shouldFindItemById() throws Exception {
        when(itemService.findById(anyLong(), anyLong()))
                .thenReturn(itemDtoResponse);

        mvc.perform(get("/items/" + itemDtoResponse.getId())
                        .header("X-Sharer-User-Id", 123L)
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.description").value("desc"));
    }

    @Test
    void findByUserId_shouldFindItemByOwnerId() throws Exception {
        when(itemService.findByUserId(anyLong()))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect((result -> {
                    String json = result.getResponse().getContentAsString();
                    List<ItemDtoResponse> dtos = mapper.readValue(json, new TypeReference<>() {
                    });
                    if (dtos.isEmpty()) {
                        throw new AssertionError("Empty ItemDtoResponse list");
                    }
                }));
    }

    @Test
    void search_shouldFindItemByText() throws Exception {
        when(itemService.search(anyString()))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items/search?text=" + anyString())
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect((result -> {
                    String json = result.getResponse().getContentAsString();
                    List<ItemDtoResponse> dtos = mapper.readValue(json, new TypeReference<>() {
                    });
                    if (dtos.isEmpty()) {
                        throw new AssertionError("Empty ItemDtoResponse list");
                    }
                }));
    }

    @Test
    void addComment_shouldAddComment() throws Exception {
        when(itemService.addComment(any(), anyLong(), anyLong()))
                .thenReturn(commentDtoResponse);

        mvc.perform(post("/items/" + itemDtoResponse.getId() + "/comment")
                        .content(mapper.writeValueAsString(commentDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("text"))
                .andExpect(jsonPath("$.item").value(item))
                .andExpect(jsonPath("$.authorName").value("name"));
    }

    @Test
    void create_shouldReturnBadRequest_whenNoUserIdHeader() throws Exception {
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequest_whenInvalidBody() throws Exception {
        mvc.perform(post("/items")
                        .content("{ invalid json }")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldReturnEmptyList_whenTextIsEmpty() throws Exception {
        when(itemService.search("")).thenReturn(List.of());

        mvc.perform(get("/items/search?text=")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    List<ItemDtoResponse> dtos = mapper.readValue(json, new TypeReference<>() {});
                    if (!dtos.isEmpty()) {
                        throw new AssertionError("Expected empty result when text is empty");
                    }
                });
    }
}