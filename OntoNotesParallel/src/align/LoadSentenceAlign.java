package align;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;

import util.Common;
import util.Util;

public class LoadSentenceAlign {

//	chiSize: 456467.0
//	engSize: 530465.0
//	0.8605035204961684

	
	static String matchBase = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/align/";
	static String outputBase = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/senAlignOut/";

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("parallelMap");
		double chiSize = 0;
		double engSize = 0;
		for (int i = 0; i < lines.size(); i++) {
			String parallel = lines.get(i);
			System.out.println(parallel);
			String tokens[] = parallel.split("#");
			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(
					tokens[0].trim(), "chi", true));

			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
					tokens[1].trim(), "eng", true));
			ArrayList<String> aligns = new ArrayList<String>();
			ArrayList<String> matches = Common.getLines(matchBase + i
					+ ".match");

			System.out.println(chiDoc.getSentences().size() + " # "
					+ engDoc.getSentences().size() + " # " + i);

			for (int j = 0; j < matches.size(); j++) {
				String match = matches.get(j);
				String tks[] = match.split("<=>");

				String enSide = tks[0];
				String chSide = tks[1];

				if (enSide.trim().equals("omitted")) {
					aligns.add("################");
				} else {
					StringBuilder sb = new StringBuilder();
					String enIDs[] = enSide.split(",");
					for (String enID : enIDs) {
						int sid = Integer.parseInt(enID.trim()) - 1;
						String text = engDoc.getSentence(sid).getText();
						sb.append(text.trim()).append(" ");
					}
					aligns.add(sb.toString().trim());
					engSize += sb.toString().split("\\s+").length;
				}

				if (chSide.trim().equals("omitted")) {
					aligns.add("################");
				} else {
					StringBuilder sb = new StringBuilder();
					String chIDs[] = chSide.split(",");
					for (String chID : chIDs) {
						int sid = Integer.parseInt(chID.trim()) - 1;
						String text = chiDoc.getSentence(sid).getText();
						sb.append(text.trim()).append(" ");
					}
					aligns.add(sb.toString().trim());
					chiSize += sb.toString().split("\\s+").length;
				}
				aligns.add("============================================");
			}
			Common.outputLines(aligns, outputBase + i + ".align");
		}
		System.out.println("chiSize: " + chiSize);
		System.out.println("engSize: " + engSize);
		System.out.println(chiSize/engSize);
	}
}
