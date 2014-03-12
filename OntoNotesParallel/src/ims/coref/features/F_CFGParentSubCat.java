package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.CFGTree.Node;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_CFGParentSubCat extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;

	protected F_CFGParentSubCat(TargetSpanExtractor tse, int cutOff) {
		super(tse.ts.toString()+"CFGParentSubCat", cutOff);
		this.tse=tse;
	}
	
	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		Span s=tse.getSpan(instance);
		Node n=s.cfgNode==null?s.s.ct.getMinimalIncludingNode(s.start, s.end):s.cfgNode.getParent();
		String st=s.s.ct.subCat(n);
		return st;

	}
	
	
}
