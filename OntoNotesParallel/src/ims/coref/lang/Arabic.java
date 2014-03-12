package ims.coref.lang;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;
import ims.coref.features.enums.Gender;
import ims.coref.features.enums.Num;

public class Arabic extends Language{
	private static final long serialVersionUID = 1L;

	@Override
	public FeatureSet getDefaultFeatureSet() {
	String[] names={	
		"AntecedentHdPos",
		"DistanceBucketed+AnaphorPronoun",
		"AntecedentPronounForm+AnaphorPronounForm",
		"AntecedentWholeSpanForm",
		"CFGSSPath",
		"AntecedentSPrPos",
		"AntecedentCFGParentSubCat",
		"CFGSSPath+AnaphorPronoun+AntecedentPronoun",
		"AntecedentHdPos+AntecedentHdIPPos",
		"AntecedentHdForm",
		"CleverStringMatch+AntecedentProperName",
		"AnaphorHdForm+AntecedentHdPos",
		"CFGSSPosPath",
		"AnaphorWholeSpanFormAntecedentWholeSpanFormEditDistance",
		"AntecedentBagOfForm+AnaphorHdForm",
		"AntecedentBagOfLemma",
		"AnaphorHdPos+AntecedentHdLemma",
		"AnaphorQuoted+AnaphorPronounForm+AntecedentDominatingVerb",
		"AnaphorWholeSpanFormAntecedentWholeSpanFormEditScript",
		"AnaphorWholeSpanBWUVAntecedentWholeSpanBWUVEditScript",
		"AntecedentHdPPForm",
		"AnaphorBagOfBWUV+AntecedentHdBWUV",
		"MentionDistBuckets+AnaphorPronoun",
		"AntecedentHdFLChar",
		"AntecedentBagOfLemma+AnaphorHdForm",
		"AntecedentSPrPos+AnaphorHdForm"};
//		String[] names={
//				"AntecedentHdForm+AnaphorHdForm",
//				"AntecedentHdPos",
//				"DistanceBucketed+AnaphorPronoun",
//				"AntecedentPronounForm+AnaphorPronounForm",
//				"AntecedentWholeSpanForm",
//				"ExactStringMatch",
//				"CFGSSPath",
//				"AnaphorSFForm+AntecedentHdForm",
//				"AntecedentSPrPos",
//				"AntecedentCFGParentSubCat",
//				"CFGSSPath+AnaphorPronoun",
//				"AntecedentHdPos+AntecedentHdIPPos",
//				"AntecedentHdForm",
//				"CleverStringMatch+AntecedentProperName",
//				"AnaphorHdForm+AntecedentHdPos",
//				"ExactStringMatch+DistanceBucketed",
//				"CFGSSPosPath",
//				"AnaphorSFoForm",
//				"AnaphorWholeSpanForm+AntecedentWholeSpanForm",
//				"AnaphorSLForm+AntecedentSLForm",
//				"AnaphorHdPos+AntecedentHdForm",
//				"CFGSSPath+AntecedentPronoun",
//				"AntecedentSFForm+AntecedentSLPos"};
		return FeatureSet.getFromNameArray(names);
	}

	@Override
	public boolean cleverStringMatch(Span ant,Span ana) {
		String s1=getCleverStringMatchString(ant);
		String s2=getCleverStringMatchString(ana);
		return s1.length()>0 && s1.equals(s2);
	}

	private String getCleverStringMatchString(Span span) {
		StringBuilder sb=new StringBuilder();
		for(int i=span.start;i<=span.end;++i){
			if(span.s.tags[i].equals("PUNC"))
				continue;
			sb.append(span.s.forms[i]).append(" ");
		}
		return sb.toString();
	}

	@Override
	public boolean isAlias(Span ant,Span ana) {
		throw new Error("not implemented");
	}

	@Override
	public void computeAtomicSpanFeatures(Span s) {
		s.isPronoun=(s.size()==1 && s.s.tags[s.end].startsWith("PRP"));
		s.isProperName=isProperName(s);
		s.isDefinite=isDefinite(s);
		s.isQuoted=isQuoted(s);
		assignGenderAndNumber(s);
	}

	private boolean isQuoted(Span s) {
		//We want an odd number of quotes on either side for this to hold.
		int l=0;
		for(int i=s.start-1;i>0;--i)
			if(s.s.forms[i].equals('"'))
				l++;
		if(l%2==0)
			return false;
		int r=0;
		for(int j=s.end+1;r<s.s.forms.length;++j)
			if(s.s.forms[j].equals('"'))
				r++;
		return r%2==1;
	}

	private boolean isDefinite(Span s) {
		//Get the surface form of the head and then check for prefix
		String headForm=s.s.forms[s.hd];
		boolean isDefinite=headForm.startsWith("ال") 
		|| headForm.startsWith("-ال");
		return isDefinite;
	}

	private boolean isProperName(Span s) {
		boolean isProperName=false;
		for(int i=s.start;i<=s.end;++i){
			if(s.s.tags[i].equals("PUNC"))
				continue;
			if(s.s.tags[i].startsWith("NNP"))
				isProperName=true;
			else
				return false;
		}
		return isProperName;
	}

	@Override
	public int findNonTerminalHead(Sentence s, Node n) {
		return English.findEnglishCFGHead(s, n); //We're just lazy here. Let's try the English head rules
	}

	@Override
	public String getDefaultMarkableExtractors() {
		return "NT-NP,T-PRP,T-PRP$";//,NonReferential";
	}

	public void preprocessSentence(Sentence s){
		s.bwuv[0]="<root-bwuv>";
		//Get rid of all garbage.
		if(s.forms[1].contains("#")){
			for(int i=1;i<s.forms.length;++i){
				String[] q=s.forms[i].split("\\#");
				if(q.length!=4)
					throw new Error("HERE");
				s.forms[i]=q[0];
				s.bwuv[i]=q[2];
			}
		}
	}
	
	
	private void assignGenderAndNumber(Span s) {
		String hdForm=s.s.forms[s.hd];
		{ //Pronouns
			String form=hdForm.replaceAll("-", "");
			if(SING_PRONOUNS.contains(form))
				s.number=Num.Sin;
			else if(DUAL_PRONOUNS.contains(form))
				s.number=Num.Dua;
			else if(PLURAL_PRONOUNS.contains(form))
				s.number=Num.Plu;
			if(MASCULINE_PRONOUNS.contains(form))
				s.gender=Gender.Masc;
			else if(FEMININE_PRONOUNS.contains(form))
				s.gender=Gender.Fem;
			
			if(DEMONSTRATIVE_PRONOUNS.contains(form))
				s.isDemonstrative=true;
		}
		{ //Suffixes
		if(s.gender==Gender.Unknown && s.number==Num.Unknown){
			if(hdForm.endsWith("اتُ") || hdForm.endsWith("اتِ")){
				s.gender=Gender.Fem;
				s.number=Num.Plu;
			} else if(hdForm.endsWith("ةِ")){
				s.gender=Gender.Fem;
				s.number=Num.Sin;
			} else if(hdForm.endsWith("تانِ")){
				s.gender=Gender.Fem;
				s.number=Num.Dua;
			} else if(hdForm.endsWith("ينَ") || hdForm.endsWith("ونَ")){
				s.gender=Gender.Masc;
				s.number=Num.Sin;
			} else if(hdForm.endsWith("لَيْنِ")){
				s.gender=Gender.Masc;
				s.number=Num.Dua;
			}
			//XXX I couldn't really find any suffixes for male singular. No idea about this. Can't be assed to look more for this, not sure if it's gonna help anyway
		}
		}
	}
	
	static final Set<String> SING_PRONOUNS;
	static final Set<String> DUAL_PRONOUNS;
	static final Set<String> PLURAL_PRONOUNS;
	static final Set<String> MASCULINE_PRONOUNS;
	static final Set<String> FEMININE_PRONOUNS;
	static final Set<String> DEMONSTRATIVE_PRONOUNS;
	
	static {
		String[] aSING_PRONOUNS={"نِي","كَ","كِ","هِ","هُ","ها","هُوَ","هِيَ","هِي","ي","يَ","ي","أَنا","أَنْتَ","أَنْتَ","تِلْكَ","ذاكَ","ذ`لِكَ","ه`ذا"};
		String[] aDUAL_PRONOUNS={"هِما","هُما","هاتانِ","هاتَيْنِ","ه`ذَيْنِ"};
		String[] aPLURAL_PRONOUNS={"كُم","نَحْنُ","هِم","هُم","هُنَّ","هِنَّ","أَنْتُم","أُولَئِكَ","ه`ؤُلاءِ"}; //"نا" -- both plural and dual -- but seems to be predominantly plural
		String[] aMASC_PRONOUNS={"كَ","كُم","هِ","هُ","هِم","هُم","هُوَ","أَنْتَ","أَنْتَ","أَنْتُم","أُولَئِكَ","ذاكَ","ذ`لِكَ","ه`ذا","ه`ذَيْنِ","ه`ؤُلاءِ"};
		String[] aFEM_PRONOUNS={"كِ","ها","هُنَّ","هِنَّ","هِيَ","هِي","تِلْكَ","هاتانِ","هاتَيْنِ","ه`ذِهِ"};
		String[] aDEM_PRONOUNS={"أُولَئِكَ","تِلْكَ","ذاكَ","ذ`لِكَ","هاتانِ","هاتَيْنِ","ه`ذا","ه`ذِهِ","ه`ذَيْنِ","ه`ؤُلاءِ"};
		SING_PRONOUNS=new HashSet<String>();
		for(String s:aSING_PRONOUNS)
			SING_PRONOUNS.add(s);
		
		DUAL_PRONOUNS=new HashSet<String>();
		for(String s:aDUAL_PRONOUNS)
			DUAL_PRONOUNS.add(s);
		
		PLURAL_PRONOUNS=new HashSet<String>();
		for(String s:aPLURAL_PRONOUNS)
			PLURAL_PRONOUNS.add(s);
		
		MASCULINE_PRONOUNS=new HashSet<String>();
		for(String s:aMASC_PRONOUNS)
			MASCULINE_PRONOUNS.add(s);
		
		FEMININE_PRONOUNS=new HashSet<String>();
		for(String s:aFEM_PRONOUNS)
			FEMININE_PRONOUNS.add(s);
		
		DEMONSTRATIVE_PRONOUNS=new HashSet<String>();
		for(String s:aDEM_PRONOUNS)
			DEMONSTRATIVE_PRONOUNS.add(s);
	}

}
