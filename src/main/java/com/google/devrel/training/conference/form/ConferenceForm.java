package com.google.devrel.training.conference.form;

import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;

public class ConferenceForm {
    private String name;

    private String description;

    private List<String> topics;

    private String city;

    private Date startDate;
    
    private String hour;

    private int maxAttendees;

    private ConferenceForm() {}

    public ConferenceForm(String name, String description, List<String> topics, String city,
                          Date startDate, String hour, int maxAttendees) {
        this.name = name;
        this.description = description;
        this.hour = hour;
        this.topics = topics == null ? null : ImmutableList.copyOf(topics);
        this.city = city;
        this.startDate = startDate == null ? null : new Date(startDate.getTime());
        this.maxAttendees = maxAttendees;
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

	public List<String> getTopics() {
        return topics;
    }

    public String getCity() {
        return city;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }
}
