package ims.coref.training;

import ims.coref.data.Chain;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.markables.IMarkableExtractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SantosCarvalhoExampleExtractor extends AbstractTrainingExampleExtractor{
	private static final long serialVersionUID = 1L;

	public SantosCarvalhoExampleExtractor(IMarkableExtractor markableExtractor,CommitStrategy commitStrategy) {
		super(markableExtractor,commitStrategy);
	}

//	@Override
//	void extractExamplesByChain(Map<Span, Integer> span2int, Chain ch,List<Span> predSpans, List<PairInstance> sink) {
//		Collections.sort(ch.spans);
//		for(int anaChainIndex=ch.spans.size()-1;anaChainIndex>0;--anaChainIndex){
//			if(skipAnaphor(ch, predSpans, anaChainIndex))
//				continue;
//			
//			Span ana=ch.spans.get(anaChainIndex);
//			int anaphorChainID=span2int.get(ana);
//			for(int antIndex=predSpans.indexOf(ana)-1;antIndex>=0;--antIndex){
//				Span ant=predSpans.get(antIndex);
//				Integer antChainID=span2int.get(ant);
//				if(antChainID!=null && antChainID.equals(anaphorChainID)){ //Got the match, break here
//					PairInstance pi=new PairInstance(ant,ana,true);
//					sink.add(pi);
//					int senIdx=ant.s.sentenceIndex;
//					for(antIndex--;antIndex>=0;antIndex--){
//						ant=predSpans.get(antIndex);
//						if(ant.s.sentenceIndex!=senIdx){
//							break;
//						} else {
//							boolean positive=sameChain(ant,ana,span2int);
//							sink.add(new PairInstance(ant,ana,positive));
//						}
//					}
//					
//					break;
//				} else {
//					PairInstance pi=new PairInstance(ant,ana,false);
//					sink.add(pi);
//				}
//			}
//
//		}
//	}

	@Override
	void extractExamplesByChain(Map<Span, Integer> span2int, Chain ch, SpanListStruct sls, List<PairInstance> sink) {
		Collections.sort(ch.spans);
		for(int anaChainIndex=ch.spans.size()-1;anaChainIndex>0;--anaChainIndex){
			if(skipAnaphor(ch, sls, anaChainIndex))
				continue;
			Span ana=ch.spans.get(anaChainIndex);
			int anaphorChainID=span2int.get(ana);
			int anaIndex=sls.indexOf(ana);
			for(int antIndex=sls.indexOf(ana)-1;antIndex>=0;--antIndex){
				Span ant=sls.get(antIndex);
				Integer antChainID=span2int.get(ant);
				if(antChainID!=null && antChainID.equals(anaphorChainID)){ //Got the match, break here
					PairInstance pi=new PairInstance(ant,ana,true,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex));
					sink.add(pi);
					//Then do the remaining positive
					for(int i=ch.spans.indexOf(ant)-1;i>=0;--i){
						ant=ch.spans.get(i);
						antIndex=sls.indexOf(ant);
						if(sls.contains(ant))
							sink.add(new PairInstance(ant,ana,true,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex)));
					}
					break;
				} else {
					PairInstance pi=new PairInstance(ant,ana,false,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex));
					sink.add(pi);
				}
			}
		}
	}

}
