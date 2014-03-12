package ims.coref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Parallel {

	public static boolean zero = false;
	
	public static boolean testzero = false;
	
	public static boolean jointTest = true;
	
	public static boolean dev = false;
	
	public static boolean onlyPronoun = true;
	
	public static char part;
	
	public static HashSet<String> engPros = new HashSet<String>();
	public static HashSet<String> chiPros = new HashSet<String>();
	
	public static HashMap<String, int[]> chiStat = new HashMap<String, int[]>();
	public static HashMap<String, int[]> engStat = new HashMap<String, int[]>();
	
	public static boolean turn = false;
	
	public static double engOverall = 0;
	public static double engMatch = 0;
	public static double engSpanMount = 0;

	public static double chiOverall = 0;
	public static double chiMatch = 0;
	public static double chiSpanMount = 0;
	
	public static boolean ensemble = true;
	
	public static ArrayList<SortedDoc> chiSortedDocs = new ArrayList<SortedDoc>();
	public static ArrayList<SortedDoc> engSortedDocs = new ArrayList<SortedDoc>();


	public static HashSet<String> accessFiles = new HashSet<String>();
	
	public static boolean filter = false;
	
	public static int chiAllPronouns = 0;
	public static int engAllPronouns = 0;
	
	public static int chiAnaphor = 0;
	public static int engAnaphor = 0;
}
