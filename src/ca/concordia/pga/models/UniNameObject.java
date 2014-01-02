package ca.concordia.pga.models;

/**
 * The class represents an object with a unique name
 * 
 * @author Ludeng Zhao(Eric)
 * 
 */
public class UniNameObject extends Object implements Comparable<UniNameObject>{

	private String name;//ID

	UniNameObject(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (UniNameObject.class.isAssignableFrom(o.getClass())) {
			System.out.println("Inside sex");
			UniNameObject n = (UniNameObject) o;
			return this.name.equals(n.getName());
		} else {
			return this.toString().equals(o);
		}
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public String toString(){
		return name;
	}

	@Override
	public int compareTo(UniNameObject o) {
		/**
		 * for WSC
		 */
		Integer v1 = Integer.parseInt(this.name.replaceAll("\\D+", ""));
		Integer v2 = Integer.parseInt(o.name.replaceAll("\\D+", ""));
	
		/**
		 * for ICSOC experiments
		 */
//		Integer v1 = 0;
//		Integer v2 = 1;
		
		return v1.compareTo(v2);
	}


}
