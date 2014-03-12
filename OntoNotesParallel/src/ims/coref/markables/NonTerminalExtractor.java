package ims.coref.markables;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.CFGTree.NonTerminal;
import ims.coref.data.Sentence;
import ims.coref.data.Span;

import java.util.HashSet;
import java.util.Set;

public class NonTerminalExtractor extends AbstractMarkableExtractor{
	private static final long serialVersionUID = 1L;
	
	final String label;
	
	public NonTerminalExtractor(String label){
		this.label=label;
	}

	@Override
	public void extractMarkables(Sentence s, Set<Span> sink,String docName) {
		Set<Span> newSpans = new HashSet<Span>();
		recurseAndAdd(s.ct.root,newSpans);
		
		// remove smaller one np
//		if(!s.d.genre.equalsIgnoreCase("nw") || s.d.lang.equals("eng")) {
			HashSet<Span> remove = new HashSet<Span>();
			for(Span s1 : newSpans) {
				for(Span s2 : newSpans) {
					if(s1.hd == s2.hd && (s1.end-s1.start>s2.end-s2.start)) {
						s1.miniOne = s2;
						remove.add(s2);
					}
				}
			}
			newSpans.removeAll(remove);
//		}
		
		sink.addAll(newSpans);
		
	}

	private void recurseAndAdd(Node cfgNode,Set<Span> sink){
		if(cfgNode instanceof NonTerminal){
			if(cfgNode.getLabel().equals(label)){
				Span span = cfgNode.getSpan();
				sink.add(span);
			}
			for(Node n:cfgNode.getChildren())
				recurseAndAdd(n,sink);
		}
	}
	
	public String toString(){
		return "NT-"+label;
	}
}
