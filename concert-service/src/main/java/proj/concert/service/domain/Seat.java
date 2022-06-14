package proj.concert.service.domain;


import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Entity class to represent a Seat at the concert venue.
 * id         the unique identifier generated on addition
 * isBooked   state that specifies whether the seat is booked or not
 * date       the date on which that concert was booked
 * label      the seat label
 * price      the price that it costs to book the seat
 */

@Entity
@Table(name="SEATS")
public class Seat {

	@Id
	@GeneratedValue
	private long id;

	@Column()
	private boolean isBooked;

	@Column()
	private LocalDateTime date;

	@Column()
	private String label;

	@Column()
	private BigDecimal price;

	public Seat() {
	}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal price) {
		this.label = label;
		this.isBooked = isBooked;
		this.date = date;
		this.price = price;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public void setIsBooked(boolean isBooked) {
		this.isBooked = isBooked;
	}

	public LocalDateTime getDate() {
		return this.date;
	}

	public void setDate() {
		this.date = date;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		Seat seat = (Seat) o;

		return new EqualsBuilder()
				.append(label, seat.label)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(label)
				.toHashCode();
	}

}