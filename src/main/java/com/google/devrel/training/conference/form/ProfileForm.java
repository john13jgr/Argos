package com.google.devrel.training.conference.form;

public class ProfileForm {

	private String displayName;
    private String cedula;
    private String telefono;
    private String celular;
    private String direccion;
    private Sexo sexo;

    private ProfileForm () {}

    public ProfileForm(String displayName, String cedula, String telefono,
			String celular, String direccion, Sexo sexo) {
		super();
		this.displayName = displayName;
		this.cedula = cedula;
		this.telefono = telefono;
		this.celular = celular;
		this.direccion = direccion;
		this.sexo = sexo;
	}
    
    public String getDisplayName() {
        return displayName;
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
    
    public static enum Sexo {
    	NOT_SPECIFIED,
        MASCULINO,
        FEMENINO
    }
}
