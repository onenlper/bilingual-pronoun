package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.CFGTree.Node;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_CFGSubCat extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;

	private final TargetSpanExtractor tse;
	
	protected F_CFGSubCat(TargetSpanExtractor tse, int cutOff) {
		super(tse.ts.toString()+"CFGSubCat", cutOff);
		this.tse=tse;
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		Span s=tse.getSpan(instance);
		Node n=s.cfgNode;
		String st=s.s.ct.subCat(n);
		return st;
	}

}
