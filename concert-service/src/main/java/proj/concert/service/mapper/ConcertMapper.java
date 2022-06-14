package proj.concert.service.mapper;

import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ConcertMapper {
    public static ConcertDTO toDTO(Concert concert){
        ConcertDTO dto = new ConcertDTO();
        dto.setId(concert.getId());
        dto.setTitle(concert.getTitle());
        dto.setImageName(concert.getImageName());
        dto.setBlurb(concert.getBlurb());
        dto.setDates(new ArrayList<>(concert.getDates()));
        dto.setPerformers(
                new ArrayList<PerformerDTO>(
                        concert.getPerformers().
                                stream().map(PerformerMapper::toDTO).collect(Collectors.toList())));
        return dto;
    }
}
