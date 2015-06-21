package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.factory;
import static com.google.devrel.training.conference.service.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Conference;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.form.ConferenceForm;
import com.google.devrel.training.conference.form.ConferenceQueryForm;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.Sexo;
//import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

@Api(name = "conference", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = {
        Constants.WEB_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID },
        description = "Aplicación para el manejo de la base de datos en Argos Logística.")
public class ConferenceApi {

    private static final Boolean True = null;
    private static final Boolean False = null;

    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
    public Profile saveProfile(final User user, ProfileForm profileForm)
            throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Se requiere iniciar sesión");
        }

        String mainEmail = user.getEmail();
        String userId = user.getUserId();

        String displayName = profileForm.getDisplayName();
        String cedula = profileForm.getCedula();
        String telefono = profileForm.getTelefono();
        String celular = profileForm.getCelular();
        String direccion = profileForm.getDireccion();
        Sexo sexo = profileForm.getSexo();

        Profile profile = ofy().load().key(Key.create(Profile.class, userId))
                .now();

        if (profile == null) {
            if (displayName == null) {
                displayName = extractDefaultDisplayNameFromEmail(user
                        .getEmail());
            }
            if (sexo == null) {
            	sexo = Sexo.NOT_SPECIFIED;
	        }
            profile = new Profile(displayName, mainEmail, cedula, telefono, celular, direccion, sexo, userId);
        } else {
        	profile.update(displayName, cedula, telefono, celular, direccion, sexo);
        }

        ofy().save().entity(profile).now();

        return profile;
    }

    
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Se requiere iniciar sesión");
        }

        String userId = user.getUserId();
        Key key = Key.create(Profile.class, userId);

        Profile profile = (Profile) ofy().load().key(key).now();
        return profile;
    }

    private static Profile getProfileFromUser(User user) {
        Profile profile = ofy().load().key(
                Key.create(Profile.class, user.getUserId())).now();
        if (profile == null) {
            String email = user.getEmail();
            profile = new Profile(extractDefaultDisplayNameFromEmail(email), email,
            		"", "", "", "", Sexo.NOT_SPECIFIED, user.getUserId());
        }
        return profile;
    }

    @ApiMethod(name = "createConference", path = "conference", httpMethod = HttpMethod.POST)
    public Conference createConference(final User user, final ConferenceForm conferenceForm)
        throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Se requiere iniciar sesión");
        }

        String userId = user.getUserId();

        Key<Profile> profileKey = Key.create(Profile.class, userId);

        final Key<Conference> conferenceKey = factory().allocateId(profileKey, Conference.class);

        final long conferenceId = conferenceKey.getId();

        Profile profile = getProfileFromUser(user);

        Conference conference = new Conference(conferenceId, userId, conferenceForm);

        ofy().save().entities(conference, profile).now();

         return conference;
         }


    @ApiMethod(
            name = "queryConferences_nofilters",
            path = "queryConferences_nofilters",
            httpMethod = HttpMethod.POST
    )
    public List<Conference> queryConferences_nofilters() {
        Query<Conference> query = ofy().load().type(Conference.class).order("name");

        return query.list();
    }

    @ApiMethod(
            name = "queryConferences",
            path = "queryConferences",
            httpMethod = HttpMethod.POST
    )
    public List<Conference> queryConferences(ConferenceQueryForm conferenceQueryForm) {
        Iterable<Conference> conferenceIterable = conferenceQueryForm.getQuery();
        List<Conference> result = new ArrayList<>(0);
        List<Key<Profile>> organizersKeyList = new ArrayList<>(0);
        for (Conference conference : conferenceIterable) {
            organizersKeyList.add(Key.create(Profile.class, conference.getOrganizerUserId()));
            result.add(conference);
        }
        ofy().load().keys(organizersKeyList);
        return result;
    }


    public static class WrappedBoolean {

        private final Boolean result;
        private final String reason;

        public WrappedBoolean(Boolean result) {
            this.result = result;
            this.reason = "";
        }

        public WrappedBoolean(Boolean result, String reason) {
            this.result = result;
            this.reason = reason;
        }

        public Boolean getResult() {
            return result;
        }

        public String getReason() {
            return reason;
        }
    }

    @ApiMethod(
            name = "getConference",
            path = "conference/{websafeConferenceKey}",
            httpMethod = HttpMethod.GET
    )
    public Conference getConference(
            @Named("websafeConferenceKey") final String websafeConferenceKey)
            throws NotFoundException {
        Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
        Conference conference = ofy().load().key(conferenceKey).now();
        if (conference == null) {
            throw new NotFoundException("Error en el evento: " + websafeConferenceKey);
        }
        return conference;
    }

    @ApiMethod(
            name = "registerForConference",
            path = "conference/{websafeConferenceKey}/registration",
            httpMethod = HttpMethod.POST
    )

    public WrappedBoolean registerForConference(final User user,
            @Named("websafeConferenceKey") final String websafeConferenceKey)
            throws UnauthorizedException, NotFoundException,
            ForbiddenException, ConflictException {
        if (user == null) {
            throw new UnauthorizedException("Se requiere iniciar sesión");
        }

        final String userId = user.getUserId();

        WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
            @Override
            public WrappedBoolean run() {
                try {

                Key<Conference> conferenceKey = Key.create(websafeConferenceKey);

                Conference conference = ofy().load().key(conferenceKey).now();

                if (conference == null) {
                    return new WrappedBoolean (false,
                            "Error en el evento: "
                                    + websafeConferenceKey);
                }

                Profile profile = getProfileFromUser(user);

                if (profile.getConferenceKeysToAttend().contains(
                        websafeConferenceKey)) {
                    return new WrappedBoolean (false, "registrado");
                } else if (conference.getSeatsAvailable() <= 0) {
                    return new WrappedBoolean (false, "cupos");
                } else {
                    profile.addToConferenceKeysToAttend(websafeConferenceKey);
                    conference.bookSeats(1);

                    ofy().save().entities(profile, conference).now();
                    return new WrappedBoolean(true, "Registro realizado con éxito");
                }

                }
                catch (Exception e) {
                    return new WrappedBoolean(false, "Unknown exception");

                }
            }
        });
        
        if (!result.getResult()) {
            if (result.getReason().contains("Error en el evento")) {
                throw new NotFoundException (result.getReason());
            }
            else if (result.getReason() == "registrado") {
                throw new ConflictException("Ya se encuentra registrado");
            }
            else if (result.getReason() == "cupos") {
                throw new ConflictException("No hay cupos disponibles");
            }
            else {
                throw new ForbiddenException("Unknown exception");
            }
        }
        return result;
    }


    @ApiMethod(
            name = "unregisterFromConference",
            path = "conference/{websafeConferenceKey}/registration",
            httpMethod = HttpMethod.DELETE
    )
    public WrappedBoolean unregisterFromConference(final User user,
                                            @Named("websafeConferenceKey")
                                            final String websafeConferenceKey)
            throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
        if (user == null) {
            throw new UnauthorizedException("Se requiere iniciar sesión");
        }

        WrappedBoolean result = ofy().transact(new Work<WrappedBoolean>() {
            @Override
            public WrappedBoolean run() {
                Key<Conference> conferenceKey = Key.create(websafeConferenceKey);
                Conference conference = ofy().load().key(conferenceKey).now();
                if (conference == null) {
                    return new  WrappedBoolean(false,
                            "Error en el evento: " + websafeConferenceKey);
                }

                Profile profile = getProfileFromUser(user);
                if (profile.getConferenceKeysToAttend().contains(websafeConferenceKey)) {
                    profile.unregisterFromConference(websafeConferenceKey);
                    conference.giveBackSeats(1);
                    ofy().save().entities(profile, conference).now();
                    return new WrappedBoolean(true);
                } else {
                    return new WrappedBoolean(false, "No se encuentra registrado en este evento");
                }
            }
        });

        if (!result.getResult()) {
            if (result.getReason().contains("Error en la búsqueda")) {
                throw new NotFoundException (result.getReason());
            }
            else {
                throw new ForbiddenException(result.getReason());
            }
        }
        return new WrappedBoolean(result.getResult());
    }

    @ApiMethod(
            name = "getConferencesToAttend",
            path = "getConferencesToAttend",
            httpMethod = HttpMethod.GET
    )
    public Collection<Conference> getConferencesToAttend(final User user)
            throws UnauthorizedException, NotFoundException {
        if (user == null) {
            throw new UnauthorizedException("Se requiere iniciar sesión");
        }
        Profile profile = ofy().load().key(Key.create(Profile.class, user.getUserId())).now();
        if (profile == null) {
            throw new NotFoundException("El perfil no existe.");
        }
        List<String> keyStringsToAttend = profile.getConferenceKeysToAttend();
        List<Key<Conference>> keysToAttend = new ArrayList<>();
        for (String keyString : keyStringsToAttend) {
            keysToAttend.add(Key.<Conference>create(keyString));
        }
        return ofy().load().keys(keysToAttend).values();
    }

}
