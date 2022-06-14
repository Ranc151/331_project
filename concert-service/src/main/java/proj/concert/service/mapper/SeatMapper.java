package proj.concert.service.mapper;

import proj.concert.common.dto.SeatDTO;
import proj.concert.service.domain.Seat;

public class SeatMapper {
    public static SeatDTO toDTO(Seat seat) {
        return new SeatDTO(
                seat.getLabel(),
                seat.getPrice()
        );
    }
}
