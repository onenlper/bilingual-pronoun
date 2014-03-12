package ims.coref.markables;

import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.io.DocumentReader;

import java.util.HashSet;
import java.util.Set;

public class ChainedExtractor extends AbstractMarkableExtractor {
	private static final long serialVersionUID = 1L;

	final IMarkableExtractor[] extractors;
	
	public ChainedExtractor(IMarkableExtractor[] extractors){
		this.extractors=extractors;
	}
	
	@Override
	public void extractMarkables(Sentence s, Set<Span> sink,String docName) {
		for(IMarkableExtractor me:extractors)
			me.extractMarkables(s, sink,docName);
	}

	@Override
	public boolean needsTraining() {
		for(IMarkableExtractor a:extractors)
			if(a.needsTraining())
				return true;
		return false;
	}
	
	@Override
	public void train(DocumentReader reader) {
		for(IMarkableExtractor a:extractors)
			if(a.needsTraining())
				a.train(reader);		
	}

	public String toString(){
		StringBuilder sb=new StringBuilder("ChainedExtractor:");
		for(IMarkableExtractor me:extractors)
			sb.append(" ").append(me.toString());
		return sb.toString();
	}
}
