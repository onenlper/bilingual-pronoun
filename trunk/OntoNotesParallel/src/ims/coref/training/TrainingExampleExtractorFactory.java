package ims.coref.training;

import ims.coref.markables.IMarkableExtractor;
import ims.coref.training.AbstractTrainingExampleExtractor.CommitStrategy;

public class TrainingExampleExtractorFactory {

	public static ITrainingExampleExtractor getExtractor(String name, IMarkableExtractor markableExtractor,CommitStrategy commitStrategy){
		if(name.equalsIgnoreCase("soon")){
			return new SoonTrainingExampleExtractor(markableExtractor,commitStrategy);
		} else if (name.equalsIgnoreCase("santos")){
			return new SantosCarvalhoExampleExtractor(markableExtractor,commitStrategy);
		} else if(name.equalsIgnoreCase("soonAndPositive")){
			return new SoonAndAllPositiveExtractor(markableExtractor,commitStrategy);
		} else if(name.equalsIgnoreCase("santosAndPositive")){
			return new SantosCarvalhoAndAllPostiveExtractor(markableExtractor,commitStrategy);
		} else if(name.equalsIgnoreCase("santosExtended")){
			return new SantosCarvalhoExtendedAllPositiveExtractor(markableExtractor,commitStrategy);
		} else if(name.equalsIgnoreCase("allPreceding")){
			return new AllPrecedingPairsExtractor(markableExtractor,commitStrategy);
		} else {
			throw new RuntimeException("Unknown training example extractor: "+name);
		}
	}
}
