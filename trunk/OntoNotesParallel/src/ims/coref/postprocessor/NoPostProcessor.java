package ims.coref.postprocessor;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.SpanListStruct;

public class NoPostProcessor implements IChainPostProcessor{
	@Override
	public final void postProcess(CorefSolution cs, Document d, SpanListStruct sls) {
	}
	
	public String toString(){
		return "No chain post processing";
	}
}
