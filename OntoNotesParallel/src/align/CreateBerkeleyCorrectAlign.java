package align;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTreeNode;

import util.Common;
import util.Util;

public class CreateBerkeleyCorrectAlign {

	static String modifyAlign = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlignCor/senAlignOut_modify/";

	// static String originalAign =
	// "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/senAlignOut/";

	public static void main(String args[]) {
		String outputBase = "/users/yzcchen/chen3/ijcnlp2013/wordAlignCor3/";

		ArrayList<String> lines = Common.getLines("parallelMap");

		ArrayList<String> chiSentences = new ArrayList<String>();
		ArrayList<String> engSentences = new ArrayList<String>();

		ArrayList<String> lineNumbers = new ArrayList<String>();
		ArrayList<String> engTreeSentences = new ArrayList<String>();

		for (int i = 0; i < lines.size(); i++) {
			String parallel = lines.get(i);

			String tokens[] = parallel.split("#");
			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
					tokens[1].trim(), "eng", false));

			ArrayList<String> alignContent = Common.getLines(modifyAlign + i
					+ ".align");
			lineNumbers.add(parallel + " : "
					+ Integer.toString(alignContent.size() / 3));
			int engWordID = 0;
			for (int j = 0; j < alignContent.size() / 3; j++) {
				String eng = alignContent.get(j * 3);
				String chi = alignContent.get(j * 3 + 1);

				chiSentences.add(chi.trim().toLowerCase());
				engSentences.add(eng.trim().toLowerCase());

				int enTs = eng.trim().split("\\s+").length;

				CoNLLWord firstEnWord = engDoc.getWord(engWordID);
				CoNLLWord lastEnWord = engDoc.getWord(engWordID + enTs - 1);

				MyTreeNode root = new MyTreeNode("S");
				MyTreeNode subRoot = null;
				CoNLLSentence ss = null;
				for (int m = engWordID; m < engWordID + enTs; m++) {
					CoNLLWord w = engDoc.getWord(m);
					CoNLLSentence tmp = w.sentence;
					if (tmp != ss) {
						tmp.getSyntaxTree().root.value = "S";
						ss = tmp;
						
						subRoot = ss.getSyntaxTree().root.copy();
						subRoot.setAllMark(false);
						
						root.addChild(subRoot);
					}
					
					MyTreeNode leaf = subRoot.getLeaves().get(w.indexInSentence);
					ArrayList<MyTreeNode> ancestors = leaf.getAncestors();
					for(MyTreeNode ance : ancestors) {
						ance.mark = true;
					}
				}

				engTreeSentences.add(root.getTreeBankStyle(true).trim());
				engWordID += enTs;
				
//				System.out.println(root.getPlainText(true).trim());
//				System.out.println(eng.trim().toLowerCase());
//				if(true) {
////					System.exit(1);
//				}
			}
		}

		Common.outputLines(chiSentences, outputBase + "docs/docs.f");
		Common.outputLines(engSentences, outputBase + "docs/docs.e");
		Common.outputLines(engTreeSentences, outputBase + "docs/docs.etrees");
		Common.outputLines(lineNumbers, outputBase + "lineNos");
	}
}
