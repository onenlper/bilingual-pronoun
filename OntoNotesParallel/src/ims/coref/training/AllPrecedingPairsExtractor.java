package ims.coref.training;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ims.coref.data.Chain;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.markables.IMarkableExtractor;

public class AllPrecedingPairsExtractor extends AbstractTrainingExampleExtractor{
	private static final long serialVersionUID = 1L;

	protected AllPrecedingPairsExtractor(IMarkableExtractor markableExtractor,CommitStrategy commitStrategy) {
		super(markableExtractor,commitStrategy);
	}

//	@Override
//	void extractExamplesByChain(Map<Span, Integer> span2int, Chain ch, List<Span> predSpans, List<PairInstance> sink) {
//		Collections.sort(ch.spans);
//		for(int anaChainIndex=ch.spans.size()-1;anaChainIndex>0;--anaChainIndex){
//			if(skipAnaphor(ch, predSpans, anaChainIndex))
//				continue;
//			
//			Span ana=ch.spans.get(anaChainIndex);
//			for(int antIndex=predSpans.indexOf(ana)-1;antIndex>=0;--antIndex){
//				Span ant=predSpans.get(antIndex);
//				boolean positive=sameChain(ant,ana,span2int);
//				sink.add(new PairInstance(ant,ana,positive));
//			}
//		}
//	}

	@Override
	void extractExamplesByChain(Map<Span, Integer> span2int, Chain ch, SpanListStruct sls, List<PairInstance> sink) {
		Collections.sort(ch.spans);
		for(int anaChainIndex=ch.spans.size()-1;anaChainIndex>0;--anaChainIndex){
			if(skipAnaphor(ch, sls, anaChainIndex))
				continue;
			
			Span ana=ch.spans.get(anaChainIndex);
			int anaIndex=sls.indexOf(ana);
			for(int antIndex=sls.indexOf(ana)-1;antIndex>=0;--antIndex){
				Span ant=sls.spansLinearOrder[antIndex];
				boolean positive=sameChain(ant,ana,span2int);
				sink.add(new PairInstance(ant,ana,positive,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex)));
			}
		}
	}
}
