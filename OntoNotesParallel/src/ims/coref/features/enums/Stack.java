package ims.coref.features.enums;

public enum Stack {
	//Ok, we have the following cases:
	Neither,   //Neither are in any cluster
	AntOnly,   //Antecedent was in a cluster, but anaphor not
	AnaOnly,   //Antecedent wasnt, but anaphor is
	NonCoref,  //Both are clustered, but not in same cluster
	Coref      //They were really coreferent
}
