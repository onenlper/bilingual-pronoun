package ims.coref.training;

import ims.coref.data.Chain;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.markables.IMarkableExtractor;

import java.util.List;
import java.util.Map;

import util.Common;

public class SoonTrainingExampleExtractor extends
		AbstractTrainingExampleExtractor {
	private static final long serialVersionUID = 1L;

	public SoonTrainingExampleExtractor(IMarkableExtractor markableExtractor,
			CommitStrategy commitStrategy) {
		super(markableExtractor, commitStrategy);
	}

	/**
	 * Note -- we might have the case when we have an anaphor whose closest
	 * antecedent isn't in the predSpans list. Then we have two cases: 1) If
	 * this antecedent is
	 */
	// void extractExamplesByChain(Map<Span,Integer> span2int,Chain
	// ch,List<Span> predSpans,List<PairInstance> sink){
	// Collections.sort(ch.spans);
	// for(int anaChainIndex=ch.spans.size()-1;anaChainIndex>0;--anaChainIndex){
	// if(skipAnaphor(ch, predSpans, anaChainIndex))
	// continue;
	//
	// Span ana=ch.spans.get(anaChainIndex);
	// int anaphorChainID=span2int.get(ana);
	// for(int antIndex=predSpans.indexOf(ana)-1;antIndex>=0;--antIndex){
	// Span ant=predSpans.get(antIndex);
	// Integer antChainID=span2int.get(ant);
	// if(antChainID!=null && antChainID.equals(anaphorChainID)){ //Got the
	// match, break here
	// PairInstance pi=new PairInstance(ant,ana,true);
	// sink.add(pi);
	// break;
	// } else {
	// PairInstance pi=new PairInstance(ant,ana,false);
	// sink.add(pi);
	// }
	// }
	// }
	// }

	@Override
	void extractExamplesByChain(Map<Span, Integer> span2int, Chain ch,
			SpanListStruct sls, List<PairInstance> sink) {
		for (int anaChainIndex = ch.spans.size() - 1; anaChainIndex > 0; --anaChainIndex) {
			if (skipAnaphor(ch, sls, anaChainIndex)) {
				Common.bangErrorPOS("");
				continue;
			}

			Span ana = ch.spans.get(anaChainIndex);

			if (!ana.getSinglePronoun()) {
				continue;
			}

//			Span empty = new Span(true);
//			PairInstance piExtra = new PairInstance(empty, ana, false, 0, 0);
//			piExtra.rankExtra = true;
//			sink.add(piExtra);

			ana.isAnaphor = true;
			int anaphorChainID = span2int.get(ana);
			for (int anaIndex = sls.indexOf(ana), antIndex = anaIndex - 1; antIndex >= 0; --antIndex) {
				Span ant = sls.get(antIndex);
				Integer antChainID = span2int.get(ant);
				if (antChainID != null && antChainID.equals(anaphorChainID)) { // Got
																				// the
																				// match,
																				// break
																				// here
					PairInstance pi = new PairInstance(ant, ana, true,
							sls.getMentionDist(antIndex, anaIndex),
							sls.getNesBetween(antIndex, anaIndex));
					sink.add(pi);
					break;
				} else {
					PairInstance pi = new PairInstance(ant, ana, false,
							sls.getMentionDist(antIndex, anaIndex),
							sls.getNesBetween(antIndex, anaIndex));
					sink.add(pi);
				}
			}
		}
	}
	// @Override
	// void extractExamplesByChain(Map<Span, Integer> span2int, Chain ch,
	// SpanListStruct sls, List<PairInstance> sink) {
	// // Collections.sort(ch.spans);
	// for(int anaChainIndex=ch.spans.size()-1;anaChainIndex>0;--anaChainIndex){
	// if(skipAnaphor(ch, sls, anaChainIndex))
	// continue;
	// Span ana=ch.spans.get(anaChainIndex);
	// int anaphorChainID=span2int.get(ana);
	// for(int antIndex=sls.indexOf(ana)-1;antIndex>=0;--antIndex){
	// Span ant=sls.get(antIndex);
	// Integer antChainID=span2int.get(ant);
	// if(antChainID!=null && antChainID.equals(anaphorChainID)){ //Got the
	// match, break here
	// PairInstance pi=new PairInstance(ant,ana,true);
	// sink.add(pi);
	// //Then do the remaining positive
	// for(int i=ch.spans.indexOf(ant)-1;i>=0;--i){
	// ant=ch.spans.get(i);
	// if(sls.contains(ant))
	// sink.add(new PairInstance(ant,ana,true));
	// }
	// break;
	// } else {
	// PairInstance pi=new PairInstance(ant,ana,false);
	// sink.add(pi);
	// }
	// }
	// }
	// }

}
