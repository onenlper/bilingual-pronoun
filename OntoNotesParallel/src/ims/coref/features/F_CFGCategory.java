package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.CFGTree.Node;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_CFGCategory extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	
	protected F_CFGCategory(TargetSpanExtractor tse, int cutOff) {
		super(tse.ts.toString()+"CFGCategory", cutOff);
		this.tse=tse;
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		Span s=tse.getSpan(instance);
		Node n=s.cfgNode;
		if(n==null)
			return "<none>";
		return n.getLabel();
	}

}
