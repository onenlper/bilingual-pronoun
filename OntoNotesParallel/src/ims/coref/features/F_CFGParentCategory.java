package ims.coref.features;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_CFGParentCategory extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;
	
	private final TargetSpanExtractor tse;
	
	protected F_CFGParentCategory(TargetSpanExtractor tse, int cutOff) {
		super(tse.ts.toString()+"CFGParentCategory", cutOff);
		this.tse=tse;
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		Span s=tse.getSpan(instance);
		Node n=s.cfgNode;
		if(n==null){
			n=s.s.ct.getMinimalIncludingNode(s.start, s.end);
			if(n==null)
				return "<none>";
			else
				return n.getLabel();
		}
		Node par=n.getParent();
		if(par==null){
			return "<none>";
		} else {
			return par.getLabel();
		}
	}

}
