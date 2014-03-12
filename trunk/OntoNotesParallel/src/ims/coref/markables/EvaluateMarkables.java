package ims.coref.markables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ims.coref.Options;
import ims.coref.data.Chain;
import ims.coref.data.Document;
import ims.coref.data.Span;
import ims.coref.io.DocumentReader;
import ims.coref.io.DocumentWriter;
import ims.coref.io.ReaderWriterFactory;
import ims.coref.lang.Language;
import ims.coref.training.GoldStandardChainExtractor;

public class EvaluateMarkables {

	public static final boolean REMOVE_VERBS=true;
	
	int tp=0;
	int fp=0;
	int fn=0;
	
	public void counts(Collection<Span> key,Collection<Span> pred){
		for(Span g:key){
			if(pred.contains(g))
				tp++;
			else
				fn++;
		}
		for(Span p:pred){
			if(!key.contains(p))
				fp++;
		}
	}
	
	public String toString(){
		StringBuilder sb=new StringBuilder("Mention extractor stats:\n");
		double p=100.0*tp/(tp+fp);
		double r=100.0*tp/(tp+fn);
		double f=2*p*r/(p+r);
		sb.append(String.format("Precision:  100 * %d / (%d + %d) \t = %.3f\n", tp,tp,fp,p));
		sb.append(String.format("Recall:     100 * %d / (%d + %d) \t = %.3f\n", tp,tp,fn,r));
		sb.append(String.format("F1:         %.3f\n",f));
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException{
		Options options=new Options(args);
		Language.initLanguage(options.lang);
		EvaluateMarkables eval=new EvaluateMarkables();
		IMarkableExtractor me=MarkableExtractorFactory.getExtractorS(options.markableExtractors==null?Language.getLanguage().getDefaultMarkableExtractors():options.markableExtractors);
		GoldStandardChainExtractor gold=new GoldStandardChainExtractor();
		DocumentReader reader=ReaderWriterFactory.getReader(options.inputFormat, options.input);
		System.out.println("Using markable extractors: "+me.toString());
		if(me.needsTraining()){
			System.out.println("Training markable extractor");
			me.train(reader);
		}
		DocumentWriter writer=ReaderWriterFactory.getWriter(options.outputFormat, options.output);
//		reader=ReaderWriterFactory.getReader(options.inputFormat, new File("/home/users0/anders/corpora/ontonotes5/conll-2012/v3/data/eng_dev_v3_auto_conll"));
		if(options.t2!=null)
			reader=ReaderWriterFactory.getReader(options.inputFormat, options.t2);
		
		for(Document d:reader){
			Set<Span> p=me.extractMarkables(d);
			Chain[] m=gold.getGoldChains(d);
			if(REMOVE_VERBS)
				pruneVerbsFromChains(m);
			Set<Span> g=chains2set(m);
			eval.counts(g, p);
			d.clearCorefCols();
			List<Chain> l=buildSingleTonChain(p);
			d.setCorefCols(l);
			writer.write(d);
		}
		writer.close();
		System.out.println(eval.toString());
		System.out.println();
		System.out.println("Drop count: "+NonReferentialPruner.dropCount);
	}

	public static Chain buildOneChain(Set<Span> p,Integer id) {
		Chain c=new Chain(id);
		for(Span s:p)
			c.addSpan(s);
		return c;
	}
	
	public static List<Chain> buildSingleTonChain(Set<Span> p){
		List<Chain> chains=new ArrayList<Chain>();
		int i=1;
		for(Span s:p)
			chains.add(new Chain(i++,s));
		return chains;
	}

	public static Set<Span> chains2set(Chain[] chains){
		Set<Span> set=new HashSet<Span>();
		for(Chain c:chains)
			set.addAll(c.spans);
		return set;		
	}

	private static void pruneVerbsFromChains(Chain[] goldChains) {
		for(int i=0;i<goldChains.length;++i){
			Chain c=goldChains[i];
			Iterator<Span> sIt=c.spans.iterator();
			while(sIt.hasNext()){
				Span s=sIt.next();
				if(s.s.tags[s.hd].startsWith("V"))
					sIt.remove();
			}
		}
	}
}
