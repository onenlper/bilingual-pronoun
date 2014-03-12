package ims.coref.features.extractors;

import ims.coref.data.PairInstance;
import ims.coref.data.Span;

import java.io.Serializable;

public class TargetSpanExtractor implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final TargetSpan ts;
	
	public TargetSpanExtractor(TargetSpan ts){
		this.ts=ts;
	}
	
	public Span getSpan(PairInstance pi){
		switch(ts){
		case Anaphor: return pi.ana;
		case Antecedent: return pi.ant;
		default: throw new Error("!");
		}
	}
	
	public Span getOtherSpan(PairInstance pi){
		switch(ts){
		case Anaphor: return pi.ant;
		case Antecedent: return pi.ana;
		default: throw new Error("!");
		}
	}

}
