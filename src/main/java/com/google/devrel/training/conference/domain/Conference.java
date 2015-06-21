package com.google.devrel.training.conference.domain;

import static com.google.devrel.training.conference.service.OfyService.ofy;
import com.googlecode.objectify.condition.IfNotDefault;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.devrel.training.conference.form.ConferenceForm;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
public class Conference {

    private static final String DEFAULT_CITY = "Default City";

    private static final List<String> DEFAULT_TOPICS = ImmutableList.of("Default", "Topic");

    @Id
    private long id;

    @Index
    private String name;

    private String description;

    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<Profile> profileKey;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String organizerUserId;

    @Index
    private List<String> topics;

    @Index(IfNotDefault.class) private String city;

    private Date startDate;

    private String hour;
    
    @Index
    private int month;

    @Index
    private int maxAttendees;

    @Index
    private int seatsAvailable;

    private Conference() {}

    public Conference(final long id, final String organizerUserId,
                      final ConferenceForm conferenceForm) {
        Preconditions.checkNotNull(conferenceForm.getName(), "El campo 'nombre' es obligatorio.");
        this.id = id;
        this.profileKey = Key.create(Profile.class, organizerUserId);
        this.organizerUserId = organizerUserId;
        updateWithConferenceForm(conferenceForm);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
    public String getHour() {
		return hour;
	}

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Profile> getProfileKey() {
        return profileKey;
    }

    public String getWebsafeKey() {
        return Key.create(profileKey, Conference.class, id).getString();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getOrganizerUserId() {
        return organizerUserId;
    }

    public String getOrganizerDisplayName() {
        Profile organizer = ofy().load().key(getProfileKey()).now();
        if (organizer == null) {
            return organizerUserId;
        } else {
            return organizer.getDisplayName();
        }
    }

    public List<String> getTopics() {
        return topics == null ? null : ImmutableList.copyOf(topics);
    }

    public String getCity() {
        return city;
    }
    
    public Date getStartDate() {
        return startDate == null ? null : new Date(startDate.getTime());
    }

    public int getMonth() {
        return month;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public void updateWithConferenceForm(ConferenceForm conferenceForm) {
        this.name = conferenceForm.getName();
        this.description = conferenceForm.getDescription();
        this.hour = conferenceForm.getHour();
        List<String> topics = conferenceForm.getTopics();
        this.topics = topics == null || topics.isEmpty() ? DEFAULT_TOPICS : topics;
        this.city = conferenceForm.getCity() == null ? DEFAULT_CITY : conferenceForm.getCity();

        Date startDate = conferenceForm.getStartDate();
        this.startDate = startDate == null ? null : new Date(startDate.getTime());
        if (this.startDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.startDate);
            this.month = calendar.get(calendar.MONTH) + 1;
        }
        int seatsAllocated = maxAttendees - seatsAvailable;
        if (conferenceForm.getMaxAttendees() < seatsAllocated) {
            throw new IllegalArgumentException(seatsAllocated + " seats are already allocated, "
                    + "but you tried to set maxAttendees to " + conferenceForm.getMaxAttendees());
        }
        this.maxAttendees = conferenceForm.getMaxAttendees();
        this.seatsAvailable = this.maxAttendees - seatsAllocated;
    }

    public void bookSeats(final int number) {
        if (seatsAvailable < number) {
            throw new IllegalArgumentException("There are no seats available.");
        }
        seatsAvailable = seatsAvailable - number;
    }

    public void giveBackSeats(final int number) {
        if (seatsAvailable + number > maxAttendees) {
            throw new IllegalArgumentException("The number of seats will exceeds the capacity.");
        }
        seatsAvailable = seatsAvailable + number;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Id: " + id + "\n")
                .append("Name: ").append(name).append("\n");
        if (city != null) {
            stringBuilder.append("City: ").append(city).append("\n");
        }
        if (topics != null && topics.size() > 0) {
            stringBuilder.append("Topics:\n");
            for (String topic : topics) {
                stringBuilder.append("\t").append(topic).append("\n");
            }
        }
        if (startDate != null) {
            stringBuilder.append("StartDate: ").append(startDate.toString()).append("\n");
        }
        stringBuilder.append("Max Attendees: ").append(maxAttendees).append("\n");
        return stringBuilder.toString();
    }

}
