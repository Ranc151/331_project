package proj.concert.service.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Entity class to represent concerts.
 *
 * A Concert describes a concert in terms of:
 * id           the unique identifier for a concert.
 * title        the concert's title.
 * dates        the concert's scheduled dates and times (represented as a Set of LocalDateTime instances).
 * imageName    an image name for the concert.
 * performers   the performers in the concert
 * blurb        the concert's description
 */

@Entity
@Table(name = "CONCERTS")
public class Concert {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    // in case of large text
    @Column(columnDefinition = "TEXT")
    private String blurb;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = @JoinColumn(name = "CONCERT_ID")
    )
    @Column(name = "DATE")
    @Fetch(FetchMode.SUBSELECT)
    private Set<LocalDateTime> dates = new HashSet<>();


    // lazy loaded.
    // Optimize by using sub-select.
    // Set the cascade Type to persist so that once we add performer, its correlated entity will be changed as well.
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "CONCERT_ID", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID", nullable = false))
    @Fetch(FetchMode.SUBSELECT)
    private Set<Performer> performers = new HashSet<>();

    public Concert() {
    }

    public Concert(Long id, String title, String imageName, String blurb) {
        this.id = id;
        this.title = title;
        this.imageName = imageName;
        this.blurb = blurb;
    }

    public Concert(String title, String imageName) {
        this.title = title;
        this.imageName = imageName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Set<LocalDateTime> getDates() {
        return dates;
    }

    public void setDates(Set<LocalDateTime> dates) {
        this.dates = dates;
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void setPerformers(Set<Performer> performers) {
        this.performers = performers;
    }

}