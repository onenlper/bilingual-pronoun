package googleMT;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import util.Common;
import util.Util;

public class PrintSentences {

	public static void main(String args[]) {
		if (args.length != 1) {
			Common.bangErrorPOS("java ~ sourceLang");
		}
		String sourceLang = args[0];
		ArrayList<String> files = Common.getLines("parallelMap");
		ArrayList<String> lineNos = new ArrayList<String>();

		String base = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/"
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
			String tokens[] = files.get(i).split("#");

			CoNLLDocument doc = null;

			if (sourceLang.equalsIgnoreCase("chi")) {
				doc = new CoNLLDocument(Util.getFullPath(tokens[0].trim(),
						"chi", true));
			} else {
				doc = new CoNLLDocument(Util.getFullPath(tokens[1].trim(),
						"eng", true));
			}

			ArrayList<String> oneDocLines = getLines(doc);
			overall += oneDocLines.size();
			lineNos.add(files.get(i) + " : " + oneDocLines.size());
			
			allLines.addAll(oneDocLines);
		}
		System.out.println(overall);
		System.out.println(exceed);
		Common.outputLines(allLines, outputAll);
		Common.outputLines(lineNos, base + "/lineNos");
	}

	static int exceed = 0;

	public static ArrayList<String> getLines(CoNLLDocument doc) {
		ArrayList<String> lines = new ArrayList<String>();
		for (CoNLLPart part : doc.getParts()) {
			for (CoNLLSentence s : part.getCoNLLSentences()) {

				String line = s.getText();
				
				lines.add(line);
			}
		}
		return lines;
	}
}
