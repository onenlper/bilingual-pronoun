package ims.coref;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import util.Common;

import align.DocumentMap;

import de.bwaldvogel.liblinear.FeatureNode;

import ims.coref.Options.Training;
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
import ims.ml.liblinear.InstanceSink;
import ims.ml.liblinear.LibLinearInMemorySink;
import ims.ml.liblinear.LibLinearModel;
import ims.ml.liblinear.LibLinearOnDiskSink;
import ims.util.Util;

public class Train {

	static String folder = "";
	
	public static void main(String[] args) throws IOException{
		Options options=new Options(args);
		final Language lang=Language.initLanguage(options.lang);
		// load giza output
		final FeatureSet fs = getFeatureSet(options, lang);
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
		final InstanceSink is;
		if(options.training==Training.InMemory){
			is=new LibLinearInMemorySink(options.libLinearParameter,options.bias);
		} else if (options.training==Training.OnDisk){
			is=new LibLinearOnDiskSink(Util.getWriter(options.onDiskTrainingFile));
		} else {
			throw new Error("!");
		}
		System.out.println("Creating training instances");
//		EvaluateMarkables evalMark=new EvaluateMarkables();
//		GoldStandardChainExtractor gsce=new GoldStandardChainExtractor();
		int docCount=0;
		int senCount=0;
		int trainingInstanceCount=0;
		int positiveCount=0;
		for(Document d:reader){
			docCount++;
			senCount+=d.sen.size();
//			Set<Span> predSpans=markableExtractor.extractMarkables(d);
//			Map<Integer,Chain> goldChains=gsce.getGoldChains(d);
//			List<PairInstance> pis=trainingExampleExtractor.getInstances(goldChains,predSpans);
			List<PairInstance> pis=trainingExampleExtractor.getInstances(d);
			trainingInstanceCount+=pis.size();
			for(PairInstance pi:pis){
				final int label;
				if(pi.corefers){
					positiveCount++;
					label=Coref.POSITIVE;
				} else {
					label=Coref.NEGATIVE;
				}
				fs.thisLang = true;
				List<FeatureNode> fns=fs.getFeatureNodes(pi,d);
				fs.thisLang = false;
				is.sink(label, fns);
			}
		}
		is.close();
		
		System.out.println("Documents: "+docCount);
		System.out.println("Sentences: "+senCount);
		System.out.println("Training Instances (% positive): "+Util.insertCommas(trainingInstanceCount)+" ("+String.format("%.3f", 100.0*positiveCount/trainingInstanceCount)+"%)");
		
		System.out.println("Training model.");
		LibLinearModel llModel=is.train();
		System.out.println("Saving model to file.");
		ModelReaderWriter.save(options.model,markableExtractor,fs,llModel,lang);
		System.out.println("Done.");
		System.out.println(new Date());
		System.out.println();
		System.out.println("Time: "+Util.insertCommas(System.currentTimeMillis()-options.startms));
		System.out.println();
//		System.out.println("Markable extraction: ");
//		System.out.println(evalMark.toString());
	}

	public static FeatureSet getFeatureSet(Options options, final Language lang)throws IOException {
		if(options.featureSetFile!=null)
			return FeatureSet.getFromFile(options.featureSetFile);
		else
			return lang.getDefaultFeatureSet();
	}
}
