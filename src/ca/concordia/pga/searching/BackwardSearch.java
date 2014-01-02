package ca.concordia.pga.searching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import ca.concordia.pga.main.AutomatedMain;
import ca.concordia.pga.models.Concept;
import ca.concordia.pga.models.Param;
import ca.concordia.pga.models.PlanningGraph;
import ca.concordia.pga.models.Service;

public class BackwardSearch {
	public static ArrayList<String> bannedServices = null;
	public static LinkedList<String> servicesConsumed = null;
	public static Vector<Set<Concept>> goalSet = null;
	public static ArrayList<String> tempUselessService = null;
	public static LinkedList<String> tempServicesConsumed = null;
	public static LinkedList<LinkedList<String>> historicalServicesConsumed = null;
	public static Set<Concept> goalSetMod = null;

	public static void doBackwardSearch(PlanningGraph pg, Map<String, Service> serviceMap)
	{
		Set<Concept> inputSet = pg.getGoalSet();
		servicesConsumed = new LinkedList<String>();
		goalSet = pg.getPLevels();
		goalSetMod = goalSet.get(0);
		Map<String, Service> tempServices = new HashMap<String, Service>();
		tempServices.putAll(serviceMap);
		System.out.println(expandGraph(inputSet, tempServices, goalSetMod));
		System.out.println(servicesConsumed);
		System.out.println(servicesConsumed.size());
	}

	public static boolean expandGraph(Set<Concept> inputSet, Map<String, Service> serviceMap, Set<Concept> goalSet)
	{
		if (compareSets(inputSet, goalSet))
		{
			return true;
		}

		boolean inputPresent = false;
		String serviceNameToBeTried = "";
		Map<String, Service> tempServices = serviceMap;
		addParentConcepts(inputSet);
		Set<Service> servicesToBeTried = getPossibleWSToBeConsumed(inputSet, tempServices);
		System.out.println(servicesToBeTried);
		System.out.println("Level Size: " + servicesToBeTried.size());
		Set<Concept> outputSet = new HashSet<Concept>();
		int count = 0;
		for (Service service : servicesToBeTried)
		{
			serviceNameToBeTried = service.getName();
			servicesConsumed.add(serviceNameToBeTried);
			outputSet = new HashSet<Concept>();
			outputSet.addAll(serviceMap.get(serviceNameToBeTried).getInputConceptSet());
			addParentConcepts(outputSet);
			int prevSize = inputSet.size();
			addToSet(inputSet, outputSet);
			if (compareSets(inputSet, goalSet))
			{
				return true;
			}
			if (inputSet.size() == prevSize)
			{
				count++;
				servicesConsumed.remove(serviceNameToBeTried);
				serviceMap.remove(serviceNameToBeTried);
			}
			inputPresent = checkOtherInputsOfWS(inputSet, serviceMap);
			if (!inputPresent)
			{
				count++;
				tempServices.remove(serviceNameToBeTried);
				servicesConsumed.remove(serviceNameToBeTried);
				removeFromSet(inputSet, outputSet);
			}
		}
		if (count != servicesToBeTried.size())
		{
			for (String serviceCon : servicesConsumed)
			{
				tempServices.remove(serviceCon);
			}
			if (expandGraph(inputSet, serviceMap, goalSet))
			{
				return true;
			}
			else
			{
				if (servicesConsumed.size() == serviceMap.size())
				{
					System.out.println("No other services remaining to process the inputs.");
				}
				return false;
			}
		}
		return false;
	}

	public static boolean compareSets(Set<Concept> inputSet, Set<Concept> goalSet)
	{
		if (inputSet.containsAll(goalSet))
			return true;
		return false;
	}

	public static void removeFromSet(Set<Concept> srcSet, Concept objToBeRemoved)
	{
		Iterator<Concept> iter = srcSet.iterator();
		while (iter.hasNext())
		{
			Concept c = iter.next();
			if (objToBeRemoved.getName().equalsIgnoreCase(c.getName()))
			{
				iter.remove();
				break;
			}
		}
	}

	public static void removeFromSet(Set<Concept> srcSet, Set<Concept> setToBeRemoved)
	{
		for (Concept objToBeRemoved : setToBeRemoved)
		{
			Iterator<Concept> iter = srcSet.iterator();
			while (iter.hasNext())
			{
				Concept c = iter.next();
				if (objToBeRemoved.getName().equalsIgnoreCase(c.getName()))
				{
					iter.remove();
					break;
				}
			}
		}
	}

	public static void addToSet(Set<Concept> tgtSet, Set<Concept> srcSet)
	{
		Iterator<Concept> iter = srcSet.iterator();
		while (iter.hasNext())
		{
			Concept c = iter.next();
			boolean found = false;
			for (Concept temp : tgtSet)
			{
				if (temp.getName().equalsIgnoreCase(c.getName()))
				{
					found = true;
					iter.remove();
					break;
				}
			}
			if (!found)
			{
				tgtSet.add(c);
			}
		}
	}

	public static boolean checkOtherInputsOfWS(Set<Concept> serviceIpt, Map<String, Service> serviceMap)
	{
		if (!compareSets(serviceIpt, goalSetMod))
		{
			for (Entry<String, Service> service : serviceMap.entrySet())
			{
				int tempCount = 0;
				String serviceNameToBeTried = service.getKey();
				if (!servicesConsumed.contains(serviceNameToBeTried))
				{
					Set<Param> paramSet = service.getValue().getOutputParamSet();
					for (Param param : paramSet)
					{
						Concept dummy = AutomatedMain.conceptMap.get(param.getThing().getType());
						for (Concept c : serviceIpt)
						{
							if (dummy.getName().equalsIgnoreCase(c.getName()))
							{
								tempCount++;
							}
						}
					}
					if (tempCount == paramSet.size())
						return true;
				}
			}
			return false;
		}
		else
		{
			return true;
		}
	}

	public static Set<Service> getPossibleWSToBeConsumed(Set<Concept> InputSet, Map<String, Service> serviceMap)
	{

		Set<Service> possibleWSs = new HashSet<Service>();

		for (Entry<String, Service> service : serviceMap.entrySet())
		{
			int count = 0;
			Set<Concept> outputConcept = service.getValue().getOutputConceptSet();
			for (Concept iCon : InputSet)
			{
				if (outputConcept.contains(iCon))
				{
					count++;
				}
			}
			if(count == outputConcept.size())
				possibleWSs.add(service.getValue());
		}

		return possibleWSs;
	}
	public static void addParentConcepts(Set<Concept> set)
	{
		Iterator<Concept> iter = set.iterator();
		Set<Concept> dummmySet = new HashSet<Concept>();
		while(iter.hasNext())
		{
			Concept c = iter.next();
			Set<Concept> tempSet = new HashSet<Concept>();
			tempSet.addAll(c.getParentConceptsIndex());
			addToSet(dummmySet, tempSet);
		}
		addToSet(set, dummmySet);
	}

}
