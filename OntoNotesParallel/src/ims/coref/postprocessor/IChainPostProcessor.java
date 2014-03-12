package ims.coref.postprocessor;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.SpanListStruct;

public interface IChainPostProcessor {

	public void postProcess(CorefSolution cs,Document d,SpanListStruct sls);
	
}
