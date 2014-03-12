package ims.coref.data;

import java.util.Map;

public class PairInstance {

	public static final boolean USE_FEATURE_CACHING=false;
	
	public final Span ant;
	public final Span ana;
	
	public final boolean corefers;
	
	public boolean rankExtra = false;
	
	private DTPath dtPath;
	public final int mentionDist;
	public final int nesBetween;
//	public final int entitiesBetween
	
	private Map<String,String> featureCache=null; //=new HashMap<String,String>(); 
	
	public PairInstance(Span ant,Span ana,int mentionDist,int nesBetween){
		this(ant,ana,false,mentionDist,nesBetween);
	}
	
	public PairInstance(Span ant,Span ana,boolean corefers,int mentionDist,int nesBetween){
		this.ant=ant;
		this.ana=ana;
		this.corefers=corefers;
		this.mentionDist=mentionDist;
		this.nesBetween=nesBetween;
	}
	
	public boolean getLabel() {
		Span ant = this.ant;
		Span ana = this.ana;
		
		Document d1 = ant.s.d;
		Document d2 = ana.s.d;
		if(d1.docNo.equalsIgnoreCase(d2.docNo) && d1.docName.equalsIgnoreCase(d2.docName)) {
			Integer in1 = d1.goldChainMap.get(ant.getReadName());
			Integer in2 = d2.goldChainMap.get(ana.getReadName());
			if(in1!=null && in2!=null && in1.intValue()==in2.intValue()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public String getText() {
		StringBuilder sb = new StringBuilder();
		
		Span ant = this.ant;
		Span ana = this.ana;
		
		sb.append(ant.getText()).append(" # ").append(ana.getText()).append(" : ").append(this.getLabel());
		
		return sb.toString().trim();
	}

	public String getName() {
		 StringBuilder sb = new StringBuilder();
		 sb.append(this.ant.s.d.lang).append(this.ant.s.d.docName);
		 String antName = this.ant.getReadName();
		 String anaName = this.ana.getReadName();
		 sb.append(" ").append(antName).append(",").append(anaName);
		 return sb.toString().trim();
	}
	
	public PairInstance getXInstance() {
		Span xAnt = this.ant.getXSpan();
		Span xAna = this.ana.getXSpan();
		if(xAnt!=null && xAna!=null) {
			PairInstance xpair = new PairInstance(xAnt, xAna, this.mentionDist, this.nesBetween);
			return xpair;
		} else {
			return null;
		}
	}
	
	public DTPath getDTPath(){
		if(dtPath==null)
			this.dtPath=new DTPath(ant,ana);
		return dtPath;
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append("ant: ").append(ant.s.sentenceIndex).append(",").append(ant.start).append(",").append(ant.end).append("  ");
		sb.append("ana: ").append(ana.s.sentenceIndex).append(",").append(ana.start).append(",").append(ana.end);
		return sb.toString();
	}
	
	public String getCachedFeature(String key){
		return featureCache.get(key);
	}
	public void addToCache(String key,String value){
		featureCache.put(key, value);
	}
	
	public boolean isLone() {
		return this.ant.empty;
	}

}
