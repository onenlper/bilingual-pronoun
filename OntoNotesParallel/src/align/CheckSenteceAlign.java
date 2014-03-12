package align;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;
import util.Common;
import util.Util;

public class CheckSenteceAlign {

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("parallelMap");
		int equal = 0;
		for (int i = 0; i < lines.size(); i++) {
			String parallel = lines.get(i);
			String tokens[] = parallel.split("#");
			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(
					tokens[0].trim(), "chi", true));
//			System.out.println(chiDoc.getParts().size());
			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
					tokens[1].trim(), "eng", true));
//			System.out.println(engDoc.getParts().size());
			if(engDoc.getParts().size()==chiDoc.getParts().size()) {
				equal++;
			}
			System.out.println(parallel + " # " + i + " : " + equal);
			ArrayList<String> alignContent = Common.getLines(Util.modifyAlign + i
					+ ".align");
			int chID = 0;
			int enID = 0;
			for (int j = 0; j < alignContent.size() / 3; j++) {
				String eng = alignContent.get(j * 3);
				if (!eng.trim().equals("################")) {
					for (String tk : eng.split("\\s+")) {
						if (!tk.equals(engDoc.getWord(enID).orig)) {
							Common.bangErrorPOS(tk + " "
									+ engDoc.getWord(enID).orig + " # " + enID
									+ " : " + j + " # " + engDoc.getWord(enID).sentence.getSentenceIdx());
						}
						enID++;
					}
				}
				String chi = alignContent.get(j * 3 + 1);
				if (!chi.trim().equals("################")) {
					for (String tk : chi.split("\\s+")) {
						if (!tk.equals(chiDoc.getWord(chID).orig)) {
							Common.bangErrorPOS(tk + " "
									+ chiDoc.getWord(chID).orig + " # " + chID
									+ " : " + j + " # " + engDoc.getWord(enID).sentence.getSentenceIdx());
						}
						chID++;
					}
				}
			}
			System.out.println(chID + " # " + chiDoc.wordCount);
			if(chID!=chiDoc.wordCount) {
				Common.bangErrorPOS(chID + " # " + chiDoc.wordCount);
			}
			
			System.out.println(enID + " # " + engDoc.wordCount);
			if(enID!=engDoc.wordCount) {
				Common.bangErrorPOS(enID + " # " + engDoc.wordCount);
			}
		}
		System.out.println("Equal Part : "  + equal);
	}

}
