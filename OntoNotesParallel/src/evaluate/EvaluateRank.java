package evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import util.Common;

public class EvaluateRank {

	static String lang = "";

	static String ilpbase = "/users/yzcchen/chen3/ijcnlp2013/ilp/";

	static String rankbase = "/users/yzcchen/chen3/ijcnlp2013/ranker/";

	static int startFolder = 0;

	static boolean singleF = false;

	public static void main(String args[]) {

		if (args.length == 0) {
			System.out.println("java ~ chi|eng folder");
			System.exit(1);
		}
		lang = args[0];
		if (args.length == 2) {
			startFolder = Integer.parseInt(args[1]);
			singleF = true;
		}

		HashMap<String, HashMap<String, HashSet<String>>> goldMapses = new HashMap<String, HashMap<String, HashSet<String>>>();

		loadGold(goldMapses);
		loadSVMRankResult();

		evaluate(goldMapses);
	}

	public static HashMap<String, HashMap<String, Double>> probsCache = new HashMap<String, HashMap<String, Double>>();

	public static HashMap<String, HashMap<String, Double>> alignProbsCache = new HashMap<String, HashMap<String, Double>>();

	public static HashMap<String, ArrayList<String>> pronounsCache = new HashMap<String, ArrayList<String>>();

	public static HashMap<String, HashMap<String, ArrayList<String>>> pairsCache = new HashMap<String, HashMap<String, ArrayList<String>>>();

	public static void loadSVMRankResult() {
		int i = startFolder;
		for (; i < 5; i++) {
			File folder = new File(rankbase + lang + i);
			for (File file : folder.listFiles()) {
				if (file.getAbsolutePath().endsWith(".rankO")) {
					String fileStem = file.getAbsolutePath();
					int k = fileStem.lastIndexOf(".");
					loadOneSysFile(fileStem.substring(0, k));
				}
			}
			if (singleF) {
				break;
			}
		}
	}

	public static void loadOneSysFile(String fileStem) {
		ArrayList<String> svmLines = Common.getLines(fileStem + ".rankO");
		ArrayList<String> combSvmLines = Common.getLines(fileStem + ".rankE");
		ArrayList<String> xSvmLines = Common.getLines(fileStem + ".rankX");

		ArrayList<String> svmSpans = Common.getLines(fileStem + ".spansO");
		ArrayList<String> xSvmSpans = Common.getLines(fileStem + ".spansX");

		HashMap<String, ArrayList<String>> pairsCach = new HashMap<String, ArrayList<String>>();
		HashMap<String, Double> probCach = new HashMap<String, Double>();

		int k = fileStem.lastIndexOf(File.separator);
		String stem = fileStem.substring(k + 1);
		pairsCache.put(stem, pairsCach);
		probsCache.put(stem, probCach);

		if (svmLines.size() != svmSpans.size()) {
			Common.bangErrorPOS("");
		}

		for (int i = 0; i < svmLines.size(); i++) {
			String key = svmSpans.get(i);

			String ant = key.split("\\-")[0];
			String anaphor = key.split("\\-")[1];

			ArrayList<String> ants = pairsCach.get(anaphor);
			if (ants == null) {
				ants = new ArrayList<String>();
				pairsCach.put(anaphor, ants);
			}
			ants.add(ant);

			probCach.put(key + "O", Double.valueOf(svmLines.get(i)));
		}

		if (!(combSvmLines.size() == xSvmLines.size() && xSvmSpans.size() == xSvmLines
				.size())) {
			System.out.println(fileStem);
			Common.bangErrorPOS(combSvmLines.size() + " !!!" + xSvmLines.size() + " !!! " + xSvmSpans.size() );
		}

		for (int i = 0; i < xSvmSpans.size(); i++) {
			String key = svmSpans.get(i);
			probCach.put(key + "X", Double.valueOf(xSvmLines.get(i)));
			probCach.put(key + "E", Double.valueOf(combSvmLines.get(i)));
		}

	}

	private static double evaluate(
			HashMap<String, HashMap<String, HashSet<String>>> goldMapses) {
		HashMap<String, HashMap<String, String>> sysMapses = new HashMap<String, HashMap<String, String>>();
		int i = startFolder;
		for (; i < 5; i++) {
			for (String key : goldMapses.keySet()) {
				HashMap<String, String> sysMap = loadClosestEnsembleAvg(key);
				sysMapses.put(key, sysMap);
			}
			if (singleF) {
				break;
			}
		}
		return printStat(goldMapses, sysMapses);
	}

	private static HashMap<String, String> loadClosestEnsembleAvg(String file) {
		// TODO Auto-generated method stub
		HashMap<String, String> map = new HashMap<String, String>();

		HashMap<String, Double> probCach = probsCache.get(file);
		ArrayList<String> pronounsCach = pronounsCache.get(file);
		HashMap<String, ArrayList<String>> pairsCach = pairsCache.get(file);

		HashMap<String, Double> alignCach = alignProbsCache.get(file);

		for (String anaphor : pairsCach.keySet()) {
			double max = -111110;
			for (String ant : pairsCach.get(anaphor)) {
				String key = ant + "-" + anaphor + "O";
				double op = probCach.get(key);
				// System.out.println(key + "\t" + op);
				if (op > max) {
					max = op;
					map.put(anaphor, ant);
				}
			}
			// System.out.println("-----");
		}

		HashSet<String> pronouns = new HashSet<String>();
		pronouns.addAll(map.keySet());
		for (String pronoun : pronouns) {
			if (pronoun.equalsIgnoreCase(map.get(pronoun))) {
				map.remove(pronoun);
			}
		}

		return map;
	}

	private static double printStat(
			HashMap<String, HashMap<String, HashSet<String>>> goldMapses,
			HashMap<String, HashMap<String, String>> sysMapses) {
		double goldA = 0;
		double goldNA = 0;
		double hitA = 0;
		double hitNA = 0;

		double sysA = 0;

		for (String mainKey : goldMapses.keySet()) {
			HashMap<String, HashSet<String>> goldMaps = goldMapses.get(mainKey);
			HashMap<String, String> sysMaps = sysMapses.get(mainKey);
			sysA += sysMaps.size();
			for (String key : goldMaps.keySet()) {
				HashSet<String> goldAnts = goldMaps.get(key);
				String sysAnt = sysMaps.get(key);
				if (goldMaps.get(key).size() == 0) {
					goldNA++;
					if (sysAnt == null) {
						hitNA++;
					}
				} else {
					goldA++;
					if (sysAnt != null && goldAnts.contains(sysAnt)) {
						hitA++;
					}
				}
			}
		}

		double rec = hitA / goldA;
		double pre = hitA / sysA;
		System.out.format("Rec: %f / %f = %f\n", hitA, goldA, hitA / goldA
				* 100);
		System.out.format("Pre: %f / %f = %f\n", hitA, sysA, hitA / sysA * 100);
		double f = 2 * rec * pre / (rec + pre) * 100;
		System.out.format("F-1: %f\n", f);
		double acc = (hitA + hitNA) / (goldA + goldNA) * 100;
		System.out.format("Accu: (%f + %f) / (%f + %f) = %f\n", hitA, hitNA,
				goldA, goldNA, acc);
		System.out.println("================");

		return f;
	}

	private static void loadGold(
			HashMap<String, HashMap<String, HashSet<String>>> goldMapses) {
		int i = startFolder;
		for (; i < 5; i++) {
			File folder = new File(ilpbase + lang + i);
			for (File file : folder.listFiles()) {
				if (file.getAbsolutePath().endsWith(".ilp")) {
					String ilpFile = file.getAbsolutePath();
					int k = ilpFile.lastIndexOf(".");
					String goldFile = ilpFile.substring(0, k) + ".gold";
					HashMap<String, HashSet<String>> goldMaps = loadGoldOneFile(goldFile);

					int a = ilpFile.lastIndexOf(File.separator);
					int b = ilpFile.lastIndexOf('.');

					goldMapses.put(ilpFile.substring(a + 1, b), goldMaps);
				}
			}
			if (singleF) {
				break;
			}
		}
	}

	public static HashMap<String, HashSet<String>> loadGoldOneFile(String filePath) {
		HashMap<String, HashSet<String>> goldMap = new HashMap<String, HashSet<String>>();
		ArrayList<String> lines = Common.getLines(filePath);
		HashSet<String> pronouns = new HashSet<String>();
		boolean collectMention = true;
		for (String line : lines) {
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			String tokens[] = line.split("\\s+");
			if (collectMention) {
				if (tokens[1].equalsIgnoreCase("p")) {
					pronouns.add(tokens[0]);

					HashSet<String> ants = new HashSet<String>();
					goldMap.put(tokens[0], ants);

				}
			} else {
				for (int i = 0; i < tokens.length; i++) {
					String token = tokens[i];
					if (pronouns.contains(token)) {
						for (int j = 0; j < i; j++) {
							// if(!pronouns.contains(tokens[j])) {
							goldMap.get(token).add(tokens[j]);
							// }
						}
					}
				}
			}
		}
		return goldMap;
	}

}
