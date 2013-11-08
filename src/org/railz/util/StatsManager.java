package org.railz.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.config.LogManager;

public class StatsManager {
	
	private static final String CLASS_NAME = StatsManager.class.getName();
	private static final Logger LOGGER = LogManager.getLogger(CLASS_NAME);
	public static final String DEFAULT_REGULARITY = "defaultReg";
	
	public static final String CARGO_TYPE = "cargoType";
	//TODO rename
	private static Map <String, List> statSequences;
	private static Map <String, Integer> outputRegularity;
	// Different entries in here by name
	// Need list in this map
	
	private Map <String, List<NameValuePair>> statsSequences = new HashMap <String, List<NameValuePair>> ();
	
	static {
		outputRegularity = new HashMap<String, Integer> ();
		outputRegularity.put(DEFAULT_REGULARITY, 1);
		outputRegularity.put(CARGO_TYPE, 30);
	}
	private static int getRegularity (String name) {
		int regularity = outputRegularity.get(name);
		if (regularity == 0) {
			regularity = outputRegularity.get(CARGO_TYPE);
		}
		return regularity;
	}
	// Local stuff
	private String name;
	private int times = 1;
	private Map <String, Double> nameValuePairs = new HashMap <String, Double>  ();
	private List<String> keys = new ArrayList <String> ();
	private List <Double> values = new ArrayList <Double> ();
	
	private List <NameValuePair> nvPairList = new ArrayList<NameValuePair>();
	// Different vars in map
	
	public static StatsManager getInstance () {
		return new StatsManager();
	}
	
	public static StatsManager getInstance (String name) {
		StatsManager tempManager = new StatsManager(name);
		addManager (name, tempManager);
		return tempManager;
	}
	
	public static void printSequence (String name) {
		List<StatsManager> sequence = getStatSequences (name);
		for (StatsManager man : sequence) {
			
		}
	}
	private static void initiateStatSequences () {
		if (statSequences == null) {
			statSequences = new HashMap ();
		}
		//getStatSequences();
	}
	private static List<StatsManager> getStatSequences (String key) {
		initiateStatSequences();
		List<StatsManager> multipleStats = (List<StatsManager>)statSequences.get(key);
		if (multipleStats == null) {
			multipleStats = new ArrayList <StatsManager> ();
		}
		
		return multipleStats;
	}
	private static void addManager(String name2, StatsManager tempManager) {
//		if (statSequences == null) {
//			statSequences = new HashMap ();
//		}
//		List multipleStats = (List)statSequences.get(name2);
//		if (multipleStats == null) {
//			multipleStats = new ArrayList ();
//		}
		List<StatsManager> multipleStats = getStatSequences (name2);
		multipleStats.add(tempManager);
		
		int regularity = getRegularity (name2);
		int statsSize = multipleStats.size();
		int remainder = statsSize%regularity;
		System.out.println("Remainder = " + remainder );
		if (remainder == 0) {
			tempManager.printEntry();
		}
		
		//statsInstances.put(name2, tempManager);
	}

	private StatsManager () {
		
	}
	
	private StatsManager (String name) {
		this.name = name;
	}
	
	// Local Object stuff
	private Map getEntries () {
		return nameValuePairs;
	}
	

	public void addParameter (String key, double value) {
		//StatsManager statsManager = (StatsManager) statsInstances.get(this.name);

		//if (statsManager != null) {
			//statsManager.getEntries().put (key, new Integer (value));
		
		nvPairList.add(new NameValuePair (key, String.valueOf(value)));
		getEntries().put (key, new Double (value));
		//}
		keys.add(key);
		values.add(value);
	}
	
	public void printEntry () {
		final String METHOD_NAME = "printEntry";
		
//		Set<String> valueKeys = getEntries().keySet();
//		for (String key : valueKeys) {
//			Object currentVal = getEntries().get(key);
//			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, key + " = " + currentVal + " | ");
//		}
//		System.out.println();
		StringBuilder output = new StringBuilder ();
		boolean firstValue = true;
		for (int i = 0; i < keys.size(); i++) {
			if (!firstValue) {
				output.append(" | ");
			} else {
				firstValue = false;
			}
			output.append(keys.get(i) + " = " + values.get(i));
			
		}
		//LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, output.toString());
		
		StringBuilder output2 = new StringBuilder ();
		firstValue = true;
		for (NameValuePair currentPair : nvPairList) {
			if (!firstValue) {
				output2.append(" | ");
			} else {
				firstValue = false;
			}
			output2.append(currentPair.getName() + " = " + currentPair.getValue());
		}
		LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, output.toString());
	}
}
