package ims.headrules;

import java.util.List;
import java.util.Map;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.Sentence;
import ims.headrules.HeadRules.Direction;
import ims.headrules.HeadRules.Rule;

public class HeadFinder {

	private final Map<String,HeadRules> m;
	
	public HeadFinder(Map<String,HeadRules> m){
		this.m=m;
	}
	
	public int findHead(Sentence s,Node node){
		if(node==null)
			return -1;
		if(node.beg==node.end)
			return node.beg;
		HeadRules hr=m.get(node.getLabel());
		if(hr==null){
//			System.out.println("Couldnt find head rules for label: "+node.getLabel());
			return -1;
//			System.exit(1);
		}
		for(Rule r:hr.rules){
			int h=findHead(r,s,node);
			if(h>0)
				return h;
//			else
//				return node.end;
		}
		return node.end;
//		throw new RuntimeException("Failed to find head.");
	}
	
	public int findHead(Sentence s,int beg,int end){
		throw new Error("not implemented");
	}
	

	private int findHead(Rule r, Sentence s, Node node) {
		if(node.beg==node.end)
			return node.beg;
		List<Node> children=node.getChildren();
		if(r.d==Direction.LeftToRight){
			for(Node c:children)
				if(r.headPOSPattern.matcher(c.getLabel()).matches())
					return findHead(s,c);
			
		} else {
			for(int k=children.size()-1;k>=0;--k){
				Node n=children.get(k);
				if(r.headPOSPattern.matcher(n.getLabel()).matches())
					return findHead(s,n);
			}
		}
		return 0;
	}
	
}
