package ims.coref;

import java.io.IOException;
import java.util.Iterator;

import align.DocumentMap;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.io.DocumentReader;
import ims.coref.io.DocumentWriter;
import ims.coref.io.ReaderWriterFactory;
import ims.coref.resolver.ICoreferenceResolver;
import ims.coref.util.ModelReaderWriter;

public class Coref {

	public static final int POSITIVE=1;
	public static final int NEGATIVE=0;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		Options options=new Options(args);

		final ICoreferenceResolver resolver;
		if(options.stacked)
			resolver=ModelReaderWriter.loadStackedSolver(options);
		else
			resolver=ModelReaderWriter.loadResolver(options);
		System.out.println("Using resolver: "+resolver.toString());
		System.out.println("Using postprocessor: "+resolver.getPostProcessor().getClass().getCanonicalName());
		int docCount=0;
		int senCount=0;
		int chainCount=0;
		int mentionCount=0;
		DocumentReader reader=ReaderWriterFactory.getReader(options.inputFormat, options.input);
		DocumentWriter writer=ReaderWriterFactory.getWriter(options.outputFormat, options.output);
		Iterator<Document> docIt;
		docIt=reader.iterator();
		
//		for(Document d:reader){
		while(docIt.hasNext()){
			Document d=docIt.next();
			if(!options.useGoldMarkableExtractor)
				d.clearCorefCols();
			docCount++;
			senCount+=d.sen.size();
			CorefSolution cs=resolver.resolve(d);
			chainCount+=cs.getChainCount();
			mentionCount+=cs.getMentionCount();
			if(options.useGoldMarkableExtractor)
				d.clearCorefCols();
			d.setCorefCols(cs.getKey());
			writer.write(d);
		}
		writer.close();
		options.done();
		System.out.println("Documents: "+docCount);
		System.out.println("Sentences: "+senCount);
		System.out.println();
		System.out.println("Chains:   "+chainCount);
		System.out.println("Mentions: "+mentionCount);
		System.out.println();
		System.out.println(resolver.getPostProcessor().toString());
		System.out.println();
	}
}
