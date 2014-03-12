package align;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTreeNode;
import util.Common;
import util.Util;

public class CreateBerkeleyAlignerToken {

	static String modifyAlign = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/senAlignOut_modify/";

	// static String originalAign =
	// "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/senAlignOut/";

	public static void main(String args[]) {
		if(args.length!=1) {
			Common.bangErrorPOS("java ~ gold|sys");
		}
		boolean gold = true;
		String outputBase = "";
		if(args[0].equalsIgnoreCase("gold")) {
			outputBase = util.Util.tokenBAAlignBaseGold;
			gold = true;
		} else if((args[0].equalsIgnoreCase("sys"))){
			outputBase = util.Util.tokenBAAlignBaseSys;
			gold = false;
		} else {
			Common.bangErrorPOS("java ~ gold|sys");
		}
		
		ArrayList<String> lines = Common.getLines("parallelMap");

		ArrayList<String> chiSentences = new ArrayList<String>();
		ArrayList<String> engSentences = new ArrayList<String>();
		ArrayList<String> engTreeSentences = new ArrayList<String>();
		
		ArrayList<String> lineNumbers = new ArrayList<String>();

		for (int i = 0; i < lines.size(); i++) {
			String parallel = lines.get(i);
			String tokens[] = parallel.split("#");
			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
					tokens[1].trim(), "eng", gold));
			
			ArrayList<String> alignContent = Common.getLines(modifyAlign + i
					+ ".align");
			lineNumbers.add(parallel + " : " + Integer.toString(alignContent.size() / 3));
			
			int engWordID = 0;
			
			for (int j = 0; j < alignContent.size() / 3; j++) {
				String eng = alignContent.get(j * 3);
				String chi = alignContent.get(j * 3 + 1);

				chiSentences.add(chi.trim().toLowerCase());
				engSentences.add(eng.trim().toLowerCase());
				
				int enTs = eng.trim().split("\\s+").length;
				CoNLLWord firstEnWord = engDoc.getWord(engWordID);
				CoNLLWord lastEnWord = engDoc.getWord(engWordID + enTs -1 );
				
				MyTreeNode root = null;
				if(firstEnWord.sentence==lastEnWord.sentence) {
					root = firstEnWord.sentence.getSyntaxTree().root;
					root.value = "S";
				} else {
					root = new MyTreeNode("S");
					
					CoNLLSentence ss = firstEnWord.getSentence();
					ss.getSyntaxTree().root.value = "S";
					root.addChild(ss.getSyntaxTree().root);
					for(int m=engWordID;m<engWordID + enTs;m++) {
						CoNLLSentence tmp = engDoc.getWord(m).sentence;
						if(tmp!=ss) {
							tmp.getSyntaxTree().root.value = "S";
							root.addChild(tmp.getSyntaxTree().root);
							ss = tmp;
						}
					}
				}
//				System.out.println(root.getPlainText(true).trim());
				engTreeSentences.add(root.getTreeBankStyle(true).trim());
				engWordID += enTs;
			}
		}

		Common.outputLines(chiSentences, outputBase + "docs/docs.f");
		Common.outputLines(engSentences, outputBase + "docs/docs.e");
		Common.outputLines(engTreeSentences, outputBase + "docs/docs.etrees");
		Common.outputLines(lineNumbers, "/users/yzcchen/chen3/ijcnlp2013/wordAlign/" + "lineNos");
	}
}
