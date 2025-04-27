package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok(itemService.create(itemDto, userId));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDtoResponse> addComment(@Valid @RequestBody CommentDtoRequest dto,
                                                         @PathVariable Long itemId,
                                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        CommentDtoResponse response = itemService.addComment(dto, itemId, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId,
                                          @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok(itemService.update(itemDto, itemId, userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> findById(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.findById(itemId, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> findByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.findByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {
        return ResponseEntity.ok(itemService.search(text));
    }
}
