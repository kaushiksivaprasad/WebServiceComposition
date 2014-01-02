package ca.concordia.pga.models;

/**
 * The class represents semantic thing in taxonomy document
 * 
 * 
 */
public class Thing extends UniNameObject {

	private String type;//parant class 

	public Thing(String name) {
		super(name);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
