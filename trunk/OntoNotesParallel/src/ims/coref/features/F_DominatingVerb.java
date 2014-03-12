package ims.coref.features;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.extractors.TargetSpanExtractor;

public class F_DominatingVerb extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private final boolean withDirection;
	private final TargetSpanExtractor tse;
	
	protected F_DominatingVerb(TargetSpanExtractor tse, boolean withDirection,int cutOff) {
		super(tse.ts.toString()+"DominatingVerb", cutOff);
		this.tse=tse;
		this.withDirection=withDirection;
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		Span s=tse.getSpan(instance);
		Node n;
		if(s.cfgNode==null)
			n=s.s.ct.getMinimalIncludingNode(s.start, s.end);
		else
			n=s.cfgNode;
		//Now try to find the verb higher up in the cfg tree
		int verbIndex=-1;
		do {
			int head=n.getHead();
			if(s.s.tags[head].startsWith("V")){
				verbIndex=head;
				break;
			}
		} while((n=n.getParent())!=null);
		if(verbIndex!=-1)
			if(withDirection)
				return s.s.forms[verbIndex]+" + "+direction(verbIndex,s);
			else
				return s.s.forms[verbIndex];
		else
			return "<null>";
	}

	private String direction(int verbIndex, Span s) {
		return verbIndex>s.hd ? ">" : "<";
	}

}
