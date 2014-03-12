package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.DTPath;
import ims.coref.data.DTPath.Link;
import ims.coref.data.Sentence;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;

public class F_SSPathFeature extends AbstractSingleDataDrivenFeature{
	private static final long serialVersionUID = 1L;
	
	final TokenTrait tt;
	final TokenTraitExtractor tte;

	protected F_SSPathFeature(TokenTrait tt, int cutOff) {
		super("SSPath"+(tt==null?"Bergsma":tt.toString()), cutOff);
		this.tt=tt;
		this.tte=(tt==null?null:new TokenTraitExtractor(tt));
	}

	@Override
	public String extractStringValue(PairInstance instance,Document d) {
		if(instance.ana.s.sentenceIndex!=instance.ant.s.sentenceIndex)
			return null;
		if(tt==null)
			return getBergsmaPath(instance);
		switch(tt){
		case Form:
		case Pos: return getTTPath(instance);
		case Fun: return getFunPath(instance);
		default: throw new Error("!");
		}
	}

	private String getFunPath(PairInstance instance) {
		DTPath p=instance.getDTPath();
		StringBuilder sb=new StringBuilder();
		for(Link l:p.links)
			sb.append(l.lbl).append(l.up?UP:DOWN);
		return sb.toString();
	}

	private String getTTPath(PairInstance instance) {
		DTPath p=instance.getDTPath();
		StringBuilder sb=new StringBuilder(tte.getTrait(instance.ana.s, instance.ana.hd));
		for(Link l:p.links)
			sb.append(l.up?UP:DOWN).append(tte.getTrait(l.toSentence, l.toIndex));
		return sb.toString();
	}

	private String getBergsmaPath(PairInstance instance) {
		DTPath p=instance.getDTPath();
		Sentence s=instance.ana.s;
		if(p.lcaIndex==-1 || !instance.ana.isPronoun || !s.tags[p.lcaIndex].startsWith("V") || !s.tags[instance.ant.hd].startsWith("N"))
			return "<n>";
		String begin;
		if(s.forms[instance.ana.hd].endsWith("self"))
			begin="<reflexive>";
		else
			begin=s.tags[instance.ana.hd];
		StringBuilder sb=new StringBuilder(begin);
		for(int i=0;i<p.links.length-1;++i){
			Link l=p.links[i];
			sb.append(l.up?UP:DOWN).append(s.forms[l.toIndex]);
		}
		sb.append(DOWN).append(s.tags[instance.ant.hd]);
		return sb.toString();
	}

}
