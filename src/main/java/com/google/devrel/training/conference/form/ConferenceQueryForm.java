package com.google.devrel.training.conference.form;

import static com.google.devrel.training.conference.service.OfyService.ofy;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.collect.ImmutableList;
import com.google.devrel.training.conference.domain.Conference;

import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConferenceQueryForm {

    private static final Logger LOG = Logger.getLogger(ConferenceQueryForm.class.getName());

    public static enum FieldType {
        STRING, INTEGER
    }

    public static enum Field {
        CITY("city", FieldType.STRING),
        TOPIC("topics", FieldType.STRING),
        MONTH("month", FieldType.INTEGER),
        MAX_ATTENDEES("maxAttendees", FieldType.INTEGER);

        private String fieldName;

        private FieldType fieldType;

        private Field(String fieldName, FieldType fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }

        private String getFieldName() {
            return this.fieldName;
        }
    }

    public static enum Operator {
        EQ("=="),
        LT("<"),
        GT(">"),
        LTEQ("<="),
        GTEQ(">="),
        NE("!=");

        private String queryOperator;

        private Operator(String queryOperator) {
            this.queryOperator = queryOperator;
        }

        private String getQueryOperator() {
            return this.queryOperator;
        }

        private boolean isInequalityFilter() {
            return this.queryOperator.contains("<") || this.queryOperator.contains(">") ||
                    this.queryOperator.contains("!");
        }
    }

    public static class Filter {
        private Field field;
        private Operator operator;
        private String value;

        public Filter () {}

        public Filter(Field field, Operator operator, String value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }

        public Field getField() {
            return field;
        }

        public Operator getOperator() {
            return operator;
        }

        public String getValue() {
            return value;
        }
    }

    private List<Filter> filters = new ArrayList<>(0);

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Filter inequalityFilter;

    public ConferenceQueryForm() {}

    private void checkFilters() {
        for (Filter filter : this.filters) {
            if (filter.operator.isInequalityFilter()) {
                if (inequalityFilter != null && !inequalityFilter.field.equals(filter.field)) {
                    throw new IllegalArgumentException(
                            "Error");
                }
                inequalityFilter = filter;
            }
        }
    }

    public List<Filter> getFilters() {
        return ImmutableList.copyOf(filters);
    }

    public ConferenceQueryForm filter(Filter filter) {
        if (filter.operator.isInequalityFilter()) {
            if (inequalityFilter != null && !inequalityFilter.field.equals(filter.field)) {
                throw new IllegalArgumentException(
                        "Error");
            }
            inequalityFilter = filter;
        }
        filters.add(filter);
        return this;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Query<Conference> getQuery() {
        checkFilters();
        Query<Conference> query = ofy().load().type(Conference.class);
        if (inequalityFilter == null) {
            query = query.order("name");
        } else {
            query = query.order(inequalityFilter.field.getFieldName());
            query = query.order("name");
        }
        for (Filter filter : this.filters) {
            if (filter.field.fieldType == FieldType.STRING) {
                query = query.filter(String.format("%s %s", filter.field.getFieldName(),
                        filter.operator.getQueryOperator()), filter.value);
            } else if (filter.field.fieldType == FieldType.INTEGER) {
                query = query.filter(String.format("%s %s", filter.field.getFieldName(),
                        filter.operator.getQueryOperator()), Integer.parseInt(filter.value));
            }
        }
        LOG.info(query.toString());
        return query;
    }
}
