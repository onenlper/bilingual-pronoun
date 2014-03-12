package googleMTAll;

import java.util.ArrayList;
import java.util.HashSet;

import model.stanford.StanfordResult;
import model.stanford.StanfordSentence;
import model.stanford.StanfordToken;
import model.stanford.StanfordXMLReader;
import util.Common;

public class OutputTokenAll {

	static String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/";
	static String lang;

	public static void main(String args[]) {
		if (args.length != 1) {
			Common.bangErrorPOS("java ~ lang");
		}
		lang = args[0];
		outputTokens(args);
	}

	static int sentenceNumber = 0;

	static HashSet<String> nes = new HashSet<String>();
	static ArrayList<String> parallelMaps;

	private static void outputTokens(String[] args) {

		for (int i = 0; i < 81; i++) {
			outputToken(i);
		}

		Common.outputLines(overallLines, base + "/docs/docs." + lang + ".mt");
	}

	static ArrayList<String> overallLines = new ArrayList<String>();

	private static void outputToken(int id) {
		String targetLang = "";
		if(lang.equalsIgnoreCase("chi")) {
			targetLang = "eng";
		} else {
			targetLang = "chi";
		}
		
		
		StanfordResult sr = StanfordXMLReader.read(base + "/stanford/" + id
				+ "." + targetLang + ".xml");
		ArrayList<String> raws = Common.getLines(base + "/raws/" + id + "."
				+ targetLang);

		ArrayList<String> lines = new ArrayList<String>();
		int current = 0;
		ArrayList<Integer> boundries = new ArrayList<Integer>();
		for (int i = 0; i < raws.size(); i++) {
			String raw = raws.get(i);
			boundries.add(current + raw.length());
			current += raw.length() + 1;
		}

		StringBuilder sb = new StringBuilder();
		int nowLine = 0;
		for (StanfordSentence s : sr.sentences) {
			for (StanfordToken tk : s.tokens) {

				int start = tk.CharacterOffsetBegin;
				int end = tk.CharacterOffsetEnd;
				String word = tk.word;

				sb.append(word).append(" ");
				if (end >= boundries.get(nowLine) - 1) {
					lines.add(sb.toString().toLowerCase());
					nowLine++;
					sb = new StringBuilder();
				}
			}
		}
		Common.outputLines(lines, base + "/token/" + id + ".token");
		System.out.println(base + "/token/" + id + ".token");
		overallLines.addAll(lines);
	}

}
