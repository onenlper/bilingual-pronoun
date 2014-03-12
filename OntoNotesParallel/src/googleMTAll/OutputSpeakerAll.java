package googleMTAll;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;
import util.Common;
import util.Util;

public class OutputSpeakerAll {

	public static void main(String args[]) {
		String lang = args[0];
		String origLang = "";
		String tranLang = "";

		if (lang.equalsIgnoreCase("chi")) {
			origLang = "chi";
			tranLang = "eng";
		} else {
			origLang = "eng";
			tranLang = "chi";
		}

		String base = "/users/yzcchen/chen3/ijcnlp2013/googleMTALL/"
				+ origLang + "_MT/";

		ArrayList<String> files = new ArrayList<String>();
		if (origLang.equalsIgnoreCase("chi")) {
			files = Common.getLines("chinese_list_all");
		} else {
			files = Common.getLines("english_list_all");
		}
		
		base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/";
		files =  Common.getLines("/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/align/parallelMap");
		
		for (int i = 0; i < files.size(); i++) {
			String filePath = files.get(i);
			String docID = Util.getID(filePath);
			CoNLLDocument document = null;
			System.out.println(files.get(i));
			if (lang.equalsIgnoreCase("chi")) {
				document = new CoNLLDocument(Util.getFullPath(docID.trim(),
						"chi", true));
			} else {
				document = new CoNLLDocument(Util.getFullPath(docID.trim(),
						"eng", true));
			}

			ArrayList<String> origLines = Common.getLines(base + "/align/" + i
					+ "." + origLang);
			ArrayList<String> tranLines = Common.getLines(base + "/align/" + i
					+ "." + tranLang);

			int origIndex = 0;

			ArrayList<String> speakers = new ArrayList<String>();

			for (int j = 0; j < origLines.size(); j++) {
				String origLine = origLines.get(j).trim();
				String speaker = document.getWord(origIndex).speaker;
				String word = document.getWord(origIndex).word;
				String tranLine = tranLines.get(j);
				StringBuilder sb = new StringBuilder();
				for (int m = 0; m < tranLine.trim().split("\\s+").length; m++) {
					sb.append(speaker).append(" ");
				}
				speakers.add(sb.toString().trim());

//				origIndex += origLine.split("\\s+").length;
				
				String tks[] = origLine.split("\\s+");
				for(int h=0;h<tks.length;h++) {
					String w = tks[h];
					if(w.equalsIgnoreCase(document.getWord(origIndex).word)) {
						origIndex++;
					}
					
				}
				
				String speaker2 = document.getWord(origIndex-1).speaker;
				String word2 = document.getWord(origIndex-1).word;
				if(!speaker.equalsIgnoreCase(speaker2)) {
					System.out.println(word + "\t" + word2);
					System.out.println(origLine);
					System.out.println(speaker + " " + speaker2 + " : " + i + " " + j);
					Common.bangErrorPOS("");
				}
			}
			Common.outputLines(speakers, base + "/speaker/" + i + "."
					+ tranLang);
		}
	}
}
