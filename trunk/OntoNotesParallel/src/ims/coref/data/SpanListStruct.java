package ims.coref.data;

import java.util.Arrays;
import java.util.Collection;

public class SpanListStruct {

	public final Span[] spansLinearOrder;
	public final int[] neCount;
//	public final int[] entityCount;
	
	public static SpanListStruct fromCollection(Collection<Span> spanCollection){
		Span[] spans=spanCollection.toArray(new Span[spanCollection.size()]);
		Arrays.sort(spans);
		return new SpanListStruct(spans);
	}
	
	private SpanListStruct(Span[] spans){
		this.spansLinearOrder=spans;
		this.neCount=getNeCounts(spans);
	}
	
	protected SpanListStruct(SpanListStruct sls) {
		this.spansLinearOrder=sls.spansLinearOrder;
		this.neCount=sls.neCount;
	}

	private int[] getNeCounts(Span[] spans) {
		int[] l=new int[spans.length];
		int c=0;
		for(int i=0;i<spans.length;++i){
			l[i]=c;
			if(spans[i].ne!=null)
				c++;
		}
		return l;
	}
	
	public static int indexOf(Span[] spans,Span s){
		for(int i=0;i<spans.length;++i){
			if(spans[i]==s)
				return i;
		}
		return -1;
	}

	public int indexOf(Span s){
		return indexOf(spansLinearOrder,s);
	}
	
	public Span get(int antIndex) {
		return spansLinearOrder[antIndex];
	}

	public boolean contains(Span ant) {
		return indexOf(ant)>=0;
	}

	public int size() {
		return spansLinearOrder.length;
	}
	
	public int getMentionDist(int antIndex,int anaIndex){
		return Math.abs(antIndex-anaIndex);
	}
	public int getNesBetween(int antIndex,int anaIndex){
		return Math.abs(neCount[antIndex]-neCount[anaIndex]);
	}
}
