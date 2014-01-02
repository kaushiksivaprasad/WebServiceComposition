package ca.concordia.pga.searching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

public class ForwardSearch {
	public static LinkedList<String> servicesConsumed = null;
	public static Set<Concept> goalSet = null;
	public static PlanningGraph plannningGraph = null;
	public static long ComposeTime = 0;
	public static long backTrackingTime = 0l;
	public static void doForWardSearch(PlanningGraph pg, Map<String, Service> serviceMap)
	{
		plannningGraph = pg;
		Vector<Set<Concept>> inputLevels = pg.getPLevels();
		Set<Concept> inputSet = inputLevels.get(0);
		servicesConsumed = new LinkedList<String>();
		goalSet = pg.getGoalSet();
		Map<String, Service> tempServices = new HashMap<String, Service>();
		tempServices.putAll(serviceMap);
		LinkedHashSet<Concept> set = new LinkedHashSet<Concept>();
		set.addAll(inputSet);
		PlanningGraph.propLevels.add(set);
		long start = System.currentTimeMillis();
		compose(inputSet, tempServices, goalSet);
		ComposeTime = System.currentTimeMillis() - start;
	}

	public static boolean compose(Set<Concept> inputSet, Map<String, Service> serviceMap, Set<Concept> goalSet)
	{
		if (compareSets(inputSet, goalSet))
		{
			return true;
		}
		Set<Service> servicesToBeTried = getPossibleWSToBeConsumed(inputSet, serviceMap);

		if (expandGraph(servicesToBeTried, serviceMap, inputSet, servicesConsumed, new HashSet<Service>(),true))
		{
			LinkedHashSet<Service> levels = new LinkedHashSet<Service>();
			for(String str : servicesConsumed)
			{
				Service svc = serviceMap.remove(str);
				if(svc != null)
				{
					levels.add(svc);
				}
			}
			servicesConsumed = new LinkedList<String>();
			if(levels.size() != 0)
			{
				PlanningGraph.actionLevels.add(levels);
				LinkedHashSet<Concept> set = new LinkedHashSet<Concept>();
				set.addAll(inputSet);
				PlanningGraph.propLevels.add(set);
			}
			//			System.out.println(servicesConsumed);

			if (compose(inputSet, serviceMap, goalSet))
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
		boolean retVal = compareSets(serviceIpt, goalSet);
		if (!retVal)
		{
			for (Service service : getPossibleWSToBeConsumed(serviceIpt, serviceMap))
			{
				int tempCount = 0;
				Set<Param> paramSet = service.getInputParamSet();
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
			if (InputSet.containsAll(service.getValue().getInputConceptSet()))
			{
				possibleWSs.add((Service) service.getValue());
			}
		}

		return possibleWSs;
	}

	public static Set<Service> getWSThatMatchesAtleastOneInput(Set<Concept> InputSet, Map<String, Service> serviceMap)
	{
		Set<Service> possibleWSs = new HashSet<Service>();

		for (Entry<String, Service> service : serviceMap.entrySet())
		{
			for (Concept str : service.getValue().getInputConceptSet())
			{
				if (InputSet.contains(str))
				{
					possibleWSs.add((Service) service.getValue());
					break;
				}
			}
		}

		return possibleWSs;
	}

	public static Set<Service> getWSThatMatchesAtleastOneOutput(Set<Concept> outputSet, Map<String, Service> serviceMap)
	{
		Set<Service> possibleWSs = new HashSet<Service>();

		for (Entry<String, Service> service : serviceMap.entrySet())
		{
			int count = 0;
			Set<Concept> set = service.getValue().getOutputConceptSet();
			if (set.size() == outputSet.size())
			{
				for (Concept c : set)
				{
					if (outputSet.contains(c))
					{
						count++;
					}
				}
				if (count == outputSet.size())
				{
					possibleWSs.add(service.getValue());
				}
			}
		}

		return possibleWSs;
	}

	public static boolean expandGraph(Set<Service> servicesToBeTried, Map<String, Service> serviceMap, Set<Concept> inputSet,
			LinkedList<String> servicesConsumed, Set<Service> aggrPrevServices, boolean enableTimer)
	{
		int count = 0;
		Set<Concept> outputSet = null;
		String serviceNameToBeTried = null;
		boolean inputPresent = false;
		Iterator<Service> iter = servicesToBeTried.iterator();
		while (iter.hasNext())
		{
			Service service = iter.next();
			serviceNameToBeTried = service.getName();
			servicesConsumed.add(serviceNameToBeTried);
			outputSet = new HashSet<Concept>();
			outputSet.addAll(serviceMap.get(serviceNameToBeTried).getOutputConceptSet());
			int prevSize = inputSet.size();
			addToSet(inputSet, outputSet);
			if (compareSets(inputSet, goalSet))
			{
				aggrPrevServices.add(service);
				return true;
			}
			if (inputSet.size() == prevSize)
			{
				count++;
				servicesConsumed.remove(serviceNameToBeTried);
				serviceMap.remove(serviceNameToBeTried);
			}
			else
			{
				inputPresent = checkOtherInputsOfWS(inputSet, serviceMap);
				if (!inputPresent)
				{
					count++;
					serviceMap.remove(serviceNameToBeTried);
					servicesConsumed.remove(serviceNameToBeTried);
					removeFromSet(inputSet, outputSet);
				}
				else
				{
					Set<Concept> dummySet = new HashSet<Concept>();
					dummySet.addAll(serviceMap.get(serviceNameToBeTried).getOutputConceptSet());
					aggrPrevServices.addAll(servicesToBeTried);
					Set<Service> servicesToBeConsumed = getPossibleWSToBeConsumed(inputSet, serviceMap);
					Set<Service> probableServices = getWSThatMatchesAtleastOneInput(dummySet, serviceMap);
					Set<Service> svcsProducingOutput = getWSThatMatchesAtleastOneOutput(dummySet, serviceMap);
					for (Service str : aggrPrevServices)
					{
						servicesToBeConsumed.remove(str);
						probableServices.remove(str);
						svcsProducingOutput.remove(str);
					}
					probableServices.removeAll(servicesToBeConsumed);
					svcsProducingOutput.removeAll(servicesToBeConsumed);
					Set<Concept> dummyInputSet = new HashSet<Concept>();
					dummyInputSet.addAll(inputSet);
					LinkedList<String> list = new LinkedList<String>();
					long start = System.currentTimeMillis();
					if (!expandGraph(servicesToBeConsumed, serviceMap, dummyInputSet, list, aggrPrevServices,false))
					{
						if ((probableServices.size() != 0 && svcsProducingOutput.size() != 0)
								|| (probableServices.size() == 0 && svcsProducingOutput.size() == 0)
								|| (probableServices.size() == 0 && svcsProducingOutput.size() != 0))
						{
							count++;
							serviceMap.remove(serviceNameToBeTried);
							servicesConsumed.remove(serviceNameToBeTried);
							removeFromSet(inputSet, outputSet);
						}
					}
					if(enableTimer)
					{
						backTrackingTime += (System.currentTimeMillis() - start); 
					}
				}
			}
		}
		if (count != servicesToBeTried.size())
		{
			return true;
		}
		return false;
	}
}
