package align;

import java.util.ArrayList;

import util.Common;

public class CreateAlignInputForGizaToken {

	static String modifyAlign = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/senAlignOut_modify/";

	static String outputBase = "/users/yzcchen/chen3/ijcnlp2013/wordAlign/tokenBase/";
	// static String originalAign =
	// "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/senAlignOut/";

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("parallelMap");
		int equal = 0;
		ArrayList<String> chiSentences = new ArrayList<String>();
		ArrayList<String> engSentences = new ArrayList<String>();
		ArrayList<String> lineNumbers = new ArrayList<String>();

		for (int i = 0; i < lines.size(); i++) {
			String parallel = lines.get(i);
			String tokens[] = parallel.split("#");
//			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(
//					tokens[0].trim(), "chi", true));
//			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
//					tokens[1].trim(), "eng", true));

			ArrayList<String> alignContent = Common.getLines(modifyAlign + i
					+ ".align");
			lineNumbers.add(parallel + " : " + Integer.toString(alignContent.size() / 3));
			for (int j = 0; j < alignContent.size() / 3; j++) {
				String eng = alignContent.get(j * 3);
				String chi = alignContent.get(j * 3 + 1);

				chiSentences.add(chi.trim());
				engSentences.add(eng.trim());
			}

		}
		Common.outputLines(chiSentences, outputBase + "chiDocs");
		Common.outputLines(engSentences, outputBase + "engDocs");
		Common.outputLines(lineNumbers, outputBase + "lineNos");
	}
}
