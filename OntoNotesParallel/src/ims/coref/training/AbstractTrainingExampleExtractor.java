package ims.coref.training;

import ims.coref.Parallel;
import ims.coref.SortedDoc;
import ims.coref.data.Chain;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.markables.IMarkableExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTrainingExampleExtractor implements
		ITrainingExampleExtractor {
	private static final long serialVersionUID = 1L;

	/*
	 * When generating pairwise examples, we have the problem that sometimes the
	 * closest preceding antecedent of the anaphor is not in the predspans list
	 * What to do then? 1) Always commit the instances; 2) Commit only when
	 * there is a positive somewhere; 3) Commit only when the closest preceding
	 * antecedent is in the list
	 */
	public static enum CommitStrategy {
		Always, OnlyWhenPositiveExists, OnlyWhenImmediatePrecedingExists
	}

	protected final IMarkableExtractor markableExtractor;
	protected final CommitStrategy commitStrategy;

	protected AbstractTrainingExampleExtractor(
			IMarkableExtractor markableExtractor, CommitStrategy commitStrategy) {
		this.markableExtractor = markableExtractor;
		this.commitStrategy = commitStrategy;
	}

	// protected Map<Integer,Chain> getGoldChainMap(Document d){
	// GoldStandardChainExtractor goldExtractor=new
	// GoldStandardChainExtractor();
	// Map<Integer,Chain> chainMap=goldExtractor.getGoldChains(d);
	// return chainMap;
	// }

	protected Chain[] getGoldChains(Document d) {
		GoldStandardChainExtractor goldExtractor = new GoldStandardChainExtractor();
		Chain[] gold = goldExtractor.getGoldChains(d);
		return gold;
	}

	// private Map<Span,Integer> getSpan2IntMap(Iterable<Chain> cit){
	private Map<Span, Integer> getSpan2IntMap(Chain[] cit) {
		Map<Span, Integer> span2int = new HashMap<Span, Integer>();
		for (Chain c : cit) {
			if (c != null)
				for (Span s : c.spans)
					span2int.put(s, c.chainId);
		}
		return span2int;
	}

	// private Map<Span,Integer> getSpan2IntMap(Map<Integer,Chain> chainMap){
	// Map<Span,Integer> span2int=new HashMap<Span,Integer>();
	// for(Entry<Integer,Chain> e:chainMap.entrySet()){
	// for(Span s:e.getValue().spans)
	// span2int.put(s, e.getKey());
	// }
	// return span2int;
	// }

	public static void pruneNested(Chain[] goldChains) {
		for (int i = 0; i < goldChains.length; ++i) {
			Chain c = goldChains[i];
			List<Span> removes = new ArrayList<Span>();
			for (Span s1 : c.spans) {
				for (Span s2 : c.spans) {
					if (s1.hd == s2.hd
							&& (s1.end - s1.start > s2.end - s2.start)) {
						removes.add(s2);
					}
				}
			}
			c.spans.removeAll(removes);
		}
	}

	public static void pruneVerbsFromChains(Chain[] goldChains) {
		for (int i = 0; i < goldChains.length; ++i) {
			Chain c = goldChains[i];
			Iterator<Span> sIt = c.spans.iterator();
			while (sIt.hasNext()) {
				Span s = sIt.next();
				if (s.s.tags[s.hd].startsWith("V"))
					sIt.remove();
			}
			if (c.spans.size() < 2)
				goldChains[i] = null;
		}
	}

	// @Deprecated
	// private static Map<Integer,Chain> pruneVerbsFromChains(Map<Integer,Chain>
	// chainMap){
	// Iterator<Entry<Integer,Chain>> it=chainMap.entrySet().iterator();
	// while(it.hasNext()){
	// Entry<Integer,Chain> e=it.next();
	// Iterator<Span> sIt=e.getValue().spans.iterator();
	// while(sIt.hasNext()){
	// Span s=sIt.next();
	// if(s.s.tags[s.hd].startsWith("V"))
	// sIt.remove();
	// }
	// if(e.getValue().spans.size()<2)
	// it.remove();
	// }
	// return chainMap;
	// }

	// @Override
	// public List<PairInstance> getInstances(Document d) {
	// Set<Span> predSpans=markableExtractor.extractMarkables(d);
	// Map<Integer,Chain> goldMap=getGoldChainMap(d);
	// return getInstances(goldMap,predSpans);
	// }

	// private List<PairInstance> getInstances(Chain[] goldChains,Set<Span>
	// predSpans){

	static int docNumber = 0;

	public List<PairInstance> getInstances(Document d) {
		Chain[] goldChains = getGoldChains(d);
		Set<Span> predSpans = markableExtractor.extractMarkables(d);
		SpanListStruct sls = SpanListStruct.fromCollection(predSpans);
		pruneNested(goldChains);
		pruneVerbsFromChains(goldChains);
		Map<Span, Integer> goldSpan2Int = getSpan2IntMap(goldChains);
		List<PairInstance> r = new ArrayList<PairInstance>();

		// TODO check align here
		Set<Span> allSpans = new HashSet<Span>();
		allSpans.addAll(predSpans);
		// allSpans.removeAll(goldSpan2Int.keySet());
		allSpans.addAll(goldSpan2Int.keySet());
		if (Parallel.turn &&  Parallel.ensemble) {
			double overall = 0;
			double match = 0;
			if (d.lang.equals("eng")) {
				Parallel.engSpanMount += allSpans.size();
			} else {
				Parallel.chiSpanMount += allSpans.size();
			}

			int k = 0;
			if (d.documentMap != null) {
				System.out.println(d.documentMap.id);
				System.out.println(d.docName + " # " + d.lang + " # "
						+ d.documentMap.id);
			}
			for (Span s : allSpans) {
				Span xs = s.getXSpan();
				// System.out.println((k++) + "\t" + s.s.ids[s.start] + ","
				// + s.s.ids[s.end] + " " + s.xSpanType + ":\t"
				// + s.getText() + " # "
				// + (xs == null ? "" : xs.getText()) + " # "
				// + (xs == null ? "" : xs.number) + " "
				// + (xs == null ? "" : xs.gender));
				overall++;
				if (d.lang.equals("eng")) {
					Parallel.engOverall++;
				} else {
					Parallel.chiOverall++;
				}
				if (xs != null) {
					match++;
					if (d.lang.equals("eng")) {
						Parallel.engMatch++;
					} else {
						Parallel.chiMatch++;
					}
				}
			}
			System.out
					.format("%s\t%s\t%d\nOverall: %f,\t Match: %f,\t Percent:%f\n=========\n",
							d.docName, d.lang, docNumber++, overall, match,
							match / overall);

			SortedDoc sd = new SortedDoc(d.docName, d.lang, overall, match);
			if (d.lang.equals("eng")) {
				Parallel.engSortedDocs.add(sd);
			} else {
				Parallel.chiSortedDocs.add(sd);
			}
		}
		for (Chain c : goldChains) {
			if (c != null) {
				extractExamplesByChain(goldSpan2Int, c, sls, r);
			}
		}
		// extractExtraPronounExamples(goldSpan2Int, sls, r);

		return r;
	}

	// TODO
	public static int rankID = 0;
	public static int maxSentDiff = 4;

	private void extractExtraPronounExamples(Map<Span, Integer> span2int,
			SpanListStruct sls, List<PairInstance> sink) {
		for (int i = 0; i < sls.size(); i++) {
			Span ana = sls.get(i);
			if (!span2int.containsKey(ana) && ana.getSinglePronoun()) {
				ana.isAnaphor = false;

				for (int j = i - 1; j >= 0; j--) {
					Span ant = sls.get(j);

					if (ana.s.sentenceIndex - ant.s.sentenceIndex >= maxSentDiff) {
						break;
					}

					Integer antIndex = sls.indexOf(ant);
					Integer anaIndex = sls.indexOf(ana);

					PairInstance pi = new PairInstance(ant, ana, false,
							sls.getMentionDist(antIndex, anaIndex),
							sls.getNesBetween(antIndex, anaIndex));
					pi.rankExtra = true;
					sink.add(pi);
				}

				Span empty = new Span(true);
				PairInstance pi = new PairInstance(empty, ana, true, 0, 0);
				pi.rankExtra = true;
				sink.add(pi);
			}
		}
		// add null antecedent for each pronoun

	}

	// @Deprecated
	// private List<PairInstance> getInstances(Map<Integer, Chain> goldChains,
	// Set<Span> predSpans){
	// SpanListStruct sls=SpanListStruct.fromCollection(predSpans);
	// pruneVerbsFromChains(goldChains);
	// Map<Span,Integer> goldSpan2Int=getSpan2IntMap(goldChains.values());
	// List<PairInstance> r=new ArrayList<PairInstance>();
	// for(Entry<Integer,Chain> e:goldChains.entrySet()){
	// extractExamplesByChain(goldSpan2Int,e.getValue(),sls,r);
	// }
	// return r;
	// }

	protected boolean skipAnaphor(Chain ch, SpanListStruct sls,
			int anaChainIndex) {
		if (commitStrategy == CommitStrategy.OnlyWhenImmediatePrecedingExists) {
			Span closestPreceedingGoldAnt = ch.spans.get(anaChainIndex - 1);
			if (!sls.contains(closestPreceedingGoldAnt))
				return true;
			else
				return false;
		} else if (commitStrategy == CommitStrategy.OnlyWhenPositiveExists) {
			boolean con = true;
			for (int r = anaChainIndex - 1; r >= 0; --r) {
				Span s = ch.spans.get(r);
				if (sls.contains(s)) {
					con = false;
					break;
				}
			}
			return con;
		} else {
			return false;
		}
	}

	protected static boolean sameChain(Span ant, Span ana,
			Map<Span, Integer> span2int) {
		Integer antI = span2int.get(ant);
		Integer anaI = span2int.get(ana);
		return antI != null && anaI != null && antI.equals(anaI);
	}

	// abstract void extractExamplesByChain(Map<Span,Integer> goldSpan2Int,Chain
	// ch,List<Span> predSpans,List<PairInstance> sink);
	abstract void extractExamplesByChain(Map<Span, Integer> goldSpan2Int,
			Chain ch, SpanListStruct sls, List<PairInstance> sink);
}
