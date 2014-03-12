package ims.coref.features;

import java.util.ArrayList;
import java.util.List;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public class F_CFGDSPath extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;

	protected F_CFGDSPath(int cutOff) {
		super("CFGDSPath", cutOff);
	}

	@Override
	public String extractStringValue(PairInstance instance, Document d) {
		if(instance.ant.s==instance.ana.s)
			return null;
		if(instance.ana.cfgNode==null || instance.ant.cfgNode==null)
			return "<null>";
		StringBuilder sb=new StringBuilder();
		
		Node anaStartNode;
		if(instance.ana.cfgNode==null){
			anaStartNode=instance.ana.s.ct.getMinimalIncludingNode(instance.ana.start, instance.ana.end);
			sb.append(UP);
		} else {
			anaStartNode=instance.ana.cfgNode;
		}
		
		
		
		sb.append(anaStartNode.getLabel());
		{
			Node n=anaStartNode;
			while((n=n.getParent())!=null)
				sb.append(UP).append(n.getLabel());
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
			for(int i=nl.size()-1;i>=0;--i)
				sb.append(DOWN).append(nl.get(i).getLabel());
		}
		if(instance.ant.cfgNode==null)
			sb.append(DOWN);
		return sb.toString();
	}

}
