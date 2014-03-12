package ims.coref.markables;

import ims.coref.data.Document;
import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.io.DocumentReader;
import ims.coref.training.GoldStandardChainExtractor;

import java.util.HashSet;
import java.util.Set;

public class GoldStandardMarkableExtractor implements IMarkableExtractor {
	private static final long serialVersionUID = 1063825656523315580L;

//	private GoldStandardChainExtractor gsce=new GoldStandardChainExtractor();
	
	@Override
	public void extractMarkables(Sentence s, Set<Span> sink, String docName) {
		GoldStandardChainExtractor.extractGoldSpans(s, sink);
	}

	@Override
	public Set<Span> extractMarkables(Document d) {
		Set<Span> spans=new HashSet<Span>();
		for(Sentence s:d.sen)
			extractMarkables(s,spans,d.docName);
		return spans;
//		for(Chain c:gsce.getGoldChains(d))
//			for(Span s:c.spans)
//				if(!(s.start==s.end && s.s.tags[s.hd].startsWith("V")))
//					spans.add(s);
//		return spans;
	}

	@Override
	public boolean needsTraining() {
		return false;
	}

	@Override
	public void train(DocumentReader reader) {
	}

}
