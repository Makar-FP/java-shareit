package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingStorage extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(Long userId);

    @Query("select b from Booking as b where b.item.id in :itemIds")
    List<Booking> findAllByItemIdOrderByStartDesc(@Param("itemIds") List<Long> itemIds);

    List<Booking> findAllByItemId(Long itemId);

    List<Booking> findAllByBookerIdAndEndAfterOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    List<Booking> findAllByBookerIdAndItemIdAndEndBeforeOrderByStartDesc(Long userId, Long bookerId, LocalDateTime now);
}