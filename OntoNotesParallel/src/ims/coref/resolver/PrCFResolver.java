package ims.coref.resolver;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.features.FeatureSet;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.postprocessor.IChainPostProcessor;
import ims.ml.liblinear.LibLinearModel;

public class PrCFResolver extends AbstractResolver {

	private final double th;
	
	public PrCFResolver(IMarkableExtractor me, LibLinearModel llModel,FeatureSet fs, IChainPostProcessor pp, int decodeWindow, double th,SingleLinkConstraint slc) {
		super(me, llModel, fs, pp, decodeWindow,slc);
		this.th=th;
	}

	@Override
	CorefSolution doResolve(SpanListStruct spans,Document d) {
		CorefSolution cs=new CorefSolution();
		for(int anaIndex=1;anaIndex<spans.size();++anaIndex){
			if(spans.get(anaIndex).isPronoun) {
				Span s = spans.get(anaIndex);
				Span xs = s.getXSpan();
//				System.out.println(s.getText() + " # " + (xs==null?"":xs.getText()));
				resolveClosestFirst(spans,cs,anaIndex,d);
			}
			else {
				resolveBestLink(spans,cs,anaIndex,th,d);
			}
		}
		return cs;
	}
	
	public String toString() {
		return this.getClass().getCanonicalName()+"  (th: "+th+"; SLC: "+slc+")";
	}
	
}
