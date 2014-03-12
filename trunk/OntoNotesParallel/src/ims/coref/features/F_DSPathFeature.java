package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.DTPath;
import ims.coref.data.DTPath.Link;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_DSPathFeature extends AbstractSingleDataDrivenFeature {
	private static final long serialVersionUID = 1L;

	private final TokenTrait tt;
	private final TokenTraitExtractor tte;
	protected F_DSPathFeature(TokenTrait tt, int cutOff) {
		super("DSPath"+tt.toString(), cutOff);
		this.tt=tt;
		this.tte=new TokenTraitExtractor(tt);
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {
		if(instance.ant.s.sentenceIndex==instance.ana.s.sentenceIndex)
			return null;
		switch(tt){
		case Form: 
		case Pos:  return getFormOrPosPath(instance);
		case Fun:  return getFunPath(instance);
		default: throw new Error("!");
		}
	}

	private String getFormOrPosPath(PairInstance instance) {
		StringBuilder sb=new StringBuilder(tte.getTrait(instance.ana.s,instance.ana.hd));
		DTPath p=instance.getDTPath();
		for(Link l:p.links){
			if(l.toIndex==-1)
				sb.append("<root>");
			else
				sb.append(tte.getTrait(l.toSentence, l.toIndex));
		}
		return sb.toString();
	}

	private String getFunPath(PairInstance instance) {
		DTPath p=instance.getDTPath();
		StringBuilder sb=new StringBuilder();
		for(Link l:p.links){
			if(l.toIndex==-1)
				sb.append("<root>");
			else
				sb.append(l.up?UP:DOWN).append(l.lbl);
		}
		return sb.toString();
	}

}
