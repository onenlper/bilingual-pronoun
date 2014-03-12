package ims.coref.lang;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.PairInstance;
import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import util.Common;

public abstract class Language implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract FeatureSet getDefaultFeatureSet();	
	public abstract boolean cleverStringMatch(Span ant,Span ana);
	public abstract boolean isAlias(Span ant,Span ana);
	public abstract void computeAtomicSpanFeatures(Span s);
	public abstract int findNonTerminalHead(Sentence s,Node node);
	public abstract String getDefaultMarkableExtractors();
		
	public static Language english = new English();
	public static Language chinese = new Chinese();
	public static Language arabic = new Arabic();
	
	static Language lang;
	
	protected Language(){
		System.out.println("Initializing language: "+this.getClass().getCanonicalName());
	}
	
	public static Language initLanguage(String lang){
		if(lang.startsWith("eng")){
			Language.lang= english;
		} else if(lang.startsWith("chi")) {
			Language.lang= chinese;
		} else if(lang.startsWith("ara")) {
			Language.lang= arabic;
		} else {
			throw new RuntimeException("Unknown language: "+lang);
		}
		return Language.lang;
	}
	
	public static Language getLanguage(String lang) {
		if(lang.startsWith("eng")){
			return english;
		} else if(lang.startsWith("chi")) {
			return chinese;
		} else if(lang.startsWith("ara")) {
			return arabic;
		} else {
			throw new RuntimeException("Unknown language: "+lang);
		}
	}

	public static Language getLanguage(){
		return lang;
	}
	public static void setLanguage(Language lang) {
		Language.lang=lang;
	}

	public boolean cleverStringMatch(PairInstance pi){
		return cleverStringMatch(pi.ant,pi.ana);
	}
	
	public boolean isAlias(PairInstance pi){
		return isAlias(pi.ant,pi.ana);
	}

	public void preprocessSentence(Sentence s){
		//Do nothing -- but you may overload this
	}
	public Set<String> getNonReferentialTokenSet() {
		throw new Error("not implemented for this language");
	}
	
	public String getLang() {
		return this.getClass().getSimpleName().substring(0, 3).toLowerCase();
	}
}