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
    List<Booking> findAllByUserId(Long userId);

    @Query("select b from Booking as b where b.item.id in :itemIds")
    List<Booking> findAllByItemIdOrderByStartDesc(@Param("itemIds") List<Long> itemIds);

    List<Booking> findAllByItemId(Long itemId);

    List<Booking> findAllByUserIdAndEndAfterOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByUserIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByUserIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByUserIdAndStatusOrderByStartDesc(Long userId, BookingStatus status);

    List<Booking> findAllByUserIdAndItemIdAndEndBeforeOrderByStartDesc(Long userId, Long itemId, LocalDateTime now);
}