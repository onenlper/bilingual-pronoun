package ims.coref.markables;

import java.io.Serializable;

import align.DocumentMap.Unit;

public interface ITokenNonReferentialClassifier extends Serializable {

	public void extractTrainingInstances(String[] forms,String[] pos,String[] corefCol, Unit[] units, String genre);
	public void train();
	public boolean[] classifiy(String[] forms,String[] pos, Unit[] units, String genre);
	
}
