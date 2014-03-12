package ims.coref.resolver;

import ims.coref.Coref;
import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.features.FeatureSet;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.postprocessor.IChainPostProcessor;
import ims.ml.liblinear.LibLinearModel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.bwaldvogel.liblinear.FeatureNode;

public class AvgMaxProbResolver extends AbstractResolver {

	private final double newTH;
	private final double mergeTH;
	private final boolean doReorder;
	
	private final double relaxMergeThStepSize;
	
	public AvgMaxProbResolver(IMarkableExtractor me, LibLinearModel llModel,	FeatureSet fs, IChainPostProcessor pp, int decodeWindow, double newTH, double mergeTH,boolean doReorder,double relaxMergeThStepSize) {
		super(me, llModel, fs, pp, decodeWindow,SingleLinkConstraint.None);
		this.newTH=newTH;
		this.mergeTH=mergeTH;
		this.doReorder=doReorder;
		this.relaxMergeThStepSize=relaxMergeThStepSize;
	}

	@Override
	CorefSolution doResolve(SpanListStruct sls,Document d) {
		CorefSolution cs=new CorefSolution();
		if(doReorder)
			sls=new ReorderedSpanListStruct(sls);
		for(int anaIndex=1;anaIndex<sls.size();++anaIndex){
			resolveAvgMaxProb(sls,cs,anaIndex,d);
		}
		return cs;
	}
	
	
	private static final Comparator<Span> REORDERING_COMPARATOR=new Comparator<Span>(){
		@Override
		public int compare(Span arg0, Span arg1) {
			int s0=1;
			int s1=1;
			if(arg0.isProperName)
				s0=0;
			else if(arg0.isPronoun)
				s0=2;

			if(arg1.isProperName)
				s1=0;
			else if(arg0.isPronoun)
				s1=2;
			
			return s0-s1;
		}
	};
	
	static class ReorderedSpanListStruct extends SpanListStruct {

		private final Span[] spansReordered;
		private final int[] reorderIndex;
		
		protected ReorderedSpanListStruct(SpanListStruct sls) {
			super(sls);
			spansReordered=Arrays.copyOf(sls.spansLinearOrder, sls.spansLinearOrder.length);
			Arrays.sort(spansReordered,REORDERING_COMPARATOR);
			reorderIndex=getReorderedIndexMapping(sls);
		}

		private int[] getReorderedIndexMapping(SpanListStruct sls) {
			//here we need to find out which went where. From the top of my head, I can't think of a better way than brute force
			int[] reorderIndex=new int[sls.spansLinearOrder.length];
			for(int i=0;i<sls.spansLinearOrder.length;++i)
				reorderIndex[i]=sls.indexOf(spansReordered[i]);			
			return reorderIndex;
		}
		
		public int indexOf(Span s){
			return indexOf(spansReordered,s);
		}
		
		public Span get(int antIndex) {
			return spansReordered[antIndex];
		}

		public boolean contains(Span ant) {
			return indexOf(ant)>=0;
		}
	
		public int getMentionDist(int antIndex,int anaIndex){
			return Math.abs(reorderIndex[antIndex]-reorderIndex[anaIndex]);
		}
		public int getNesBetween(int antIndex,int anaIndex){
			return Math.abs(neCount[reorderIndex[antIndex]]-neCount[reorderIndex[anaIndex]]);
		}
	}

	public void resolveAvgMaxProb(SpanListStruct sls,CorefSolution cs,int anaIndex,Document d){
		Span anaphor=sls.get(anaIndex);
		Span bestSpan=null;
		Set<Integer> handledChains=new HashSet<Integer>();
		double bestScore=-1;
		for(int antIndex=anaIndex-1;antIndex>=0 ;--antIndex){ //we don't do dist here, since we assume reordering anyway
			Span ant=sls.get(antIndex);
//			Integer antChainID=cs.span2int.get(ant);
			Integer antChainID=cs.getSpanChainID(ant);
			if(antChainID==null){ //Create new chain ?
				PairInstance pi=new PairInstance(ant,anaphor,sls.getMentionDist(antIndex, anaIndex),sls.getNesBetween(antIndex, anaIndex));
				List<FeatureNode> fns=fs.getFeatureNodes(pi,d);
				double p=llModel.getProbabilityForClass(fns, Coref.POSITIVE);
				if(p>bestScore && p>=newTH){
					bestScore=p;
					bestSpan=ant;
				}
			} else { //Merge ?
				if(handledChains.contains(antChainID))
					continue;
				//Here take the geometric mean of the probabilities to all the previous spans in the cluster
				double product=1;
				double terms=0;
				for(Span a:cs.getSpanList(antChainID)){
					int ai=sls.indexOf(a);
					PairInstance pi=new PairInstance(a,anaphor,sls.getMentionDist(ai, anaIndex),sls.getNesBetween(ai, anaIndex));
					List<FeatureNode> fns=fs.getFeatureNodes(pi,d);
					double p=llModel.getProbabilityForClass(fns, Coref.POSITIVE);
					product*=p;
					terms++;
				}
				double th=relaxMergeThStepSize>0?getRelaxedMergeTh(terms):mergeTH;
				double p=Math.pow(product, 1.0/terms);
				if(p>bestScore && p>=th){
					bestScore=p;
					bestSpan=ant;
				}
				handledChains.add(antChainID);
			}
		}
		if(bestSpan!=null){
			cs.addLink(bestSpan,anaphor);
		}
	}

	private double getRelaxedMergeTh(double terms) {
		return mergeTH-terms*relaxMergeThStepSize;
	}

	public String toString(){
		return this.getClass().getCanonicalName()+"  (newTH: "+newTH+", mergeTH: "+mergeTH+", relaxMerge: "+relaxMergeThStepSize+")";
	}
}
