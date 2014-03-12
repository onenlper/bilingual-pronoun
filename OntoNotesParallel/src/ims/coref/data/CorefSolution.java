package ims.coref.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CorefSolution {

	private final Map<Integer,Chain> chainMap;
	private final Map<Span,Integer> span2int;
	
	private int idCounter=0;
	
	public CorefSolution(){
		this.chainMap=new HashMap<Integer,Chain>();
		this.span2int=new HashMap<Span,Integer>();
	}
	
	public CorefSolution(Map<Integer, Chain> m) {
		this.chainMap=m;
		this.span2int=new HashMap<Span,Integer>();
		for(Entry<Integer,Chain> e:m.entrySet()){
			Integer key=e.getKey();
			for(Span s:e.getValue().spans){
				span2int.put(s, key);
			}
		}
	}

	public void addLink(Span ant, Span ana) {
		Integer chainID=span2int.get(ant);
		if(chainID==null){
			chainID=idCounter++;
			Chain ch=new Chain(chainID,ant,ana);
			chainMap.put(chainID, ch);
//			//XXX
//			if(span2int.containsKey(ant))
//				System.err.println("Ant already mapped");
//			if(span2int.containsKey(ana))
//				System.err.println("Ana already mapped");
			span2int.put(ant, chainID);
			span2int.put(ana, chainID);
		} else {
			Chain ch=chainMap.get(chainID);
			ch.spans.add(ana);
//			//XXX
//			if(span2int.containsKey(ana))
//				System.err.println("Ana already mapped");
			span2int.put(ana, chainID);
		}
	}

	public void addToChain(Chain ch, Span pot) {
		ch.addSpan(pot);
		span2int.put(pot, ch.chainId);
	}

	public List<Span> getSpanList(Integer chainID) {
		return chainMap.get(chainID).spans;
	}

	public int getChainCount() {
		return chainMap.size();
	}

	public int getMentionCount() {
		int count=0;
		for(Chain c:chainMap.values())
			count+=c.spans.size();
		return count;
	}

	public List<Chain> getKey() {
		List<Chain> key=new ArrayList<Chain>(chainMap.values());
		Collections.sort(key); //Make sure we have an ordered list.
		return key;
	}

	public void assignStackMap(Document doc) {
		doc.stackMap=span2int;
	}

	public Integer getSpanChainID(Span s) {
		return span2int.get(s);
	}
	
	public Set<Span> getClonedSpanSet(){
		return new HashSet<Span>(span2int.keySet());
	}

	public int getChainSize(Integer predChainId) {
		Chain c=chainMap.get(predChainId);
		return c.spans.size();
	}
}
