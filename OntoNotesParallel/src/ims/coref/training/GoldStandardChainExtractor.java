package ims.coref.training;

import ims.coref.data.Chain;
import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.Sentence;
import ims.coref.data.Span;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoldStandardChainExtractor {

    private static final Pattern BAR=Pattern.compile("\\|");
    private static final Pattern ONE_TOKEN=Pattern.compile("\\((\\d+)\\)");
    private static final Pattern BEGIN=Pattern.compile("\\((\\d+)");
    private static final Pattern END=Pattern.compile("(\\d+)\\)");
    private static final Pattern BLANK=Pattern.compile("(?:\\*|-|_)");

	
	private Map<Integer,Chain> getGoldChainTreeMap(Document d){
        Map<Integer,Chain> chains=new TreeMap<Integer,Chain>(); 
        Map<Integer,List<Integer>> m=new TreeMap<Integer,List<Integer>>(); //This maps CorefIDs to a list of start tokens
        for(Sentence s:d.sen){ //Parse the coref column here:
                for(int i=1;i<s.forms.length;++i){
                        if(BLANK.matcher(s.corefCol[i]).matches())
                                continue;
                        String[] entries=BAR.split(s.corefCol[i]);
                        for(String e:entries){
                                Matcher m1=ONE_TOKEN.matcher(e);
                                if(m1.matches()){
                                        add(i,i,new Integer(m1.group(1)),chains,s);
                                        continue;
                                }
                                Matcher m2=BEGIN.matcher(e);
                                if(m2.matches()){
                                        Integer chainId=new Integer(m2.group(1));
                                        if(!m.containsKey(chainId)){
                                                m.put(chainId, new ArrayList<Integer>());
                                        }
                                        m.get(chainId).add(i);
                                        continue;
                                }
                                Matcher m3=END.matcher(e);
                                if(m3.matches()){
                                        Integer chainId=new Integer(m3.group(1));
                                        List<Integer> starts=m.get(chainId);
                                        int begin=starts.remove(starts.size()-1);
                                        add(begin,i,chainId,chains,s);
                                } else {
                                        throw new RuntimeException("Strange coreference string: '"+e+"', look into this.");
                                }
                        }
                }
                m.clear();
        }
        for(Chain c:chains.values())
        	Collections.sort(c.spans);
		return chains;
	}

	public Chain[] getGoldChains(Document d){
        Map<Integer,Chain> chains=new HashMap<Integer,Chain>(); //XXX Since we traverse this set we want to make sure that it has an ordering 
        Map<Integer,List<Integer>> m=new TreeMap<Integer,List<Integer>>(); //This maps CorefIDs to a list of start tokens
//        int deleted=0;
        for(Sentence s:d.sen){ //Parse the coref column here:
                for(int i=1;i<s.forms.length;++i){
                        if(BLANK.matcher(s.corefCol[i]).matches())
                                continue;
                        String[] entries=BAR.split(s.corefCol[i]);
                        for(String e:entries){
                                Matcher m1=ONE_TOKEN.matcher(e);
                                if(m1.matches()){
                                        add(i,i,new Integer(m1.group(1)),chains,s);
                                        continue;
                                }
                                Matcher m2=BEGIN.matcher(e);
                                if(m2.matches()){
                                        Integer chainId=new Integer(m2.group(1));
                                        if(!m.containsKey(chainId)){
                                                m.put(chainId, new ArrayList<Integer>());
                                        }
                                        m.get(chainId).add(i);
                                        continue;
                                }
                                Matcher m3=END.matcher(e);
                                if(m3.matches()){
                                        Integer chainId=new Integer(m3.group(1));
                                        List<Integer> starts=m.get(chainId);
                                        int begin=starts.remove(starts.size()-1);
                                        add(begin,i,chainId,chains,s);
                                } else {
                                        throw new RuntimeException("Strange coreference string: '"+e+"', look into this.");
                                }
                        }
                }
                m.clear();
        }
        Chain[] r=new Chain[chains.size()];
        int i=0;
        for(Chain c:chains.values()){
        	Collections.sort(c.spans);
        	r[i++]=c;
        }
        Arrays.sort(r);
		return r;
	}
	
	public static void extractGoldSpans(Sentence s,Set<Span> sink){
        Map<Integer,List<Integer>> m=new TreeMap<Integer,List<Integer>>(); //This maps CorefIDs to a list of start tokens
		for(int i=1;i<s.forms.length;++i){
			if(BLANK.matcher(s.corefCol[i]).matches())
                continue;
			String[] entries=BAR.split(s.corefCol[i]);
			for(String e:entries){
                Matcher m1=ONE_TOKEN.matcher(e);
                if(m1.matches()){
                		sink.add(s.getSpan(i, i));
                        continue;
                }
                Matcher m2=BEGIN.matcher(e);
                if(m2.matches()){
                        Integer chainId=new Integer(m2.group(1));
                        if(!m.containsKey(chainId)){
                                m.put(chainId, new ArrayList<Integer>());
                        }
                        m.get(chainId).add(i);
                        continue;
                }
                Matcher m3=END.matcher(e);
                if(m3.matches()){
                        Integer chainId=new Integer(m3.group(1));
                        List<Integer> starts=m.get(chainId);
                        int begin=starts.remove(starts.size()-1);
                        sink.add(s.getSpan(begin, i));
                } else {
                        throw new RuntimeException("Strange coreference string: '"+e+"', look into this.");
                }
        }
		}
	}

    
	public CorefSolution getGoldCorefSolution(Document d){
		Map<Integer,Chain> m=getGoldChainTreeMap(d);
		CorefSolution cs=new CorefSolution(m);
		return cs;
	}


	private void add(int begin, int end, Integer chainId,Map<Integer, Chain> chains, Sentence s) {
		Span span=s.getSpan(begin, end);
		Chain c=chains.get(chainId);
		if(c==null){
			c=new Chain(chainId,span);
			chains.put(chainId,c);
		} else {
			c.addSpan(span);
		}
	}
	
	
}
