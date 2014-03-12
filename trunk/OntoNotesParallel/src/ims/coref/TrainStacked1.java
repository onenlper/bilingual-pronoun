package ims.coref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.bwaldvogel.liblinear.FeatureNode;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;
import ims.coref.io.DocumentReader;
import ims.coref.io.ReaderWriterFactory;
import ims.coref.lang.Language;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.markables.MarkableExtractorFactory;
import ims.coref.training.ITrainingExampleExtractor;
import ims.coref.training.TrainingExampleExtractorFactory;
import ims.coref.util.ModelReaderWriter;
import ims.ml.liblinear.LibLinearInMemorySink;
import ims.ml.liblinear.LibLinearModel;
import ims.util.Pair;
import ims.util.Util;

public class TrainStacked1 {

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException{
		Options options=new Options(args);
		final Language lang=Language.initLanguage(options.lang);
		final FeatureSet fs = Train.getFeatureSet(options, lang);
		final IMarkableExtractor markableExtractor=MarkableExtractorFactory.getExtractorS(options.markableExtractors==null?Language.getLanguage().getDefaultMarkableExtractors():options.markableExtractors);
		final ITrainingExampleExtractor trainingExampleExtractor=TrainingExampleExtractorFactory.getExtractor(options.trainingExampleExtractor,markableExtractor,options.commitStrategy);
		System.out.println("Using training example extractor: "+trainingExampleExtractor.getClass().getCanonicalName());
		DocumentReader reader=ReaderWriterFactory.getReader(options.inputFormat, options.input);
		if(markableExtractor.needsTraining()){
			System.out.println("Training anaphoricy classifier");
			markableExtractor.train(reader);
		}
		System.out.println("Registering features");
		fs.registerAll(reader, trainingExampleExtractor);
		System.out.println("Done. Using features:");
		for(IFeature f:fs.getFeatures())
			System.out.println(f.toString());
		System.out.println();
		System.out.println("Size of feature space: "+Util.insertCommas(fs.getSizeOfFeatureSpace()));
		//Now train 10 models
//		GoldStandardChainExtractor gsce=new GoldStandardChainExtractor();
		LibLinearInMemorySink[] isSinks=new LibLinearInMemorySink[options.stackfolds];
		for(int i=0;i<isSinks.length;++i)
			isSinks[i]=new LibLinearInMemorySink(options.libLinearParameter,options.bias);
		for(Iterator<Pair<Document,Integer>> it=new StackTrainDocumentIterator(reader,options.stackfolds);it.hasNext();){
			Pair<Document,Integer> p=it.next();
			Document doc=p.getLeft();
			int skipFold=p.getRight();

			for(PairInstance pi:trainingExampleExtractor.getInstances(doc)){
//				System.out.println(pi);
				int label=pi.corefers?Coref.POSITIVE:Coref.NEGATIVE;
				List<FeatureNode> fns=fs.getFeatureNodes(pi, doc);
				for(int i=0;i<options.stackfolds;++i){
					if(i==skipFold)
						continue;
					isSinks[i].sink(label, fns);
				}
			}
		}
		List<TrainJob> trainJobs=new ArrayList<TrainJob>();
		for(LibLinearInMemorySink is:isSinks)
			trainJobs.add(new TrainJob(is));
//		List<TrainJob> trainJobs=new ArrayList<TrainJob>();
//		System.out.println("Generating training instances.");
//		for(int j=0;j<options.stackfolds;++j){
//			System.out.println("Fold "+j);
//			LibLinearInMemorySink is=new LibLinearInMemorySink(LibLinearInMemorySink.DEFAULT_S7_PARAMETER,options.bias);
//			int docCount=0;
//			for(Document d:reader){
//				docCount++;
//				if(docCount%options.stackfolds==j)
//					continue;
//				Set<Span> predSpans=markableExtractor.extractMarkables(d);
//				Map<Integer,Chain> goldChains=gsce.getGoldChains(d);
//				for(PairInstance pi:trainingExampleExtractor.getInstances(goldChains,predSpans)){
//					int label=pi.corefers?Coref.POSITIVE:Coref.NEGATIVE;
//					is.sink(label, fs.getFeatureNodes(pi,d));
//				}
//			}
//			trainJobs.add(new TrainJob(is));
//		}
		ExecutorService threadPool=Executors.newFixedThreadPool(Options.cores);
		LibLinearModel[] models=new LibLinearModel[options.stackfolds];
		int i=0;
		for(Future<LibLinearModel> f:threadPool.invokeAll(trainJobs))
			models[i++]=f.get();
		threadPool.shutdown();
		ModelReaderWriter.saveStacked1(options.stackModel1, markableExtractor, fs, models, lang,trainingExampleExtractor);		
		options.done();
	}
	
	public static class TrainJob implements Callable<LibLinearModel> {
		private final LibLinearInMemorySink is;
		TrainJob(LibLinearInMemorySink is){	this.is=is;	}
		@Override
		public LibLinearModel call() throws Exception {	return is.train(); }
	}
	
	
	public static class StackTrainDocumentIterator implements Iterator<Pair<Document,Integer>> {
		
		private final Iterator<Document> docIt;
		private final int folds;
		private int docCount=-1;
		private int partCount=-1;
		private String lastDoc="<null>";
		private Document next;
		
		
		public StackTrainDocumentIterator(DocumentReader reader,int folds){
			this.docIt=reader.iterator();
			this.folds=folds;
			readNext();
		}
		
		private void readNext(){
			if(!docIt.hasNext()){
//				System.out.println("doesnt have next");
				next=null;
				return;
			}
			partCount++;
			next=docIt.next();
			if(!next.docName.equals(lastDoc))
				docCount++;
			lastDoc=next.docName;
		}
		
		@Override
		public boolean hasNext() {
			return next!=null;
		}

		@Override
		public Pair<Document,Integer> next() {
			final int nextFold;
			//Then decide where it goes
			if(Options.PARTITION_BY_PART){
				nextFold=partCount%folds;
			} else {
				nextFold=docCount%folds;
			}
			Pair<Document,Integer> p=new Pair<Document,Integer>(next,nextFold);
			readNext();
			return p;
		}

		@Override
		public void remove() {
			throw new Error("!");
		}
		
	}
}
