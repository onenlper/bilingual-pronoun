package ims.coref.data;

import java.util.ArrayList;
import java.util.List;

public class Chain implements Comparable<Chain>{

	public List<Span> spans;
	public Integer chainId;
	
	public Chain(Integer id,Span... init){
		this.chainId=id;
		spans=new ArrayList<Span>();
		for(Span s:init)
			spans.add(s);
	}
	
	public void addSpan(Span span) {
		spans.add(span);
	}

	@Override
	public int compareTo(Chain arg0) {
		return chainId-arg0.chainId;
	}

}
