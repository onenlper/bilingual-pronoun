package evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import model.Entity;
import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLWord;
import util.Common;

public class EvaluateST {

	public static void main(String args[]) {
		if (args.length != 2) {
			Common.bangErrorPOS("java ~ folder conllFile");
		}
		String folder = args[0];
		String conllPath = args[1];
		HashMap<String, HashMap<String, HashSet<String>>> corefResults = loadSTCoref(conllPath);
		HashMap<String, HashMap<String, HashSet<String>>> goldResults = loadGoldAll(folder);
		System.out.println(corefResults.size() + " # " + goldResults.size());

		String skipStr = "nw/xinhua/00/chtb_0029, "
				+ "bc/phoenix/00/phoenix_0009, " + "nw/xinhua/00/chtb_0079, "
				+ "nw/xinhua/01/chtb_0179, " + "nw/xinhua/02/chtb_0229, "
				+ "wb/cmn/00/cmn_0009, " + "nw/xinhua/01/chtb_0129, "
				+ "nw/xinhua/02/chtb_0279";
		String tks[] = skipStr.split(",");
		HashSet<String> skipSet = new HashSet<String>();
		for (String tk : tks) {
			skipSet.add(tk.trim().replace("/", "-"));
		}
		
		double goldA = 0;
		double goldNA = 0;
		double hitA = 0;
		double hitNA = 0;

		double sysA = 0;

		for (String key : goldResults.keySet()) {
			if (skipSet.contains(key.substring(0, key.length()-2))) {
				// System.out.println("skip " + stem);
				continue;
			}
			HashMap<String, HashSet<String>> goldResult = goldResults.get(key);
			HashMap<String, HashSet<String>> sysResult = corefResults.get(key);

			HashSet<String> pronouns = new HashSet<String>();

			for (String pronoun : goldResult.keySet()) {
				// TODO
				pronouns.add(pronoun);
				HashSet<String> goldAnts = goldResult.get(pronoun);
				if (goldAnts.size() > 0) {
					goldA++;
					HashSet<String> sysAnts = sysResult.get(pronoun);
					if (sysAnts != null) {
						boolean hit = false;
						for (String sysAnt : sysAnts) {
							if (goldAnts.contains(sysAnt)) {
								hit = true;
								break;
							}
						}
						if (hit) {
							hitA++;
						}
					}
				} else {
					goldNA++;
					if (!sysResult.containsKey(pronoun)) {
						hitNA++;
					}
				}

				if (sysResult.containsKey(pronoun)) {
					sysA++;
				}
			}

		}

		double rec = hitA / goldA;
		double pre = hitA / sysA;
		System.out.format("Rec: %f / %f = %f\n", hitA, goldA, hitA / goldA
				* 100);
		System.out.format("Pre: %f / %f = %f\n", hitA, sysA, hitA / sysA * 100);
		System.out.format("F-1: %f\n", 2 * rec * pre / (rec + pre) * 100);

		System.out.format("Accu: (%f + %f) / (%f + %f) = %f\n", hitA, hitNA,
				goldA, goldNA, (hitA + hitNA) / (goldA + goldNA) * 100);
	}

	static String base = "/users/yzcchen/chen3/ijcnlp2013/ilp/";

	private static HashMap<String, HashMap<String, HashSet<String>>> loadGoldAll(
			String part) {
		File folder = new File(base + "chi" + part);
		HashMap<String, HashMap<String, HashSet<String>>> goldResults = new HashMap<String, HashMap<String, HashSet<String>>>();
		for (File file : folder.listFiles()) {
//			System.out.println(file.getAbsolutePath());
			if (file.getAbsolutePath().endsWith(".ilp")) {
				String ilpFile = file.getAbsolutePath();
				
				int k = ilpFile.lastIndexOf(".");
				String filePrefix = ilpFile.substring(0, k);
				String goldFile = filePrefix + ".gold";
				HashMap<String, HashSet<String>> goldMaps = loadGold(goldFile);

				int a = filePrefix.lastIndexOf(File.separator);
				String stem = filePrefix.substring(a + 1).replace(".", "_");
//				 System.out.println(stem);
				goldResults.put(stem, goldMaps);
			}
		}
		return goldResults;
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

	private static HashMap<String, HashMap<String, HashSet<String>>> loadSTCoref(
			String conllPath) {
		CoNLLDocument document = new CoNLLDocument(conllPath);
		HashMap<String, HashMap<String, HashSet<String>>> corefResults = new HashMap<String, HashMap<String, HashSet<String>>>();

		for (CoNLLPart part : document.getParts()) {
			HashMap<String, HashSet<String>> corefResult = new HashMap<String, HashSet<String>>();
			corefResults.put(part.getPartName(), corefResult);
			// System.out.println(part.getPartName());
			ArrayList<Entity> chains = part.getChains();
			for (Entity chain : chains) {
				Collections.sort(chain.mentions);

				for (int i = 1; i < chain.mentions.size(); i++) {
					EntityMention anaphor = chain.mentions.get(i);
					String anaphorStr = getReadName(anaphor, part);
					HashSet<String> ants = new HashSet<String>();
					corefResult.put(anaphorStr, ants);

					for (int j = i - 1; j >= 0; j--) {
						EntityMention ant = chain.mentions.get(j);
						String antStr = getReadName(ant, part);
						ants.add(antStr);
						// TODO
						if (true) {
//							break;
						}
					}
					
//					for (int j = i + 1; j <chain.mentions.size(); j++) {
//						EntityMention ant = chain.mentions.get(j);
//						String antStr = getReadName(ant, part);
//						ants.add(antStr);
//					}
				}
			}
		}
		return corefResults;
	}

	static String getReadName(EntityMention m, CoNLLPart part) {
		CoNLLWord start = part.getWord(m.start);
		CoNLLWord end = part.getWord(m.end);
		StringBuilder sb = new StringBuilder();

		sb.append(start.sentence.getSentenceIdx()).append(":")
				.append(start.indexInSentence + 1).append(",")
				.append(end.indexInSentence + 1);
		return sb.toString();
	}
}
