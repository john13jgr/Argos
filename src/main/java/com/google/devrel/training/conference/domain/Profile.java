package com.google.devrel.training.conference.domain;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.devrel.training.conference.form.ProfileForm.Sexo;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;


@Entity
public class Profile {
    String displayName;
    String mainEmail;
    String cedula;
    String telefono;
    String celular;
    String direccion;
    Sexo sexo;

    @Id String userId;

    private List<String> conferenceKeysToAttend = new ArrayList<>(0);

    public Profile(String displayName, String mainEmail,
			String cedula, String telefono,
			String celular, String direccion, Sexo sexo, String userId) {
		super();
		this.displayName = displayName;
		this.mainEmail = mainEmail;
		this.cedula = cedula;
		this.telefono = telefono;
		this.celular = celular;
		this.direccion = direccion;
		this.sexo = sexo;
		this.userId = userId;
	}
    
    public String getDisplayName() {
        return displayName;
    }

	public String getMainEmail() {
        return mainEmail;
    }

    public String getUserId() {
        return userId;
    }
    
    public String getCedula() {
		return cedula;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getCelular() {
		return celular;
	}

	public String getDireccion() {
		return direccion;
	}

	public Sexo getSexo() {
		return sexo;
	}

    public List<String> getConferenceKeysToAttend() {
        return ImmutableList.copyOf(conferenceKeysToAttend);
    }

    private Profile() {}

    public void update(String displayName, String cedula, String telefono,
    		String celular, String direccion, Sexo sexo) {
        if (displayName != null) {
            this.displayName = displayName;
        }
        if (cedula != null) {
            this.cedula = cedula;
        }
        if (telefono != null) {
            this.telefono = telefono;
        }
        if (celular != null) {
            this.celular = celular;
        }
        if (direccion != null) {
            this.direccion = direccion;
        }
        if (sexo != null) {
            this.sexo = sexo;
        }
    }

    public void addToConferenceKeysToAttend(String conferenceKey) {
        conferenceKeysToAttend.add(conferenceKey);
    }

    public void unregisterFromConference(String conferenceKey) {
        if (conferenceKeysToAttend.contains(conferenceKey)) {
            conferenceKeysToAttend.remove(conferenceKey);
        } else {
            throw new IllegalArgumentException("Conferencia inválida: " + conferenceKey);
        }
    }

}
