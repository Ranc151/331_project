package proj.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import proj.concert.common.types.Genre;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


/**
 * Entity class to represent performers.
 *
 * A Performer describes a performer in terms of:
 * id         the unique identifier for a performer.
 * name       the performer's name.
 * imageName  the name of an image file for the performer.
 * genre      the performer's genre.
 * blurb      the performer's description.
 * concerts   the concerts that the performer has attended or going to attend
 */

@Entity
@Table(name = "PERFORMERS")
public class Performer implements Comparable<Performer> {

    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "VARCHAR(40)")
    private String name;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    // in case of large text
    @Column(columnDefinition = "TEXT")
    private String blurb;


    // lazy loading as we don't always require concerts information.
    // remove cascade so that if we remove a performer, it will also remove them from the association entity
    @ManyToMany(
            fetch = FetchType.LAZY,
            mappedBy = "performers",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
    )
    private Set<Concert> concerts = new HashSet<>();

    public Performer() {
    }

    public Performer(Long id, String name, String imageName, Genre genre, String blurb) {
        this.id = id;
        this.name = name;
        this.imageName = imageName;
        this.genre = genre;
        this.blurb = blurb;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Performer that = (Performer) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(name, that.name)
                .append(imageName, that.imageName)
                .append(genre, that.genre)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(imageName)
                .append(genre)
                .toHashCode();
    }

    @Override
    public int compareTo(Performer other) {
        return other.getName().compareTo(getName());
    }
}