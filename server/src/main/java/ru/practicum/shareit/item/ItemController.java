package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDtoResponse> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestBody ItemDtoRequest itemDtoRequest) {
        return ResponseEntity.ok(itemService.create(itemDtoRequest, userId));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDtoResponse> addComment(@Valid @RequestBody CommentDtoRequest dto,
                                                         @PathVariable Long itemId,
                                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        CommentDtoResponse response = itemService.addComment(dto, itemId, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDtoResponse> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long itemId,
                                                  @RequestBody ItemDtoRequest itemDtoRequest) {
        return ResponseEntity.ok(itemService.update(itemDtoRequest, itemId, userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDtoResponse> findById(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.findById(itemId, userId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDtoResponse>> findByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.findByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDtoResponse>> search(@RequestParam String text) {
        return ResponseEntity.ok(itemService.search(text));
    }
}
