package proj.concert.service.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a booking.
 * id          the unique identifier of a booking
 * concertId   the id of the concert which was booked (unidirectional association with class concert)
 * date        the date on which that concert was booked
 * seats       the seats which were booked for that concert on that date
 * user        the user who booked the seats
 */

@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue
    private long id;
    @Column(name = "CONCERT_ID")
    private long concertId;
    private LocalDateTime date;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Seat> seats = new ArrayList<>();

    // we don't want username and password being stored in memory, therefore we use proxy User
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public Booking() {
    }

    public Booking(long concertId, LocalDateTime date, List<Seat> seats, User user) {
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
        this.user = user;
    }

    public long getConcertId() {
        return concertId;
    }

    public void setConcertId(long concertId) {
        this.concertId = concertId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public long getId() {
        return this.id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}