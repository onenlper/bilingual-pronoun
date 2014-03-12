package ims.coref;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.bwaldvogel.liblinear.FeatureNode;

import ims.coref.TrainStacked1.*;
import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.features.F_StackEnum;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;
import ims.coref.io.DocumentReader;
import ims.coref.io.ReaderWriterFactory;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.resolver.ICoreferenceResolver;
import ims.coref.training.ITrainingExampleExtractor;
import ims.coref.training.TrainingExampleExtractorFactory;
import ims.coref.util.ModelReaderWriter;
import ims.coref.util.ModelReaderWriter.StackModel1;
import ims.ml.liblinear.LibLinearInMemorySink;
import ims.ml.liblinear.LibLinearModel;
import ims.util.Pair;
import ims.util.Util;

public class TrainStacked2 {	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException{
		Options options=new Options(args);
		System.out.println("Loading stacked model.");
		StackModel1 sm1=ModelReaderWriter.loadStacked1(options);
		final IMarkableExtractor markableExtractor=sm1.me; //Just use the same markable extractor for now, else stuff might get messy
		final ITrainingExampleExtractor trainingExampleExtractor;
		final boolean sameTeex;
		if(options.stackTrainingExampleExtractor.toLowerCase().startsWith("stack")){
			trainingExampleExtractor=sm1.teex;
			sameTeex=true;
			System.out.println("Using same training example extractor as the first stage:");
			System.out.println(sm1.teex.getClass().getName());
		} else {
			trainingExampleExtractor=TrainingExampleExtractorFactory.getExtractor(options.trainingExampleExtractor,markableExtractor,options.commitStrategy);
			sameTeex=false;
			System.out.println("Using different training example extractors");
			System.out.println("Stage 1: "+sm1.teex.getClass().getName());
			System.out.println("Stage 2: "+trainingExampleExtractor.getClass().getName());
		}
		
//		System.out.println("Using training example extractor: "+trainingExampleExtractor.getClass().getCanonicalName());
		FeatureSet stackFS=getStackedFS(options);

		DocumentReader reader=ReaderWriterFactory.getReader(options.inputFormat, options.input);
		if(needToRegisterStacked(stackFS)){
			System.out.println("Registering features for stacked features:");
			stackFS.registerStacked(reader, trainingExampleExtractor,sm1.resolvers);
		} else {
			System.out.println("Skipping registering stacked features -- not needed");
		}
		System.out.println("Stack features");
		for(IFeature f:stackFS.getFeatures())
			System.out.println(f.toString());
		
		System.out.println("Size of stage 1 fs:    "+Util.insertCommas(sm1.fs.getSizeOfFeatureSpace()));
		System.out.println("Size of stacked fs:    "+Util.insertCommas(stackFS.getSizeOfFeatureSpace()));
		System.out.println();
		System.out.println("Generating training instances for both classifiers.");
		//Now we need to train two models -- the first stage model and the second stage model
		LibLinearInMemorySink model1sink=new LibLinearInMemorySink(options.libLinearParameter, options.bias);
		LibLinearInMemorySink model2sink=new LibLinearInMemorySink(options.libLinearParameter, options.bias);
		
		if(sameTeex)
			generateTrainingExamplesSameTrainingExampleExtractor(sm1, stackFS, reader,model1sink, model2sink);
		else
			generateTrainingExamplesDifferentTrainingExampleExtractor(sm1,stackFS, reader,trainingExampleExtractor,model1sink,model2sink);
		
		
		System.out.println("Done");
		System.out.println();
		System.out.println("Training models");
		ExecutorService threadPool=Executors.newFixedThreadPool(Options.cores);
		Future<LibLinearModel> f1=threadPool.submit(new TrainJob(model1sink));
		Future<LibLinearModel> f2=threadPool.submit(new TrainJob(model2sink));
		threadPool.shutdown();

		ModelReaderWriter.saveStacked2(options.model,sm1,stackFS,f1.get(),f2.get(),options);
		options.done();
	}

	private static void generateTrainingExamplesDifferentTrainingExampleExtractor(StackModel1 sm1, FeatureSet stackFS, DocumentReader reader,ITrainingExampleExtractor teex,LibLinearInMemorySink model1sink, LibLinearInMemorySink model2sink) {
//		GoldStandardChainExtractor gsce=new GoldStandardChainExtractor();
		Iterator<Document> it=new StackTrain2ExtraThreadIterator(reader,sm1);
		while(it.hasNext()){
			Document d=it.next();
//		Iterator<Pair<Document,Integer>> it=new StackTrainDocumentIterator(reader,sm1.resolvers.length);
//		while(it.hasNext()){
//			Pair<Document,Integer> p=it.next();
//			Document d=p.getLeft();
//			int holdOut=p.getRight();
//			CorefSolution cs=sm1.resolvers[holdOut].resolve(d);
//			d.stackMap=cs.span2int;
//			
//			Map<Integer,Chain> goldChains=gsce.getGoldChains(d);
//			Set<Span> predSpans=sm1.me.extractMarkables(d);
			//Do it for first:
			{
				for(PairInstance pi:sm1.teex.getInstances(d)){
					List<FeatureNode> fns=sm1.fs.getFeatureNodes(pi, d);
					int label=pi.corefers?Coref.POSITIVE:Coref.NEGATIVE;
					model1sink.sink(label, fns);
				}
			}
			//Do it for stacked:
			{
				for(PairInstance pi:teex.getInstances(d)){
					int label=pi.corefers?Coref.POSITIVE:Coref.NEGATIVE;
					List<FeatureNode> fns=sm1.fs.getFeatureNodes(pi, d);
					stackFS.appendFeatureNodes(pi,d,sm1.fs.getSizeOfFeatureSpace(),fns);
					model2sink.sink(label, fns);
				}
			}
		}		
	}

	private static void generateTrainingExamplesSameTrainingExampleExtractor(StackModel1 sm1,FeatureSet stackFS, DocumentReader reader,LibLinearInMemorySink model1sink,LibLinearInMemorySink model2sink) {
//		GoldStandardChainExtractor gsce=new GoldStandardChainExtractor();
//		StackTrainDocumentIterator it=new StackTrainDocumentIterator(reader,sm1.resolvers.length);

		//XXX SWAP
		System.out.println(new Date()+": Starting generating training examples");
		Iterator<Document> it2=new StackTrain2ExtraThreadIterator(reader,sm1);
		while(it2.hasNext()){
			Document d=it2.next();
		//XXX SWAP
//		while(it.hasNext()){
//			Pair<Document,Integer> p=it.next();
////			System.out.println("iterator loop: "+p.getLeft().docString+"\t"+p.getRight());
//			Document d=p.getLeft();
//			int holdOut=p.getRight();
//			CorefSolution cs=sm1.resolvers[holdOut].resolve(d);
//			d.stackMap=cs.span2int;

		//XXX END SWAP
//			Set<Span> predSpans=sm1.me.extractMarkables(d);
//			Map<Integer,Chain> goldChains=gsce.getGoldChains(d);
			List<PairInstance> pis=sm1.teex.getInstances(d);
			for(PairInstance pi:pis){
				int label=pi.corefers?Coref.POSITIVE:Coref.NEGATIVE;
				List<FeatureNode> fns=sm1.fs.getFeatureNodes(pi, d);
				model1sink.sink(label, fns);
				stackFS.appendFeatureNodes(pi,d,sm1.fs.getSizeOfFeatureSpace(),fns);
				model2sink.sink(label, fns);
			}
		}
		System.out.println(new Date()+": DONE generating training examples");
	}

	private static boolean needToRegisterStacked(FeatureSet stackFS) {
		return !(stackFS.getFeatures().size()==1 && stackFS.getFeatures().get(0) instanceof F_StackEnum);
	}

	private static FeatureSet getStackedFS(Options options) throws IOException {
		if(options.featureSetFile==null)
			return FeatureSet.getFromNameArray("StackEnum");
		else
			return FeatureSet.getFromFile(options.featureSetFile);
	}
	
	
	static class StackTrain2ExtraThreadIterator implements Iterator<Document> {

		private final StackTrainDocumentIterator it1;
		private final ExecutorService thread=Executors.newFixedThreadPool(1);
		private final StackModel1 sm1;
		
		private Future<Document> futureDoc;
		
		public StackTrain2ExtraThreadIterator(DocumentReader reader,StackModel1 sm1) {
			System.out.println("Creating threaded iterator");
			this.it1=new StackTrainDocumentIterator(reader,sm1.resolvers.length);
			this.sm1=sm1;
			futureDoc=getFutureDoc();
		}

		private Future<Document> getFutureDoc() {
			if(!it1.hasNext()){
				thread.shutdown();
				return null;
			}
			Pair<Document,Integer> p=it1.next();
			ResolveJob rj=new ResolveJob(p.getLeft(),sm1.resolvers[p.getRight()]);
			Future<Document> f=thread.submit(rj);
			return f;
		}

		@Override
		public boolean hasNext() {
			return futureDoc!=null;
		}

		@Override
		public synchronized Document next() {
			try {
//				System.out.println((new Date())+":  FETCHING FUTURE DOC");
				Document n=futureDoc.get();
//				System.out.println((new Date())+":  GOT FUTURE DOC");
				futureDoc=getFutureDoc();
				return n;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("!");
			}
		}

		@Override
		public void remove() {
			throw new Error("Not implemented");
		}
		
	}
	
	static class ResolveJob implements Callable<Document> {

		private final ICoreferenceResolver resolver;
		private final Document doc;
		
		public ResolveJob(Document d,ICoreferenceResolver res){
			this.resolver=res;
			this.doc=d;
		}
		
		@Override
		public Document call() throws Exception {
			CorefSolution cs=resolver.resolve(doc);
////		System.out.println("iterator loop: "+p.getLeft().docString+"\t"+p.getRight());
			cs.assignStackMap(doc);
			return doc;

		}
		
	}
}
