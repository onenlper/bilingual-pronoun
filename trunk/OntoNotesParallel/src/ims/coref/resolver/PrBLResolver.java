package ims.coref.resolver;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.SpanListStruct;
import ims.coref.features.FeatureSet;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.postprocessor.IChainPostProcessor;
import ims.ml.liblinear.LibLinearModel;

public class PrBLResolver extends AbstractResolver{

	private final double th;
	
	public PrBLResolver(IMarkableExtractor me, LibLinearModel llModel,FeatureSet fs, IChainPostProcessor pp, int decodeWindow, double th,SingleLinkConstraint slc) {
		super(me, llModel, fs, pp, decodeWindow,slc);
		this.th=th;
	}

	@Override
	CorefSolution doResolve(SpanListStruct sls,Document d) {
		CorefSolution cs=new CorefSolution();
		for(int anaIndex=1;anaIndex<sls.size();++anaIndex){
			if(sls.get(anaIndex).isPronoun)
				resolveBestLink(sls,cs,anaIndex,th,d);
			else
				resolveClosestFirst(sls,cs,anaIndex,d);
		}
		return cs;	
	}

	public String toString() {
		return this.getClass().getCanonicalName()+"  (th: "+th+"; SLC: "+slc+")";
	}
	
}
