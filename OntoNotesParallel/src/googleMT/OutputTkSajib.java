package googleMT;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import model.stanford.StanfordResult;
import model.stanford.StanfordSentence;
import model.stanford.StanfordToken;
import model.stanford.StanfordXMLReader;
import util.Common;

public class OutputTkSajib {

	static String base = "/users/yzcchen/share/review_train/";
	static String lang;

	public static void main(String args[]) {
		outputTokens();
	}

	static int sentenceNumber = 0;

	static HashSet<String> nes = new HashSet<String>();
	static ArrayList<String> parallelMaps;

	private static void outputTokens() {

		File folder = new File(base);
		for (File f : folder.listFiles()) {
			if (!f.getAbsolutePath().endsWith(".utf8.xml")) {
				continue;
			}

			int k = f.getAbsolutePath().indexOf(".utf8.xml");

			StanfordResult sr = StanfordXMLReader.read(f.getAbsolutePath());
			ArrayList<String> lines = new ArrayList<String>();

			StringBuilder sb = new StringBuilder();
			for (StanfordSentence s : sr.sentences) {
				sb = new StringBuilder();
				for (StanfordToken tk : s.tokens) {

					String word = tk.word;
					sb.append(word).append(" ");
				}
				lines.add(sb.toString().trim());
			}
			Common.outputLines(lines, f.getAbsolutePath().substring(0, k)
					+ ".txt");
		}

	}

}
