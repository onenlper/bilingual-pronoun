package ims.coref.features.extractors;

import ims.coref.data.Sentence;

import java.io.Serializable;

public class TokenTraitExtractor implements Serializable{
	private static final long serialVersionUID = 1L;

	public final TokenTrait tt;
	
	public TokenTraitExtractor(TokenTrait tt){
		this.tt=tt;
	}
	
	public String getTrait(Sentence s,int index){
		if(index<0 || index>=s.forms.length)
			return "<no-token>";
		switch(tt){
		case Form:  return s.forms[index];
		case Pos:   return s.tags[index];
		case Fun:   return s.dt.lbls[index];
		case Lemma: return s.lemmas[index];
		case BWUV: return s.bwuv[index];
		case FFChar:return s.forms[index].length()<2?null:s.forms[index].substring(0, 1);
		case FLChar:return s.forms[index].length()<2?null:s.forms[index].substring(s.forms[index].length()-1);
		case FF2Char:return s.forms[index].length()<3?null:s.forms[index].substring(0,2);
		case FL2Char:return s.forms[index].length()<3?null:s.forms[index].substring(s.forms[index].length()-2);
		default: throw new Error("!");
		}
	}
}
