package ims.coref.features;

import java.util.ArrayList;
import java.util.List;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.CFGTree.Node;
import ims.coref.data.Sentence;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_CFGSSHeadTraitPath extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private final TokenTraitExtractor tte;
	
	protected F_CFGSSHeadTraitPath(TokenTrait tt, int cutOff) {
		super("CFGSS"+tt.toString()+"Path", cutOff);
		this.tte=new TokenTraitExtractor(tt);
	}

	
	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		if(instance.ant.s!=instance.ana.s)
			return null;
		if(instance.ant.cfgNode==null){
			if(instance.ana.cfgNode==null){
				return null;
			} else {
				return null;
			}
		} else if(instance.ana.cfgNode==null){
			return null;
		}
		Node antNode=instance.ant.cfgNode;
		Node anaNode=instance.ana.cfgNode;
		StringBuilder sb;
		if(antNode==anaNode){
			sb=new StringBuilder(getHeadTrait(anaNode,instance.ana.s));
		} else if(dominates(antNode,anaNode)){
			sb=getAnaphorDescendantOfAntecedentPath(antNode,anaNode,instance.ana.s);
		} else if(dominates(anaNode,antNode)){
			sb=getAntecedentDescendantOfAnaphorPath(antNode,anaNode,instance.ana.s);
		} else {
			sb=getNonEmbeddingPath(antNode,anaNode,instance.ana.s);
		}
		if(instance.ana.cfgNode==null)
			sb.insert(0, UP);
		if(instance.ant.cfgNode==null)
			sb.append(DOWN);
		return sb.toString();
	}
	
	private StringBuilder getNonEmbeddingPath(Node antNode,Node anaNode, Sentence s) {
		StringBuilder sb=new StringBuilder(getHeadTrait(anaNode,s));
		int lastHead=anaNode.getHead();
		Node lca=anaNode.getParent();
		while(!dominates(lca,antNode)){
			if(lastHead!=lca.getHead())
				sb.append(UP).append(getHeadTrait(lca,s));
			lastHead=lca.getHead();
			lca=lca.getParent();
		}
		//Then add the link to the lca
		sb.append(UP).append(getHeadTrait(lca,s));
		//Then go down the path to the ant (make a list and traverse it backwards)
		List<Node> nl=new ArrayList<Node>();
		nl.add(antNode);
		Node n=antNode;
		while((n=n.getParent())!=lca)
			nl.add(n);
		for(int i=nl.size()-1;i>=0;--i){
			Node q=nl.get(i);
			if(lastHead!=q.getHead())
				sb.append(DOWN).append(getHeadTrait(q,s));
			lastHead=q.getHead();
		}
		return sb;
	}

	private StringBuilder getAntecedentDescendantOfAnaphorPath(Node antNode,Node anaNode, Sentence s) {
		List<Node> nl=new ArrayList<Node>();
		Node n=antNode;
		nl.add(n);
		while((n=n.getParent())!=anaNode)
			nl.add(n);
		StringBuilder sb=new StringBuilder(getHeadTrait(anaNode,s));
		int lastHead=-1;
		for(int i=nl.size()-1;i>=0;--i){
			Node q=nl.get(i);
			if(lastHead!=q.getHead())
				sb.append(DOWN).append(getHeadTrait(q,s));
			lastHead=q.getHead();
		}
		return sb;
	}

	private StringBuilder getAnaphorDescendantOfAntecedentPath(Node antNode,Node anaNode, Sentence s) {
		StringBuilder sb=new StringBuilder(getHeadTrait(anaNode,s));
		Node p=anaNode;
		int lastHead=anaNode.getHead();
		while((p=p.getParent())!=antNode){
			if(p.getHead()!=lastHead)
				sb.append(UP).append(getHeadTrait(p,s));
			lastHead=p.getHead();
		}
		if(antNode.getHead()!=lastHead)
			sb.append(UP).append(getHeadTrait(antNode,s));
		return sb;
	}


	public boolean dominates(Node anc,Node desc){
		return desc.beg>=anc.beg && desc.end<=anc.end;
	}

	private String getHeadTrait(Node node,Sentence s){
		return tte.getTrait(s, node.getHead());
	}
}
