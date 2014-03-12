package ims.coref.training;

import java.io.Serializable;
import java.util.List;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;

public interface ITrainingExampleExtractor extends Serializable {

	public List<PairInstance> getInstances(Document d);

}
