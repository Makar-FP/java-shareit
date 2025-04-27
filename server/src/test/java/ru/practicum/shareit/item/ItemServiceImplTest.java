package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItApp;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(classes = ShareItApp.class)
@ActiveProfiles("test")
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceImplTest {

    @Autowired
    ItemService itemService;

    @Autowired
    UserService userService;

    @Autowired
    BookingService bookingService;

    @Autowired
    ItemRequestService itemRequestService;

    static UserDto userDtoRequest1;
    static UserDto userDtoRequest2;

    static ItemDtoRequest itemDtoRequest1;
    static ItemDtoRequest itemDtoRequest2;

    @BeforeAll
    static void setup() {
        userDtoRequest1 = new UserDto(null, "User One", "user1@example.com");
        userDtoRequest2 = new UserDto(null, "User Two", "user2@example.com");

        itemDtoRequest1 = new ItemDtoRequest("Item1", "Description1", true, null);
        itemDtoRequest2 = new ItemDtoRequest("Item2", "Description2", true, null);
    }

    @Test
    void create_shouldCreateItem() {
        UserDto user = userService.create(userDtoRequest1);

        ItemDtoResponse createdItem = itemService.create(itemDtoRequest1, user.getId());

        Assertions.assertThat(createdItem.getName()).isEqualTo(itemDtoRequest1.getName());
        Assertions.assertThat(createdItem.getDescription()).isEqualTo(itemDtoRequest1.getDescription());
        Assertions.assertThat(createdItem.getAvailable()).isEqualTo(itemDtoRequest1.getAvailable());
    }

    @Test
    void create_shouldFailWhenUserNotFound() {
        Assertions.assertThatThrownBy(() -> itemService.create(itemDtoRequest1, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldUpdateItem() {
        UserDto owner = userService.create(userDtoRequest1);
        ItemDtoResponse createdItem = itemService.create(itemDtoRequest1, owner.getId());

        ItemDtoResponse updatedItem = itemService.update(itemDtoRequest2, createdItem.getId(), owner.getId());

        Assertions.assertThat(updatedItem.getName()).isEqualTo(itemDtoRequest2.getName());
        Assertions.assertThat(updatedItem.getDescription()).isEqualTo(itemDtoRequest2.getDescription());
        Assertions.assertThat(updatedItem.getAvailable()).isEqualTo(itemDtoRequest2.getAvailable());
    }

    @Test
    void update_shouldFailWhenItemNotFound() {
        UserDto owner = userService.create(userDtoRequest1);

        Assertions.assertThatThrownBy(() -> itemService.update(itemDtoRequest2, 999L, owner.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldFailWhenUserIsNotOwner() {
        UserDto owner = userService.create(userDtoRequest1);
        UserDto anotherUser = userService.create(userDtoRequest2);

        ItemDtoResponse item = itemService.create(itemDtoRequest1, owner.getId());

        Assertions.assertThatThrownBy(() -> itemService.update(itemDtoRequest2, item.getId(), anotherUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_shouldReturnItemWithBookingsIfOwner() {
        UserDto owner = userService.create(userDtoRequest1);
        ItemDtoResponse item = itemService.create(itemDtoRequest1, owner.getId());

        ItemDtoResponse foundItem = itemService.findById(item.getId(), owner.getId());

        Assertions.assertThat(foundItem.getName()).isEqualTo(item.getName());
        Assertions.assertThat(foundItem.getLastBooking()).isNull();
        Assertions.assertThat(foundItem.getNextBooking()).isNull();
    }

    @Test
    void findById_shouldReturnItemWithoutBookingsIfNotOwner() {
        UserDto owner = userService.create(userDtoRequest1);
        UserDto anotherUser = userService.create(userDtoRequest2);

        ItemDtoResponse item = itemService.create(itemDtoRequest1, owner.getId());

        ItemDtoResponse foundItem = itemService.findById(item.getId(), anotherUser.getId());

        Assertions.assertThat(foundItem.getName()).isEqualTo(item.getName());
        Assertions.assertThat(foundItem.getLastBooking()).isNull();
        Assertions.assertThat(foundItem.getNextBooking()).isNull();
    }

    @Test
    void findById_shouldFailIfItemNotFound() {
        Assertions.assertThatThrownBy(() -> itemService.findById(999L, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByUserId_shouldReturnItemsOfUser() {
        UserDto owner = userService.create(userDtoRequest1);
        itemService.create(itemDtoRequest1, owner.getId());
        itemService.create(itemDtoRequest2, owner.getId());

        List<ItemDtoResponse> items = itemService.findByUserId(owner.getId());

        Assertions.assertThat(items.size()).isEqualTo(2);
    }

    @Test
    void search_shouldReturnItemsByText() {
        UserDto owner = userService.create(userDtoRequest1);
        itemService.create(itemDtoRequest1, owner.getId());
        itemService.create(itemDtoRequest2, owner.getId());

        List<ItemDtoResponse> foundItems = itemService.search("description");

        Assertions.assertThat(foundItems.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void search_shouldReturnEmptyListWhenTextBlank() {
        List<ItemDtoResponse> foundItems = itemService.search(" ");

        Assertions.assertThat(foundItems).isEmpty();
    }

    @Test
    void addComment_shouldAddCommentWhenBookingExists() {
        UserDto owner = userService.create(userDtoRequest1);
        UserDto booker = userService.create(userDtoRequest2);

        ItemDtoResponse item = itemService.create(itemDtoRequest1, owner.getId());

        BookingDtoRequest bookingRequest = new BookingDtoRequest(
                item.getId(),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );
        BookingDtoResponse booking = bookingService.create(bookingRequest, booker.getId());
        bookingService.update(booking.getId(), true, owner.getId());

        CommentDtoRequest commentRequest = new CommentDtoRequest("Nice item!");
        itemService.addComment(commentRequest, item.getId(), booker.getId());

        ItemDtoResponse itemWithComments = itemService.findById(item.getId(), owner.getId());

        Assertions.assertThat(itemWithComments.getComments()).hasSize(1);
        Assertions.assertThat(itemWithComments.getComments().get(0).getText()).isEqualTo("Nice item!");
    }

    @Test
    void addComment_shouldFailWhenNoBooking() {
        UserDto owner = userService.create(userDtoRequest1);
        ItemDtoResponse item = itemService.create(itemDtoRequest1, owner.getId());

        CommentDtoRequest commentRequest = new CommentDtoRequest("Awesome!");

        Assertions.assertThatThrownBy(() -> itemService.addComment(commentRequest, item.getId(), owner.getId()))
                .isInstanceOf(ValidationException.class);
    }
}