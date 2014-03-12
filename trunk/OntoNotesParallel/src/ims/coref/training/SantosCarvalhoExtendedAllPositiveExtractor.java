package ims.coref.training;

import ims.coref.data.Chain;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.markables.IMarkableExtractor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Here we do SantosCarvalho + that we add examples from all preceding sentences where additional positive examples occur
 * 
 * @author anders
 *
 */
public class SantosCarvalhoExtendedAllPositiveExtractor extends AbstractTrainingExampleExtractor{
	private static final long serialVersionUID = 1L;

	protected SantosCarvalhoExtendedAllPositiveExtractor(IMarkableExtractor markableExtractor,CommitStrategy commitStrategy) {
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
//					Span firstPositive=ant;
//					PairInstance pi=new PairInstance(ant,ana,true);
//					sink.add(pi);
//					int senIdx=ant.s.sentenceIndex;
//					for(antIndex--;antIndex>=0;antIndex--){
//						ant=predSpans.get(antIndex);
//						if(ant.s.sentenceIndex!=senIdx){
//							//Here do the additional sentences
//							Set<Integer> handledSenIds=new HashSet<Integer>();
//							handledSenIds.add(senIdx);
//							//Overall loop for every previous positive example
//							for(int antChIndex=ch.spans.indexOf(firstPositive)-1;antChIndex>=0;--antChIndex){
//								Span nextPosAnt=ch.spans.get(antChIndex);
//								if(!predSpans.contains(nextPosAnt) || handledSenIds.contains(nextPosAnt.s.sentenceIndex))
//									continue;
//								handledSenIds.add(nextPosAnt.s.sentenceIndex);
//								//Move antIndex back to point to a span in the sentence of the next positive ant
//								for(antIndex--;antIndex>=0;antIndex--)
//									if(predSpans.get(antIndex).s.sentenceIndex==nextPosAnt.s.sentenceIndex)
//										break;
//								if(antIndex>=0){
//									//Extract all examples from that sentence
//									ant=predSpans.get(antIndex);
//									for(;ant.s.sentenceIndex==nextPosAnt.s.sentenceIndex && antIndex>=0;){
//										ant=predSpans.get(antIndex--);
//										boolean pos=sameChain(ant,ana,span2int);
//										sink.add(new PairInstance(ant,ana,pos));
//									}
//								}
//							}
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
//		}
//	}

	@Override
	void extractExamplesByChain(Map<Span, Integer> span2int, Chain ch,SpanListStruct sls, List<PairInstance> sink) {
		Collections.sort(ch.spans);
		for(int anaChainIndex=ch.spans.size()-1;anaChainIndex>0;--anaChainIndex){
			if(skipAnaphor(ch, sls, anaChainIndex))
				continue;
			
			Span ana=ch.spans.get(anaChainIndex);
			int anaphorChainID=span2int.get(ana);
			for(int anaIndex=sls.indexOf(ana),antIndex=anaIndex-1;antIndex>=0;--antIndex){
				Span ant=sls.get(antIndex);
				Integer antChainID=span2int.get(ant);
				if(antChainID!=null && antChainID.equals(anaphorChainID)){ //Got the match, break here
					Span firstPositive=ant;
					PairInstance pi=new PairInstance(ant,ana,true,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex));
					sink.add(pi);
					int senIdx=ant.s.sentenceIndex;
					for(antIndex--;antIndex>=0;antIndex--){
						ant=sls.get(antIndex);
						if(ant.s.sentenceIndex!=senIdx){
							//Here do the additional sentences
							Set<Integer> handledSenIds=new HashSet<Integer>();
							handledSenIds.add(senIdx);
							//Overall loop for every previous positive example
							for(int antChIndex=ch.spans.indexOf(firstPositive)-1;antChIndex>=0;--antChIndex){
								Span nextPosAnt=ch.spans.get(antChIndex);
								if(!sls.contains(nextPosAnt) || handledSenIds.contains(nextPosAnt.s.sentenceIndex))
									continue;
								handledSenIds.add(nextPosAnt.s.sentenceIndex);
								//Move antIndex back to point to a span in the sentence of the next positive ant
								for(antIndex--;antIndex>=0;antIndex--)
									if(sls.get(antIndex).s.sentenceIndex==nextPosAnt.s.sentenceIndex)
										break;
								if(antIndex>=0){
									//Extract all examples from that sentence
									ant=sls.get(antIndex);
									for(;ant.s.sentenceIndex==nextPosAnt.s.sentenceIndex && antIndex>=0;){
										ant=sls.get(antIndex--);
//										antIndex=sls.indexOf(ant);
										boolean pos=sameChain(ant,ana,span2int);
										sink.add(new PairInstance(ant,ana,pos,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex)));
									}
								}
							}
							break;
						} else {
							boolean positive=sameChain(ant,ana,span2int);
							sink.add(new PairInstance(ant,ana,positive,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex)));
						}
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
