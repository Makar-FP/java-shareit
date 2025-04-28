package ru.practicum.shareit.comment.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

@Repository
public interface CommentStorage extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(Long itemId);

    @Query("select c from Comment as c where c.item.id in :itemIds")
    List<Comment> findAllByItemId(@Param("itemIds") List<Long> itemIds);
}