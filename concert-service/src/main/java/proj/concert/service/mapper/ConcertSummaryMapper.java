package proj.concert.service.mapper;

import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.service.domain.Concert;

public class ConcertSummaryMapper {
    public static ConcertSummaryDTO toDTO(Concert concert){
        return new ConcertSummaryDTO(
                concert.getId(),
                concert.getTitle(),
                concert.getImageName()
        );
    }
}
