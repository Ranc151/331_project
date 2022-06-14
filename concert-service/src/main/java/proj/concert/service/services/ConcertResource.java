package proj.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proj.concert.common.dto.*;
import proj.concert.service.domain.*;
import proj.concert.service.mapper.*;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

import proj.concert.common.types.BookingStatus;
import proj.concert.service.jaxrs.LocalDateTimeParam;
import java.time.LocalDateTime;

class ConcertSubscription {
    public ConcertInfoSubscriptionDTO info;
    public AsyncResponse sub;

    public ConcertSubscription(ConcertInfoSubscriptionDTO info, AsyncResponse sub) {
        this.info = info;
        this.sub = sub;
    }
}

@Path("/concert-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConcertResource {
    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);
    private static final String AUTH_COOKIE = "auth";
    private final List<ConcertSubscription> concertSubscriptions = new Vector<>();

    /*
     GET /concerts/{id}
     Retrieves a Concert based on its unique id.
     The HTTP response message has a status code of either 200 or 404, depending on whether the specified Concert is found.
     */
    @GET
    @Path("/concerts/{id}")
    public Response getConcertById(@PathParam("id") Long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            LOGGER.info("Retrieving concert ID=" + id);
            Concert concert = em.find(Concert.class, id);
            em.getTransaction().commit();

            if (concert == null) {
                LOGGER.debug("Concert ID=" + id + " does not exist.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(ConcertMapper.toDTO(concert)).build();
        } finally {
            em.close();
        }
    }

    /*
     GET /concerts
     Retrieves all Concerts.
     The HTTP response message has a status code of 200.
     */
    @GET
    @Path("/concerts")
    public Response retrieveAllConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            LOGGER.info("Retrieving all concerts...");
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            em.getTransaction().commit();
            List<ConcertDTO> resultList = new ArrayList<>(concerts.stream().map(ConcertMapper::toDTO).collect(Collectors.toList()));
            return Response
                    .ok(resultList)
                    .build();
        } finally {
            em.close();
        }
    }

    /*
     GET /concerts/summaries
     Retrieves the Concert summaries.
     The HTTP response has a status code of either 200 or 404, depending on whether the concert summaries are found.
     */
    @GET
    @Path("/concerts/summaries")
    public Response retrieveAllConcertsSummaries(){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            LOGGER.info("Retrieving concerts summaries...");
            // get the concerts
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            em.getTransaction().commit();
            // map concerts to ConcertSummaryDTO
            List<ConcertSummaryDTO> resultList = new ArrayList<>(concerts.stream().map(ConcertSummaryMapper::toDTO).collect(Collectors.toList()));
            return Response
                    .ok(resultList)
                    .build();
        } finally {
            em.close();
        }
    }

    /*
     GET /performers
     Retrieves all Performers.
     The HTTP response message has a status code of 200.
     */
    @GET
    @Path("/performers")
    public Response retrieveAllPerformers() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            LOGGER.info("Retrieving all performers...");
            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
            List<Performer> performers = performerQuery.getResultList();
            em.getTransaction().commit();
            List<PerformerDTO> resultList = new ArrayList<>(performers.stream().map(PerformerMapper::toDTO).collect(Collectors.toList()));
            return Response
                    .ok(resultList)
                    .build();
        } finally {
            em.close();
        }
    }

    /*
     GET /performers/{id}
     Retrieves a Performers based on its unique id.
     The HTTP response message has a status code of either 200 or 404, depending on whether the specified Concert is found.
     */
    @GET
    @Path("/performers/{id}")
    public Response getPerformerById(@PathParam("id") Long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            LOGGER.info("Retrieving performer ID=" + id);
            // use em.find() to get matched performers base on unique id
            Performer performer = em.find(Performer.class, id);
            em.getTransaction().commit();

            // return not found when no matched
            if (performer == null) {
                LOGGER.debug("Performer ID=" + id + " does not exist.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            return Response.ok(PerformerMapper.toDTO(performer)).build();
        } finally {
            em.close();
        }
    }

    /*
     GET /bookings
     Retrieves all booking from a specific user.
     The HTTP response message has a status code of either 200 or 401, depending on whether the user is authenticated or not.
     */
    @GET
    @Path("/bookings")
    public Response retrieveAllBookings(@CookieParam(AUTH_COOKIE) Cookie authCookie) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        User user = getUser(em, authCookie);
        if (user == null) {
            em.close();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        LOGGER.info("Retrieving all bookings...");
        List<Booking> bookings = em.createQuery("select b from Booking b where b.user=:user", Booking.class)
                .setParameter("user", user)
                .getResultList();

        List<BookingDTO> resultList = new ArrayList<>(bookings.stream().map(BookingMapper::toBookingDTO).collect(Collectors.toList()));
        em.close();
        return Response
                .ok(resultList)
                .build();
    }

    /*
     POST /bookings
     Attempts a booking.
     The HTTP response message has a status code of either 201, 400, 401, 403 depending on whether the user is authenticated,
     if the concert or date is wrong, or if the number of seats requested don't exist.
     */
    @POST
    @Path("/bookings")
    public Response createBooking(@CookieParam(AUTH_COOKIE) Cookie authCookie, BookingRequestDTO bookingDto) {
        // This has no protection against double booking as far as I am aware, would appreciate some guidance on how to implement that.
        EntityManager em = PersistenceManager.instance().createEntityManager();
        User user = getUser(em, authCookie);

        if (user == null) {
            em.close();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        em.getTransaction().begin();

        Concert concert = em.find(Concert.class, bookingDto.getConcertId());

        if (concert == null) {
            em.getTransaction().rollback();
            em.close();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!concert.getDates().contains(bookingDto.getDate())) {
            em.getTransaction().rollback();
            em.close();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        List<Seat> seats = em.createQuery("select s from Seat s where s.label in (:seatLabels) and s.date=:date", Seat.class)
                .setParameter("seatLabels", bookingDto.getSeatLabels())
                .setParameter("date", bookingDto.getDate())
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();

        for(Seat seat : seats) {
            if (seat.isBooked()) {
                em.getTransaction().rollback();
                em.close();
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        LOGGER.info("Booking seats...");

        for(Seat seat : seats) {
            seat.setIsBooked(true);
        }

        Booking booking = new Booking(bookingDto.getConcertId(), bookingDto.getDate(), seats, user);

        em.persist(booking);
        em.getTransaction().commit();

        em.getTransaction().begin();
        issueNotifications(em, bookingDto.getConcertId(), bookingDto.getDate());
        em.getTransaction().commit();

        em.close();
        return Response.created(URI.create("/concert-service/bookings/" + booking.getId())).build();
    }

    /*
     GET /bookings/{id}
     Retrieves a booking based on its unique id.
     The HTTP response message has a status code of either 200, 403, or 404, depending on whether the booking is found or if the user is authenticated.
     */
    @GET
    @Path("/bookings/{id}")
    public Response getBookingById(@CookieParam(AUTH_COOKIE) Cookie authCookie, @PathParam("id") Long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        User user = getUser(em, authCookie);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Booking booking;

        try {
            booking = em.find(Booking.class, id);
        } catch (javax.persistence.NoResultException e) {
            em.close();
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (booking.getUser().getUsername() != user.getUsername()) {
            em.close();
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        em.close();

        return Response.ok(BookingMapper.toBookingDTO(booking)).build();

    }

    /*
     GET /seats/{date}
     Retrieves specified seats on a particular date.
     The HTTP response message has a status code of either 200.
     */
    @GET
    @Path("/seats/{time}")
    public Response getSeatByTime(@PathParam("time") LocalDateTimeParam time, @QueryParam("status")BookingStatus status){
        EntityManager em = PersistenceManager.instance().createEntityManager();

        em.getTransaction().begin();
        LocalDateTime date = time.getLocalDateTime();
        List<Seat> seats = new ArrayList<>();
        LOGGER.info("Retrieving seats...");
        List<Seat> unfilteredSeats = getSeats(em, date);
        // if status is Any return all the seats
        if (status == BookingStatus.Any){
            seats = unfilteredSeats;
        }
        else {
            boolean booked = status == BookingStatus.Booked;
            // add booked seats to List
            for(Seat seat : unfilteredSeats) {
                if (seat.isBooked() == booked) {
                    seats.add(seat);
                }
            }
        }

        em.getTransaction().commit();
        em.close();

        // map the selected seats to SeatDTO
        List<SeatDTO> resultList = new ArrayList<>(seats.stream().map(SeatMapper::toDTO).collect(Collectors.toList()));
        return Response
                .ok(resultList)
                .build();
    }

    /*
     POST /login
     Logs the user into the database.
     If the user doesn't exist in the database, then there will be a status of 401 returned.
     */
    @POST
    @Path("/login")
    public Response login(UserDTO userDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :username and u.password = :password", User.class)
                    .setParameter("username", userDTO.getUsername())
                    .setParameter("password", userDTO.getPassword())
                    .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            User user = userQuery.getSingleResult();
            UUID sessionId = UUID.randomUUID();
            try {
                user.setSessionId(sessionId);
            } catch (Exception e) {
                LOGGER.error(e.toString());
            }
            NewCookie newCookie = new NewCookie(AUTH_COOKIE, sessionId.toString());
            LOGGER.info("Generated auth cookie for the new user " + user.getUsername());
            em.getTransaction().commit();
            return Response.ok(user).cookie(newCookie).build();
        } catch (javax.persistence.NoResultException e) {
            em.getTransaction().rollback();
            // check whether the user credential is correct or not
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } finally {
            em.close();
        }
    }

    /*
     POST /subscribe/concertInfo
     Subscribes a user to a concert and will be notified if the booked seat capacity exceeds a certain percentage.
     The HTTP response message has a status code of either 200, 400, or 403, depending on whether the user is authenticated,
     if the concert exists in the database and the date is correct.
     */
    @POST
    @Path("subscribe/concertInfo")
    public void subscribeToConcert(@CookieParam(AUTH_COOKIE) Cookie authCookie, @Suspended AsyncResponse sub, ConcertInfoSubscriptionDTO info) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        User user = getUser(em, authCookie);
        if (user == null) {
            em.close();
            sub.resume(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        Concert concert = em.find(Concert.class, info.getConcertId());

        if (concert == null) {
            em.close();
            sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }

        if (!concert.getDates().contains(info.getDate())) {
            em.close();
            sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }

        em.close();

        concertSubscriptions.add(new ConcertSubscription(info, sub));
    }

    // --------------HELPER METHODS--------------
    // The client sends a login request with credentials to the backend server.
    //
    // The server then validates the credentials. If the login is successful, the web server will create a session in the database and include a Set-Cookie header on the response containing a unique ID in the cookie object.
    //
    // The browser saves the cookie locally. As long as the user stays logged in, the client needs to send the cookie in all the subsequent requests to the server. The server then compares the session ID stored in the cookie against the one in the database to verify the validity.
    //
    // During the logout operation, the server will make the cookie expire by deleting it from the database.
    private User getUser(EntityManager em, Cookie cookie) {
        if (cookie == null) {
            return null;
        }

        try {
            User user = em.createQuery("select user from User user where user.sessionId = :sessionId", User.class)
                .setParameter("sessionId", UUID.fromString(cookie.getValue()))
                .getSingleResult();

            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private List<Seat> getSeats(EntityManager em, LocalDateTime date) {
        List<Seat> seats = em.createQuery("select s from Seat s where s.date=:date", Seat.class)
                .setParameter("date", date)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .getResultList();
        return seats;
    }

    private void issueNotifications(EntityManager em, Long concertId, LocalDateTime date) {
        LOGGER.info("Issuing notifications...");
        List<Seat> seats = getSeats(em, date);
        int numberOfBookedSeats = 0;

        for(Seat seat : seats) {
            if (seat.isBooked()) {
                numberOfBookedSeats++;
            }
        }

        float percentageOfSeatsBooked = ((float)numberOfBookedSeats / (float)seats.size()) * 100f;

        ConcertInfoNotificationDTO notification = new ConcertInfoNotificationDTO(seats.size() - numberOfBookedSeats);
        for (ConcertSubscription subscription : concertSubscriptions) {
            if (
                    subscription.info.getConcertId() == concertId &&
                    subscription.info.getDate().equals(date) &&
                    percentageOfSeatsBooked >= subscription.info.getPercentageBooked()
            ) {
                subscription.sub.resume(Response.ok(notification).build());
            }
        }
    }
}
