package yypronoun;

import model.CoNLL.CoNLLDocument;
import util.YYFeature;

public abstract class YYPr {

	boolean train;
	
	YYFeature fea;
	
	CoNLLDocument engDoc;
	CoNLLDocument chiDoc;
	
	public YYPr(boolean train, CoNLLDocument engDoc, CoNLLDocument chiDoc) {

		this.engDoc = engDoc;
		
		this.chiDoc = chiDoc;
		
		fea = new PrFea(train, "prFea");
		
		
	}
	
	public void freeze() {
		
	}
	
	
 	
}
