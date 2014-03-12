package ims.coref.features;

import java.util.ArrayList;
import java.util.List;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public class F_CFGSSPath extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;

	protected F_CFGSSPath(int cutOff) {
		super("CFGSSPath", cutOff);
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		if(instance.ant.s!=instance.ana.s)
			return null;
		//XXX although the other way of handling when the cfgNode is null makes sense, it seems like this works better.
		if(instance.ant.cfgNode==null){
			if(instance.ana.cfgNode==null){
				return "<both-null>";
			} else {
				return "<ant-null>";
			}
		} else if(instance.ana.cfgNode==null){
			return "<ana-null>";
		}
		Node antNode=instance.ant.cfgNode;
		Node anaNode=instance.ana.cfgNode;
//		Node antNode=(instance.ant.cfgNode==null?instance.ant.s.ct.getMinimalIncludingNode(instance.ant.start, instance.ant.end):instance.ant.cfgNode);
//		Node anaNode=(instance.ana.cfgNode==null?instance.ana.s.ct.getMinimalIncludingNode(instance.ana.start, instance.ana.end):instance.ana.cfgNode);
		StringBuilder sb;
		if(antNode==anaNode){
			sb=new StringBuilder(antNode.getLabel());
		} else if(dominates(antNode,anaNode)){
			sb=getAnaphorDescendantOfAntecedentPath(antNode,anaNode);
		} else if(dominates(anaNode,antNode)){
			sb=getAntecedentDescendantOfAnaphorPath(antNode,anaNode);
		} else {
			sb=getNonEmbeddingPath(antNode,anaNode);
		}
		if(instance.ana.cfgNode==null)
			sb.insert(0, UP);
		if(instance.ant.cfgNode==null)
			sb.append(DOWN);
		return sb.toString();
	}
	
	private StringBuilder getNonEmbeddingPath(Node antNode,Node anaNode) {
		StringBuilder sb=new StringBuilder(anaNode.getLabel());
		Node lca=anaNode.getParent();
		while(!dominates(lca,antNode)){
			sb.append(UP).append(lca.getLabel());
			lca=lca.getParent();
		}
		//Then add the link to the lca
		sb.append(UP).append(lca.getLabel());
		//Then go down the path to the ant (make a list and traverse it backwards)
		List<Node> nl=new ArrayList<Node>();
		nl.add(antNode);
		Node n=antNode;
		while((n=n.getParent())!=lca)
			nl.add(n);
		for(int i=nl.size()-1;i>=0;--i){
			sb.append(DOWN).append(nl.get(i).getLabel());
		}
		return sb;
	}

	private StringBuilder getAntecedentDescendantOfAnaphorPath(Node antNode,Node anaNode) {
		List<Node> nl=new ArrayList<Node>();
		Node n=antNode;
		nl.add(n);
		while((n=n.getParent())!=anaNode)
			nl.add(n);
		StringBuilder sb=new StringBuilder(anaNode.getLabel());
		for(int i=nl.size()-1;i>=0;--i)
			sb.append(DOWN).append(nl.get(i).getLabel());
		return sb;
	}

	private StringBuilder getAnaphorDescendantOfAntecedentPath(Node antNode,Node anaNode) {
		StringBuilder sb=new StringBuilder(anaNode.getLabel());
		Node p=anaNode;
		while((p=p.getParent())!=antNode)
			sb.append(UP).append(p.getLabel());
		sb.append(UP).append(antNode.getLabel());
		return sb;
	}

	public boolean dominates(Node anc,Node desc){
		return desc.beg>=anc.beg && desc.end<=anc.end;
	}

}
