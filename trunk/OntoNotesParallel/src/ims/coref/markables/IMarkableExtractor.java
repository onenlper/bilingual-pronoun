package ims.coref.markables;

import java.io.Serializable;
import java.util.Set;

import ims.coref.data.Document;
import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.io.DocumentReader;

public interface IMarkableExtractor extends Serializable {

	public void extractMarkables(Sentence s,Set<Span> sink,String docName);
	public Set<Span> extractMarkables(Document d);
	public boolean needsTraining();
	public void train(DocumentReader reader);
	
}
