package ru.practicum.shareit.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public Comment mapDtoToComment(CommentDtoRequest dto, User owner, Item item) {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setText(dto.getText());
        comment.setOwner(owner);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public CommentDtoResponse mapCommentToDto(Comment comment) {
        CommentDtoResponse dto = new CommentDtoResponse();
        dto.setId(comment.getId());
        dto.setItem(comment.getItem());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getOwner().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}
