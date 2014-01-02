package ca.concordia.pga.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;

import ca.concordia.pga.models.Concept;
import ca.concordia.pga.models.Param;
import ca.concordia.pga.models.PlanningGraph;
import ca.concordia.pga.models.Service;
import ca.concordia.pga.models.Thing;
import ca.concordia.pga.searching.BackwardSearch;
import ca.concordia.pga.searching.ForwardSearch;
import ca.concordia.pga.utils.DocumentParser;
import ca.concordia.pga.utils.IndexBuilder;

public class AutomatedMain {

	// change the Prefix URL according your environment
	 static final String PREFIX_URL = ".//DataSets//WSC2009_Testsets//Testset01//";
//	 static final String PREFIX_URL = ".//DataSets//simpledataset//";
	static final String TAXONOMY_URL = PREFIX_URL + "Taxonomy.owl";
	static final String SERVICES_URL = PREFIX_URL + "Services.wsdl";
	static final String CHALLENGE_URL = PREFIX_URL + "Challenge.wsdl";
	static final String FILENAME = PREFIX_URL + "output.txt";
	public static Map<String, Concept> conceptMap = null;

	public static void main(String[] args) throws IOException
	{
		conceptMap = new HashMap<String, Concept>();
		Map<String, Thing> thingMap = new HashMap<String, Thing>();
		Map<String, Service> serviceMap = new HashMap<String, Service>();
		Map<String, Param> paramMap = new HashMap<String, Param>();
		PlanningGraph pg = new PlanningGraph();
		File file = new File(FILENAME);
		try
		{
			DocumentParser.parseTaxonomyDocument(conceptMap, thingMap, TAXONOMY_URL);
			DocumentParser.parseServicesDocument(serviceMap, paramMap, conceptMap, thingMap, SERVICES_URL);
		} catch (DocumentException e)
		{
			e.printStackTrace();
		}

		IndexBuilder.buildInvertedIndex(conceptMap, serviceMap);

		/**
		 * begin parsing process
		 */
		try
		{
			DocumentParser.parseChallengeDocument(paramMap, conceptMap, thingMap, pg, CHALLENGE_URL);
		} catch (DocumentException e)
		{
			e.printStackTrace();
		}
		System.out.println("Parsing Successfully Completed");
		Vector<Set<Concept>> inputLevels = pg.getPLevels();
		Set<Concept> inputSet = new HashSet<Concept>();
		inputSet.addAll(inputLevels.get(0));
		ForwardSearch.doForWardSearch(pg, serviceMap);
		int levelCount = 0;
		LinkedList<LinkedHashSet<Concept>> propLevels = new LinkedList<LinkedHashSet<Concept>>(PlanningGraph.propLevels);
		System.out.println("Input Parameters : "+propLevels.get(0));
		System.out.println("Goal : "+pg.getGoalSet());
		StringBuilder builder = new StringBuilder();
		System.out.println("====================================================================");
		System.out.println("Input Concepts: " + propLevels.get(0));
		System.out.println("Goal Concepts: " + pg.getGoalSet());
		for(LinkedHashSet<Service> aLevel : PlanningGraph.actionLevels){
			System.out.println("******************************Level " + (++levelCount) + "******************************");
			System.out.println("Services: "+aLevel);
			builder.append(aLevel.toString().replace("[", "").replace("]", ""));
			builder.append("\n");
			System.out.println("Number of services:" + aLevel.size());
			System.out.println("Parameters: " + propLevels.get(levelCount));
			System.out.println("Number of Parameters:" + propLevels.get(levelCount).size());
		}
		System.out.println("====================================================================");
		System.out.println("Forward search time was " + ForwardSearch.ComposeTime + " ms.");
		System.out.println("Number of levels: " + levelCount);
		System.out.println("Back Tracking time : "+ForwardSearch.backTrackingTime+" ms.");
		System.out.println("====================================================================");
		FileUtils.writeStringToFile(file, builder.toString());
		
//		long start = System.currentTimeMillis();
////		BackwardSearch.doBackwardSearch(pg, serviceMap);
//		long end = System.currentTimeMillis();
//		System.out.println("Backward search time was " + (end - start) + " ms.");
	}

}
