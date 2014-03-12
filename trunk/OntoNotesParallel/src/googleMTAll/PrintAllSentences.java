package googleMTAll;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;

import util.Common;
import util.Util;

public class PrintAllSentences {

	public static void main(String args[]) {
		if (args.length != 1) {
			Common.bangErrorPOS("java ~ lang");
		}
		String sourceLang = args[0];
		ArrayList<String> files = new ArrayList<String>();
		if (sourceLang.equalsIgnoreCase("chi")) {
			files = Common.getLines("chinese_list_all");
		} else {
			files = Common.getLines("english_list_all");
		}
		ArrayList<String> lineNos = new ArrayList<String>();

		String base = "/users/yzcchen/chen3/ijcnlp2013/googleMTALL/"
				+ sourceLang + "_MT/";

		String outputAll = "";
		if (sourceLang.equalsIgnoreCase("chi")) {
			outputAll = base + "/docs/docs.f";
		} else {
			outputAll = base + "/docs/docs.e";
		}
		int overall = 0;
		ArrayList<String> allLines = new ArrayList<String>();
		for (int i = 0; i < files.size(); i++) {
			String file = files.get(i);
			String id = Util.getID(file);
			CoNLLDocument doc = null;
			if (sourceLang.equalsIgnoreCase("chi")) {
				doc = new CoNLLDocument(Util.getFullPath(id, "chi", true));
			} else {
				doc = new CoNLLDocument(Util.getFullPath(id, "eng", true));
			}

			ArrayList<String> oneDocLines = getLines(doc);
			overall += oneDocLines.size();
			lineNos.add(files.get(i) + " : " + oneDocLines.size());
			allLines.addAll(oneDocLines);
		}
		System.out.println(overall);
		Common.outputLines(allLines, outputAll);
		Common.outputLines(lineNos, base + "/lineNos");
	}

	public static ArrayList<String> getLines(CoNLLDocument doc) {
		ArrayList<String> lines = new ArrayList<String>();
		for (CoNLLPart part : doc.getParts()) {
			for (CoNLLSentence s : part.getCoNLLSentences()) {

				String line = s.getText()
//						.replace(" n't", "n't").replace(" '", "'")
						;

				lines.add(line);
			}
		}
		return lines;
	}

}
