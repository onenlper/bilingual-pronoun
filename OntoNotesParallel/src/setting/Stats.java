package setting;

import java.util.ArrayList;

import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;

public class Stats {
	
	public static void main(String args[]) {
		CoNLLDocument document = new CoNLLDocument("engCoNLL.key.3");
		
		System.out.println(document.getParts().size());
		int k = 0;
		for(CoNLLPart part : document.getParts()) {
			ArrayList<EntityMention> ms = part.getMentions();
			k += part.getCoNLLSentences().size();
		}
		System.out.println(k);
	}
}
