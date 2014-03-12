package align;

import java.util.ArrayList;
import java.util.HashMap;

import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLSentence;
import util.Common;
import util.Util;

public class Align {

	static HashMap<String, String> chiParals = new HashMap<String, String>();
	
	static HashMap<String, String> engParals = new HashMap<String, String>();
	
	static void loadParalsMap() {
		ArrayList<String> lines = Common.getLines("");
		
	}
	
	public static void main(String args[]) {
		ArrayList<String> paraMap = Common.getLines("parallelMap");
		ArrayList<String> alignSens = new ArrayList<String>();
		for (String parallel : paraMap) {
			String tokens[] = parallel.split("#");
			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(
					tokens[0].trim(), "chi", true));

			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
					tokens[1].trim(), "eng", true));

			System.out.println(parallel +  " # " + chiDoc.getSentences().size() + ":" + engDoc.getSentences().size());
			ArrayList<String> chiLines = new ArrayList<String>();
			for(CoNLLSentence sentence : chiDoc.getSentences()) {
				chiLines.add(sentence.getText());
			}
			String chiPath = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/chi/" + tokens[0].trim().replace("/", "-");
			Common.outputLines(chiLines, chiPath);
			ArrayList<String> engLines = new ArrayList<String>();
			for(CoNLLSentence sentence : engDoc.getSentences()) {
				engLines.add(sentence.getText());
			}
			String engPath = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/eng/" + tokens[1].trim().replace("/", "-");
			Common.outputLines(engLines, engPath);
			alignSens.add(engPath + " " + chiPath);
//			for (int i = 0; i < chiDoc.getSentences().size()
//					&& i < engDoc.getSentences().size(); i++) {
//				CoNLLSentence chiSen = chiDoc.getSentences().get(i);
//				CoNLLSentence engSen = engDoc.getSentences().get(i);
//
//				System.out.println(chiSen.getText());
//				System.out.println(engSen.getText());
//				System.out.println("--------------");
//			}
		}
		Common.outputLines(alignSens, "alignSens");
	}
}
