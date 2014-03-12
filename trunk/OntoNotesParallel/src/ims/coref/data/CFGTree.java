package ims.coref.data;

import ims.coref.lang.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CFGTree {

	private static final Pattern NEWPHRASE_PATTERN=Pattern.compile("^\\(([a-zA-Z]+)");
	private static final Pattern ENDPHRASE_PATTERN=Pattern.compile("^\\*?\\)");
	
	private final Sentence s;
	public final NonTerminal root;
	public final Terminal[] terminals;
	
	public CFGTree(String[] cfgCol,Sentence s){
		this.s=s;
		Stack<NonTerminal> stack=new Stack<NonTerminal>();
		NonTerminal dummy=new NonTerminal(null,0,"DUMMY");
		stack.push(dummy);
		List<Terminal> terminals=new ArrayList<Terminal>();
		terminals.add(null); //For root
		if(cfgCol[1].startsWith("(("))
			cfgCol[1]=cfgCol[1].replaceFirst("\\(\\(", "(TOP(");
		for(int i=1;i<cfgCol.length;++i){
			StringBuilder sb=new StringBuilder(cfgCol[i]);
			while(sb.length()>2){
				Matcher newMatcher=NEWPHRASE_PATTERN.matcher(sb);
				if(newMatcher.lookingAt()){
					NonTerminal phrase=new NonTerminal(stack.peek(),i,newMatcher.group(1));
					stack.peek().addChild(phrase);
					stack.push(phrase);
					sb.delete(0,newMatcher.end());
				} else {
					break;
				}
			}
			Terminal token=new Terminal(stack.peek(),i);
			stack.peek().addChild(token);
			terminals.add(token);
			while(sb.length()>0){
				Matcher endMatcher=ENDPHRASE_PATTERN.matcher(sb);
				if(endMatcher.lookingAt()){
					stack.pop().setEnd(i);
					sb.delete(0, endMatcher.end());
				} else if("*".equals(sb.toString())){
					sb.delete(0, 2);
				} else {
					throw new RuntimeException("Error parsing "+sb);
				}
			}
		}
		if(stack.size()!=1){
			throw new RuntimeException("Error while parsing cfgtree");
		}
		root=(NonTerminal) dummy.getChildren().get(0);
		this.terminals=terminals.toArray(new Terminal[terminals.size()]);
	}
	
	
	public abstract class Node {
		public abstract String getLabel();
		public abstract List<Node> getChildren();
		public int beg;
		public int end;
		private int head=-2;
		NonTerminal parent;
		
		protected Node(NonTerminal parent){
			this.parent=parent;
		}
		
		public Span getSpan(){
			return s.getSpan(beg,end);
//			return s.getSpan(this);
		}
		public int getLeftMost(){
			return beg;
		}
		public int getRightMost(){
			return end;
		}
		public NonTerminal getParent() {
			return parent;
		}
		public int getHead(){
			if(head==-2)
				head=Language.getLanguage(s.d.lang).findNonTerminalHead(s, this);
			return head;
		}
	}
	
	public class NonTerminal extends Node{
		protected NonTerminal(NonTerminal parent,int beg,String label) {
			super(parent);
			this.label=label;
			this.beg=beg;
		}
		
		public void addChild(Node node) {
			children.add(node);
		}

		void setEnd(int end){
			this.end=end;
		}
		String label;
		List<Node> children=new ArrayList<Node>();
		
		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public List<Node> getChildren() {
			return children;
		}
		
		public String toString(){
			StringBuffer sb=new StringBuffer();
			sb.append("("+label+" ");
			for(Node child:children)
				sb.append(child.toString()).append(" ");
			sb.append(")");
			return sb.toString();
		}
	}
	
	public class Terminal extends Node{

		protected Terminal(NonTerminal parent,int beg) {
			super(parent);
			this.beg=beg;
			this.end=beg;
		}

		@Override
		public String getLabel() {
			return s.tags[beg];
		}

		@Override
		public List<Node> getChildren() {
			return Collections.emptyList();
		}
	
		public String toString(){
			return "("+s.tags[beg]+" "+s.forms[beg]+")";
		}

	}

	public Terminal getTerminal(int ind) {
		return terminals[ind];
	}

	public Span minimalCFGNodeSpan(Span pot) {
		Node n=getTerminal(pot.end);
		while(n.beg>pot.start || n.end<pot.end)
			n=n.getParent();
		return n.getSpan();
	}

	public Node getMinimalIncludingNode(int beg, int end) {
		Node last=terminals[end];
		Node next=last.getParent();
//		while(next.beg>beg && next.end<end){
		while(last.end<end || last.beg>beg){
			last=next;
			next=next.getParent();
		}
		return last;
	}

	public String subCat(Node n) {
		if(n==null)
			return "<null>";
		StringBuilder sb=new StringBuilder(n.getLabel()); //The lhs of the production rule (eg NP -> NP PP)
		for(Node c:n.getChildren())
			sb.append(' ').append(c.getLabel());
		return sb.toString();
	}

	public Node getExactNode(int beg, int end) {
		if(beg==end)
			return terminals[beg];
		Node last=terminals[end];
		Node next=last.getParent();
//		while(next.beg>beg && next.end<end){
		while(last.end<end || last.beg>beg){
			last=next;
			next=next.getParent();
		}
		if(last.beg==beg && last.end==end)
			return last;
		else
			return null;
	}
}
