package ims.coref.features;

import java.util.ArrayList;
import java.util.List;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Sentence;
import ims.coref.data.CFGTree.Node;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_CFGDSHeadTraitPath extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private final TokenTraitExtractor tte;
	
	protected F_CFGDSHeadTraitPath(TokenTrait tt, int cutOff) {
		super("CFGDS"+tt.toString()+"Path", cutOff);
		this.tte=new TokenTraitExtractor(tt);
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		if(instance.ant.s==instance.ana.s)
			return null;
		if(instance.ana.cfgNode==null || instance.ant.cfgNode==null)
			return null;
		StringBuilder sb=new StringBuilder();
		
		
		
		Node anaStartNode;
		if(instance.ana.cfgNode==null){
			anaStartNode=instance.ana.s.ct.getMinimalIncludingNode(instance.ana.start, instance.ana.end);
			sb.append(UP);
		} else {
			anaStartNode=instance.ana.cfgNode;
		}
		
		
		
		sb.append(getHeadTrait(anaStartNode,instance.ana.s));
		int lastAnaHead=anaStartNode.getHead();
		{
			Node n=anaStartNode;
			while((n=n.getParent())!=null){
				if(n.getHead()!=lastAnaHead)
					sb.append(UP).append(getHeadTrait(n,instance.ana.s));
				lastAnaHead=n.getHead();
			}
		}
		List<Node> nl=new ArrayList<Node>();
		Node antStartNode;
		if(instance.ant.cfgNode==null){
			antStartNode=instance.ant.s.ct.getMinimalIncludingNode(instance.ant.start, instance.ant.end);
		} else {
			antStartNode=instance.ant.cfgNode;
		}
		{
			Node n=antStartNode;
			nl.add(n);
			while((n=n.getParent())!=null)
				nl.add(n);
			int lastAntHead=-2;
			for(int i=nl.size()-1;i>=0;--i){
				Node q=nl.get(i);
				if(q.getHead()!=lastAntHead)
					sb.append(DOWN).append(getHeadTrait(q,instance.ant.s));
				lastAntHead=q.getHead();
			}
		}
		if(instance.ant.cfgNode==null)
			sb.append(DOWN);
		return sb.toString();
	}

	private String getHeadTrait(Node node,Sentence s){
		return tte.getTrait(s, node.getHead());
	}
}
