package ca.concordia.pga.utils;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ca.concordia.pga.models.Concept;
import ca.concordia.pga.models.Param;
import ca.concordia.pga.models.PlanningGraph;
import ca.concordia.pga.models.Service;
import ca.concordia.pga.models.Thing;

public class DocumentParser {

	/**
	 * Parse taxonomy document from given URL
	 * 
	 * @param conceptMap
	 * @param thingMap
	 * @param url
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public static void parseTaxonomyDocument(Map<String, Concept> conceptMap,
			Map<String, Thing> thingMap, String url) throws DocumentException {
		File taxonomyFile = new File(url);
		SAXReader reader = new SAXReader();
		Document document = reader.read(taxonomyFile);
		Element taxonomyRoot = document.getRootElement();

		/**
		 * loop through semantic elements to check taxonomy
		 */
		for (Iterator i = taxonomyRoot.elementIterator(); i.hasNext();) {
			Element el = (Element) i.next();
			if (el.getName().equals("Class")) {
				Concept concept = new Concept(el.attribute("ID").getText());//Get the concept name(ID)
				List<Element> list = el.elements();
				if(list.size() != 0)
				{
					for(Element dummy : list)
					{
						concept.setDirectParantName(dummy
								.attribute("resource").getText()
								.replaceAll("#", ""));//Get the direct parant class

					}
				}
				else
				{
					concept.setRoot(true);
				}
//				if (el.element("subClassOf") != null) {
//					concept.setDirectParantName(el.element("subClassOf")
//							.attribute("resource").getText()
//							.replaceAll("#", ""));//Get the direct parant class
//
//				} else {
//					concept.setRoot(true);
//				}
				conceptMap.put(concept.getName(), concept);

			} else if (el.getName().equals("Thing")) {
				Thing thing = new Thing(el.attribute("ID").getText());

				thing.setType(el.element("type").attribute("resource")
						.getText().replaceAll("#", ""));//type = parant class

				thingMap.put(thing.getName(), thing);
			}
		}

		/**
		 * build indexing for concept
		 */
		for (String key : conceptMap.keySet()) {
			Concept concept = conceptMap.get(key);
			Concept varConcept = conceptMap.get(key);
			do {
				concept.addConceptToParentIndex(varConcept);//ancestor classes
				varConcept.addConceptToChildrenIndex(concept);//descendant classes
				if (varConcept.isRoot()) {
					varConcept = null;
				} else {
					varConcept = conceptMap.get(varConcept
							.getDirectParantName());
				}

			} while (varConcept != null);

		}

	}

	/**
	 * Parse services document from given URL
	 * 
	 * @param serviceMap
	 * @param paramMap
	 * @param conceptMap
	 * @param thingMap
	 * @param url
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public static void parseServicesDocument(Map<String, Service> serviceMap,
			Map<String, Param> paramMap, Map<String, Concept> conceptMap,
			Map<String, Thing> thingMap, String url) throws DocumentException {

		File ServicesFile = new File(url);
		SAXReader reader = new SAXReader();
		Document document = reader.read(ServicesFile);
		Element servicesRoot = document.getRootElement();
		Element semRoot = servicesRoot.element("semExtension");

		/**
		 * loop through semantic elements
		 */
		Service service = null;

		for (Iterator i = semRoot.elementIterator(); i.hasNext();) {
			Element semMsgExtEl = (Element) i.next();
			if (semMsgExtEl.getName().equals("semMessageExt")) {
				boolean isRequestParam;
				if (semMsgExtEl.attribute("id").getText().contains(
						"RequestMessage")) {
					service = new Service(semMsgExtEl.attribute("id").getText()
							.replaceAll("RequestMessage", ""));
					isRequestParam = true;
				} else {
					isRequestParam = false;
				}

				for (Iterator j = semMsgExtEl.elementIterator(); j.hasNext();) {
					Element semExtEl = (Element) j.next();
					if (semExtEl.getName().equals("semExt")) {
						Param param = new Param(semExtEl.attribute("id")
								.getText());
						Thing thing = thingMap.get(semExtEl.element(
								"ontologyRef").getText().replaceAll(
								"http://www.ws-challenge.org/wsc08.owl#", ""));

						param.setThing(thing);
						paramMap.put(param.getName(), param);
						if (isRequestParam) {
							service.addInputParam(param);
							service.addInputConcept(conceptMap.get(thing
									.getType()));
						} else {
							service.addOutputParam(param);
							for (Concept c : conceptMap.get(thing.getType())
									.getParentConceptsIndex()) {
								service.addOutputConcept(c);//outputs indexing
							}
						}
					}
				}
				if (semMsgExtEl.attribute("id").getText().contains(
						"ResponseMessage")) {
					serviceMap.put(service.getName(), service);
				}

			}
		}
	}

	/**
	 * Parse the challenge String given by client. Also convert the I/O params
	 * to concepts
	 * 
	 * @param paramMap
	 * @param conceptMap
	 * @param thingMap
	 * @param pg
	 * @param url
	 * @throws DocumentException
	 */
	@SuppressWarnings("unchecked")
	public static void parseChallengeDocument(Map<String, Param> paramMap,
			Map<String, Concept> conceptMap, Map<String, Thing> thingMap,
			PlanningGraph pg, String url) throws DocumentException {

		Set<Concept> initPLevel = new HashSet<Concept>();
		Set<Concept> goalSet = new HashSet<Concept>();

		File ServicesFile = new File(url);
		SAXReader reader = new SAXReader();
		Document document = reader.read(ServicesFile);
		Element servicesRoot = document.getRootElement();
		Element semRoot = servicesRoot.element("semExtension");

		for (Iterator i = semRoot.elementIterator(); i.hasNext();) {
			Element semMsgExtEl = (Element) i.next();
			if (semMsgExtEl.getName().equals("semMessageExt")) {
				boolean isRequestParam;
				if (semMsgExtEl.attribute("id").getText().contains(
						"RequestMessage")) {
					isRequestParam = true;
				} else {
					isRequestParam = false;
				}

				for (Iterator j = semMsgExtEl.elementIterator(); j.hasNext();) {
					Element semExtEl = (Element) j.next();
					if (semExtEl.getName().equals("semExt")) {
						Param param = new Param(semExtEl.attribute("id")
								.getText());
						Thing thing = thingMap.get(semExtEl.element(
								"ontologyRef").getText().replaceAll(
								"http://www.ws-challenge.org/wsc08.owl#", ""));

						param.setThing(thing);
						paramMap.put(param.getName(), param);
						if (isRequestParam) {
							for (Concept c : conceptMap.get(thing.getType())
									.getParentConceptsIndex()) {
								initPLevel.add(c);
							}
						} else {
							goalSet.add(conceptMap.get(thing.getType()));
						}
					}
				}
				if (semMsgExtEl.attribute("id").getText().contains(
						"ResponseMessage")) {
					pg.addPLevel(initPLevel);
					pg.setGoalSet(goalSet);
				}
			}
		}
		pg.addALevel(new HashSet<Service>());

	}


}
