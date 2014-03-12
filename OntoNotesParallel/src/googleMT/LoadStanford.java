package googleMT;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import model.stanford.StanfordCoreference;
import model.stanford.StanfordMention;
import model.stanford.StanfordResult;
import model.stanford.StanfordSentence;
import model.stanford.StanfordToken;
import model.stanford.StanfordXMLReader;
import util.Common;

public class LoadStanford {

	static String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/";
	static String sourceLang;

	static String targetLang;

	public static void main(String args[]) {
		if (args.length != 1) {
			Common.bangErrorPOS("java ~ lang");
		}
		sourceLang = args[0];

		if (sourceLang.equalsIgnoreCase("chi")) {
			targetLang = "eng";
		} else {
			targetLang = "chi";
		}

		// outputTokens(args);
		parallelMaps = Common.getLines(base + "parallelMap");

//		base = base + "/" + sourceLang + "_MT/";

		for (int i = 0; i < 81; i++) {
			outputCoNLL(i);
		}
		System.out.println(nes);
		System.out.println(sentenceNumber);
	}

	static int sentenceNumber = 0;

	static HashSet<String> nes = new HashSet<String>();
	static ArrayList<String> parallelMaps;

	static ArrayList<String> loadSpeaker(String path) {
		ArrayList<String> speakerLines = Common.getLines(path);
		ArrayList<String> speakers = new ArrayList<String>();
		for (String line : speakerLines) {
			speakers.addAll(Arrays.asList(line.trim().split("\\s+")));
		}
		return speakers;
	}

	static void loadSRL(String path) {
		ArrayList<String> srls = Common.getLines(path);
		srlAmounts.clear();
		srlLabels.clear();
		int sentenceID = 0;

		int sl = 14;
		int verbID = 0;
		int tkID = 0;
		for (String srl : srls) {
			if (srl.trim().isEmpty()) {
				sentenceID++;
				verbID = 0;
			} else {
				String tks[] = srl.split("\\s+");
				int srlAm = tks.length - sl;
				if (!srlAmounts.containsKey(sentenceID)) {
					srlAmounts.put(sentenceID, srlAm);
				}
				if (tks[12].equalsIgnoreCase("Y")) {
					String key = tkID + "_" + verbID;
					srlLabels.put(key, "(V*)");
					verbID++;
				}
				for (int i = 14; i < tks.length; i++) {
					int vID = i - 14;
					String key = tkID + "_" + vID;
					String label = tks[i];
					if (label.equalsIgnoreCase("A0")) {
						srlLabels.put(key, "(ARG0*)");
					} else if (label.equalsIgnoreCase("A1")) {
						srlLabels.put(key, "(ARG1*)");
					}
				}
				tkID++;
			}
		}
	}

	static HashMap<Integer, Integer> srlAmounts = new HashMap<Integer, Integer>();;
	static HashMap<String, String> srlLabels = new HashMap<String, String>();

	private static void outputCoNLL(int id) {
		StanfordResult sr = StanfordXMLReader.read(base + "/stanford/" + id
				+ "." + targetLang + ".xml");

		ArrayList<String> speakers = loadSpeaker(base + "/speaker/" + id + "."
				+ targetLang);

//		if (targetLang.equalsIgnoreCase("eng")) {
//			loadSRL(base + "/srl/" + id + ".srl");
//		}
		String paraMap = parallelMaps.get(id);
		String docName = null;
		if (targetLang.equalsIgnoreCase("chi")) {
			docName = paraMap.split("#")[0].trim();
		} else {
			docName = paraMap.split("#")[1].trim();
		}

		ArrayList<String> lines = new ArrayList<String>();
		lines.add("#begin document (" + docName + "); part 000");


		ArrayList<String> linesSRL = new ArrayList<String>();

		StanfordCoreference scor = sr.coreference;
		ArrayList<ArrayList<StanfordMention>> scs = scor.coreferenceChains;

		HashMap<String, String> corefIDs = new HashMap<String, String>();

		int tkID = 0;
//		loadCoreference(scs, corefIDs);

		for (StanfordSentence ss : sr.sentences) {
			sentenceNumber++;
			String parseTree = ss.parseTree.root.getTreeBankStyle(true)
					.replace("ROOT", "TOP").replace("NP-TMP", "NP");
			int from = 0;
			int to = 0;

			for (int i = 0; i < ss.tokens.size(); i++) {
				StanfordToken token = ss.tokens.get(i);
				StringBuilder sb = new StringBuilder();

				StringBuilder sbSRL = new StringBuilder();

				// documentID
				sb.append(docName).append("\t");
				// partID
				sb.append("0").append("\t");
				// word number
				sb.append(token.id).append("\t");
				// word itself
				sb.append(token.word).append("\t");
				// Part-of-Speech
				sb.append(token.POS).append("\t");
				// Parse bit
				String key = "(" + token.POS + " " + token.word + ")";
				to = parseTree.indexOf(key, from);
				if (to == -1) {
					System.out.println(base + "/stanford/" + id + "."
							+ targetLang + ".xml");
					Common.bangErrorPOS(parseTree);
				}
				// find next (
				String segTree = parseTree.substring(0, to).trim() + "*";

				int till = parseTree.indexOf("(", to + key.length());
				if (till == -1) {
					till = parseTree.length();
				}
				segTree += parseTree.substring(to + key.length(), till);
				sb.append(segTree.replaceAll("\\s+", "")).append("\t");
				parseTree = parseTree.substring(till);
				// Predicate lemma
				String lemma = "-";
				if (!token.lemma.isEmpty()) {
					lemma = token.lemma;
				}
				sb.append(lemma).append("\t");
				// Predicate Frameset ID
				sb.append("-").append("\t");
				// Word sense
				sb.append("-").append("\t");
				// TODO
				// Speaker/Author
				String speaker = "-";
				speaker = speakers.get(tkID);
				sb.append(speaker).append("\t");
				// Named Entities
				String ne = token.ner.replace("ORGANIZATION", "ORG")
						.replace("ORDINAL", "CARDINAL")
						.replace("LOCATION", "LOC");
				nes.add(ne);
				String preNE = "O";
				String nextNE = "O";
				if (i > 0) {
					preNE = ss.tokens.get(i - 1).ner;
				}
				if (i < ss.tokens.size() - 1) {
					nextNE = ss.tokens.get(i + 1).ner;
				}

				String neToken = "*";
				if (!ne.equalsIgnoreCase("O") && !preNE.equals(ne)
						&& !nextNE.equals(ne)) {
					neToken = "(" + ne + ")";
				} else if (!ne.equalsIgnoreCase("O") && !preNE.equals(ne)
						&& nextNE.equals(ne)) {
					neToken = "(" + ne + "*";
				} else if (!ne.equalsIgnoreCase("O") && preNE.equals(ne)
						&& !nextNE.equals(ne)) {
					neToken = "*)";
				}
				sb.append(neToken).append("\t");

				// semantic role labeling
				if (srlAmounts.containsKey(ss.id)) {
					int srlAmount = srlAmounts.get(ss.id);
					for (int k = 0; k < srlAmount; k++) {
						String srlKey = tkID + "_" + k;
						String label = "*";
						if (srlLabels.containsKey(srlKey)) {
							label = srlLabels.get(srlKey);
						}
						sb.append(label).append("\t");
					}
				}

				String corefTk = "-";
				if (corefIDs.containsKey(ss.id + "_" + i)) {
					corefTk = corefIDs.get(ss.id + "_" + i);
				}
				sb.append(corefTk).append("\t");
				lines.add(sb.toString());
				sbSRL.append(tkID).append("\t").append(token.word);
				linesSRL.add(sbSRL.toString());
				
				tkID++;
			}
			lines.add("");
			linesSRL.add("");
		}
		lines.add("#end document");
//		lines.add("");
		Common.outputLines(lines, base + "/conll/" + id + ".conll");

//		if (targetLang.equalsIgnoreCase("eng")) {
//			Common.outputLines(linesSRL, base + "/srl/" + id + ".conll");
//		}
	}

	private static void loadCoreference(
			ArrayList<ArrayList<StanfordMention>> scs,
			HashMap<String, String> corefIDs) {
		for (int i = 0; i < scs.size(); i++) {
			ArrayList<StanfordMention> sc = scs.get(i);
			for (StanfordMention sm : sc) {
				int sentID = sm.sentenceId;
				int start = sm.startId;
				int end = sm.endId;
				if (start == end) {
					String key = sentID + "_" + start;
					String value = "(" + i + ")";
					addCoref(corefIDs, key, value);
				} else {
					String key1 = sentID + "_" + start;
					String value1 = "(" + i;
					addCoref(corefIDs, key1, value1);

					String key2 = sentID + "_" + end;
					String value2 = i + ")";
					addCoref(corefIDs, key2, value2);
				}
			}
		}
	}

	private static void addCoref(HashMap<String, String> corefIDs, String key,
			String value) {
		if (corefIDs.containsKey(key)) {
			corefIDs.put(key, corefIDs.get(key) + "|" + value);
		} else {
			corefIDs.put(key, value);
		}
	}

	static ArrayList<String> overallLines = new ArrayList<String>();

}
