package ims.coref.resolver;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.SpanListStruct;
import ims.coref.features.FeatureSet;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.postprocessor.IChainPostProcessor;
import ims.ml.liblinear.LibLinearModel;

public class BestLinkResolver extends AbstractResolver {

	private final double th;
	
	public BestLinkResolver(IMarkableExtractor me, LibLinearModel llModel,FeatureSet fs,IChainPostProcessor pp,int decodeWindow,double decodeTH,SingleLinkConstraint slc) {
		super(me, llModel, fs, pp, decodeWindow,slc);
		th=decodeTH;
	}
	
	@Override
	public CorefSolution doResolve(SpanListStruct sls,Document d) {
		CorefSolution cs=new CorefSolution();
		for(int anaIndex=1;anaIndex<sls.size();++anaIndex)
			resolveBestLink(sls,cs,anaIndex,th,d);
		return cs;
	}


	public String toString() {
		return this.getClass().getCanonicalName()+"  (th: "+th+"; SLC: "+slc+")";
	}
}
