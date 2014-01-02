package ca.concordia.pga.models;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

/**
 * The class represents a planning graph
 * 
 * 
 */
public class PlanningGraph {

	private Vector<Set<Concept>> PLevels;
	private Vector<Set<Service>> ALevels;
	private Set<Concept> goalSet;
	private Set<Concept> givenConceptSet;
	public static LinkedHashSet<LinkedHashSet<Service>> actionLevels = new LinkedHashSet<LinkedHashSet<Service>>();
	public static LinkedHashSet<LinkedHashSet<Concept>> propLevels = new LinkedHashSet<LinkedHashSet<Concept>>();
	public PlanningGraph() {
		PLevels = new Vector<Set<Concept>>();
		ALevels = new Vector<Set<Service>>();
		goalSet = new HashSet<Concept>();
		givenConceptSet = new HashSet<Concept>();
	}

	public Vector<Set<Concept>> getPLevels() {
		return PLevels;
	}

	public Vector<Set<Service>> getALevels() {
		return ALevels;
	}

	public Set<Concept> getPLevel(int index) {
		return this.PLevels.get(index);
	}

	public Set<Service> getALevel(int index) {
		return this.ALevels.get(index);
	}
	
	public void setALevel(int index, Set<Service> ALevel){
		this.ALevels.set(index, ALevel);
	}

	public void addPLevel(Set<Concept> level) {
		this.PLevels.add(level);
	}

	public void addALevel(Set<Service> level) {
		this.ALevels.add(level);
	}

		public Set<Concept> getGoalSet() {
		return goalSet;
	}

	public void setGoalSet(Set<Concept> goalSet) {
		this.goalSet = goalSet;
	}
	
	public Set<Concept> getGivenConceptSet() {
		return givenConceptSet;
	}

	public void setGivenConceptSet(Set<Concept> givenConceptSet) {
		this.givenConceptSet = givenConceptSet;
	}	

}
