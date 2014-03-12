package ims.coref.resolver;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.postprocessor.IChainPostProcessor;

public interface ICoreferenceResolver {

	public CorefSolution resolve(Document d);
	public IChainPostProcessor getPostProcessor();
}
