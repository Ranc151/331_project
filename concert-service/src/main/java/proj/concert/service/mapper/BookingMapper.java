package proj.concert.service.mapper;

import proj.concert.common.dto.BookingDTO;
import proj.concert.service.domain.Booking;

import java.util.stream.Collectors;

public class BookingMapper {
    public static BookingDTO toBookingDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO(
                booking.getConcertId(),
                booking.getDate(),
                booking.getSeats().stream().map(SeatMapper::toDTO).collect(Collectors.toList())
        );
        return bookingDTO;
    }
}
