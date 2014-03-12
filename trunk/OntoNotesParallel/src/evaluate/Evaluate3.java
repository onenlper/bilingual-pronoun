package evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Common;

public class Evaluate3 {

	static String lang = "";

	static String base = "/users/yzcchen/chen3/ijcnlp2013/ilp/";

	static int startFolder = 0;

	static boolean singleF = false;

	static double wX, wE;

	static double pX, pE;

	static double cO, cX, cE;

	static double wXs[] = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1,
			2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
	static double wEs[] = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1,
			2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
	static double pXs[] = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
	static double pEs[] = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };

	static double cOs[] = { 0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08,
			0.09, 0.1, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19,
			0.20, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29, 0.30 };

	static double cXs[] = { 0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08,
			0.09, 0.1, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19,
			0.20, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29, 0.30 };

	static double cEs[] = { 0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08,
			0.09, 0.1, 0.11, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19,
			0.20, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29, 0.30 };

	static String measure = "f";
	static String arg = "";
	static HashSet<String> skipSet = new HashSet<String>();

	public static void main(String args[]) {
		if (args.length == 0) {
			System.out.println("java ~ chi|eng acc|f folder");
			System.exit(1);
		}

		for (String a : args) {
			arg = arg + " " + a;
		}

		lang = args[0];
		measure = args[1];

		if (args.length > 2) {
			startFolder = Integer.parseInt(args[2]);
			singleF = true;
		}

		String skipStr = "nw/xinhua/00/chtb_0029, "
				+ "bc/phoenix/00/phoenix_0009, " + "nw/xinhua/00/chtb_0079, "
				+ "nw/xinhua/01/chtb_0179, " + "nw/xinhua/02/chtb_0229, "
				+ "wb/cmn/00/cmn_0009, " + "nw/xinhua/01/chtb_0129, "
				+ "nw/xinhua/02/chtb_0279";
		String tks[] = skipStr.split(",");
		for (String tk : tks) {
			 skipSet.add(tk.trim().replace("/", "-"));
		}

		HashMap<String, HashMap<String, HashSet<String>>> goldMapses = new HashMap<String, HashMap<String, HashSet<String>>>();
		HashMap<String, HashMap<String, String>> sysMapses = new HashMap<String, HashMap<String, String>>();

		loadGold(goldMapses);
		loadSystem();

		// 0.9,0.0 0.8,1.0 0.2,0.13,0.23 for 22
		// 0.8,0.2 0.4,0.2 0.19,0.24,0.3
		// String weightStr = "1.0,2.0  0.1,0.5 0.28,0.04,0.18";
		// String weightStr = "0.9,1.0  0.0,0.2 0.28,0.13,0.28";
		String weightStr = "0.6,0.5,0.0,0.0 0.3,0.0,0.0";
		tks = weightStr.split("[,\\s+]");
		System.out.println(tks.length);
		System.out.println(new ArrayList<String>(Arrays.asList(tks)));
		double[] weights = new double[7];
		int idx = 0;
		for (int i = 0; i < tks.length; i++) {
			if (tks[i].trim().isEmpty()) {
				continue;
			}
			weights[idx++] = Double.valueOf(tks[i]);
		}
		wX = weights[0];
		wE = weights[1];

		pX = weights[2];
		pE = weights[3];

		cO = weights[4];
		cX = weights[5];
		cE = weights[6];
		double best = 0;

		// wX = 1;
		// wE = 1;
		//
		// pX = 0;
		// pE = 0;
		//
		// cO = 0;
		// cX = 0;
		// cE = 0;
		best = tune(goldMapses, sysMapses);

		// System.out.println("Best F: " + bestF);
		// System.out.println("Under: " + bestFK);
		//
		// System.out.println("Best Acc: " + bestAcc);
		// System.out.println("Under: " + bestAccK);
	}

	private static double tune(
			HashMap<String, HashMap<String, HashSet<String>>> goldMapses,
			HashMap<String, HashMap<String, String>> sysMapses) {
		for (String ilpFile : goldMapses.keySet()) {
			HashMap<String, String> sysMap = loadClosestEnsembleAvg(ilpFile);
			sysMapses.put(ilpFile, sysMap);
		}
		return printStat(goldMapses, sysMapses);
	}

	private static void loadGold(
			HashMap<String, HashMap<String, HashSet<String>>> goldMapses) {
		int i = startFolder;
		File folder = new File(base + lang + i);

		int ap = 0;
		for (File file : folder.listFiles()) {
			if (file.getAbsolutePath().endsWith(".ilp")) {
				String ilpFile = file.getAbsolutePath();
				int k = ilpFile.lastIndexOf(".");

				int a = ilpFile.lastIndexOf(File.separator);
				int b = ilpFile.indexOf(".");
				String stem = ilpFile.substring(a + 1, b);
				if (skipSet.contains(stem)) {
					// System.out.println("skip " + stem);
					continue;
				}

				String goldFile = ilpFile.substring(0, k) + ".gold";
				HashMap<String, HashSet<String>> goldMaps = loadGold(goldFile);
				ap += goldMaps.size();
				int each = 3691 / 5;
				if (ap > each * 1 && ap <= each * 2)
					goldMapses.put(ilpFile, goldMaps);
			}
		}
		System.out.println(ap + "###");
	}

	private static void loadSystem() {
		int i = startFolder;
		// for (; i < 5; i++) {
		File folder = new File(base + lang + i);
		for (File file : folder.listFiles()) {
			if (file.getAbsolutePath().endsWith(".ilp")) {
				String ilpFile = file.getAbsolutePath();

				int a = ilpFile.lastIndexOf(File.separator);
				int b = ilpFile.indexOf(".");
				String stem = ilpFile.substring(a + 1, b);
				if (skipSet.contains(stem)) {
					// System.out.println("skip " + stem);
					continue;
				}

				loadOneSysFile(ilpFile);
			}
		}
		// if (singleF) {
		// break;
		// }
		// }
	}

	static double bestF = 0;
	static String bestFK = "";
	static double bestAcc = 0;
	static String bestAccK = "";

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
		String k = wX + "," + wE + "\t" + pX + "," + pE + "\t" + cO + "," + cX
				+ "," + cE;
		System.out.println(k);
		System.out.format("Rec: %f / %f = %f\n", hitA, goldA, hitA / goldA
				* 100);
		System.out.format("Pre: %f / %f = %f\n", hitA, sysA, hitA / sysA * 100);
		double f = 2 * rec * pre / (rec + pre) * 100;
		if (f > bestF) {
			bestF = f;
			bestFK = k;
		}
		System.out.format("F-1: %f\n", f);
		double acc = (hitA + hitNA) / (goldA + goldNA) * 100;
		if (acc > bestAcc) {
			bestAcc = acc;
			bestAccK = k;
		}
		System.out.format("Accu: (%f + %f) / (%f + %f) = %f\n", hitA, hitNA,
				goldA, goldNA, acc);
		System.out.println("=== " + arg + " ===");
		System.out.println("Best F: " + bestF);
		System.out.println("Under: " + bestFK);

		System.out.println("Best Acc: " + bestAcc);
		System.out.println("Under: " + bestAccK);
		System.out.println("================");

		if (measure.equalsIgnoreCase("f")) {
			return f;
		} else {
			return acc;
		}
	}

	static Pattern mentionP = Pattern.compile("([^\\s]*)\\s(.)@(.*)");

	static Pattern probP = Pattern
			.compile("([^\\-]*)-([^\\-]*)-([^\\s]*)\\s(.)");

	public static HashMap<String, HashMap<String, Double>> probsCache = new HashMap<String, HashMap<String, Double>>();

	public static HashMap<String, HashMap<String, Double>> alignProbsCache = new HashMap<String, HashMap<String, Double>>();

	public static HashMap<String, ArrayList<String>> pronounsCache = new HashMap<String, ArrayList<String>>();

	public static HashMap<String, HashMap<String, ArrayList<String>>> pairsCache = new HashMap<String, HashMap<String, ArrayList<String>>>();

	public static HashMap<String, String> loadClosestEnsembleAvg(String file) {
		HashMap<String, String> map = new HashMap<String, String>();

		HashMap<String, Double> probCach = probsCache.get(file);
		ArrayList<String> pronounsCach = pronounsCache.get(file);
		HashMap<String, ArrayList<String>> pairsCach = pairsCache.get(file);

		HashMap<String, Double> alignCach = alignProbsCache.get(file);

		for (String anaphor : pronounsCach) {
			ArrayList<String> ants = pairsCach.get(anaphor);
			for (String ant : ants) {

				double oprob = probCach.get(ant + "_" + anaphor + "_O");
				double xprob = probCach.get(ant + "_" + anaphor + "_X");
				double eprob = probCach.get(ant + "_" + anaphor + "_E");

				double antAlign = alignCach.get(ant);
				double anaphorAlign = alignCach.get(anaphor);

				double align = antAlign > anaphorAlign ? anaphorAlign
						: antAlign;

				double prob;

				if (.5 - Math.abs(.5 - oprob) < cO) {
					prob = oprob;
				} else if (.5 - Math.abs(.5 - xprob) < cX) {
					prob = xprob;
				} else if (.5 - Math.abs(.5 - eprob) < cE) {
					prob = eprob;
				} else {
					// prob = (oprob + xprob * wX * Math.pow(align, pX) + eprob
					// * wE * Math.pow(align, pE))
					// / (wX + wE + 1);

					prob = (oprob + xprob * wX * Math.pow(align, pX) + eprob
							* wE * Math.pow(align, pE))
							/ (1 + wX * Math.pow(align, pX) + wE
									* Math.pow(align, pE));
				}
				// System.out.println(prob + " " + xprob + " " + eprob);
				// prob = (prob * w1 + xprob * w2)
				// / (w1 + w2);
				if (prob > 0.5 && !map.containsKey(anaphor)) {
					// if(map.containsKey(ant)) {
					// map.put(anaphor, map.get(ant));
					// } else if(!pronouns.contains(ant))
					map.put(anaphor, ant);

				}
			}
		}
		return map;
	}

	public static void loadOneSysFile(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO

		HashMap<String, Double> probCach = new HashMap<String, Double>();
		ArrayList<String> pronounsCach = new ArrayList<String>();
		HashMap<String, ArrayList<String>> pairsCach = new HashMap<String, ArrayList<String>>();
		HashMap<String, Double> alignProbCach = new HashMap<String, Double>();

		probsCache.put(file, probCach);
		pronounsCache.put(file, pronounsCach);
		pairsCache.put(file, pairsCach);
		alignProbsCache.put(file, alignProbCach);

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronounsCach.add(mStr);
						ArrayList<String> ants = new ArrayList<String>();
						pairsCach.put(mStr, ants);
					}
					double alignProb = Double.parseDouble(m.group(3));
					alignProbCach.put(mStr, alignProb);
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					double xprob = prob;
					double eprob = prob;

					pairsCach.get(anaphor).add(ant);

					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						xprob = Double.parseDouble(lines.get(i + 1).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						eprob = Double.parseDouble(lines.get(i + 2).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o")) {
						probCach.put(ant + "_" + anaphor + "_O", prob);
						probCach.put(ant + "_" + anaphor + "_X", xprob);
						probCach.put(ant + "_" + anaphor + "_E", eprob);
					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
	}

	public static HashMap<String, HashSet<String>> loadGold(String filePath) {
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
