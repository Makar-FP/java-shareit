package ru.practicum.shareit.comment.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    String text;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @Column
    LocalDateTime created;
}