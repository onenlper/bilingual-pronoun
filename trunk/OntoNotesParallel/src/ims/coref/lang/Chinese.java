package ims.coref.lang;

import ims.coref.data.CFGTree.Node;
import ims.coref.data.CFGTree.NonTerminal;
import ims.coref.data.CFGTree.Terminal;
import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;
import ims.coref.features.Lone_genra;
import ims.headrules.HeadFinder;
import ims.headrules.HeadRules;
import ims.headrules.HeadRules.Direction;
import ims.headrules.HeadRules.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Chinese extends Language {
	private static final long serialVersionUID = 1L;
	
	@Override
	public FeatureSet getDefaultFeatureSet() {
		String[] names={
				"AntecedentHdForm+AnaphorHdForm",
				"AntecedentHdPos",
				"ExactStringMatch",
				"DistanceBucketed",
				"DistanceBucketed+AnaphorPronoun",
				"SameSpeaker+AntecedentPronounForm+AnaphorPronounForm",
				"AntecedentWholeSpanForm",
				"AntecedentSPrPos",
				"AnaphorCFGSubCat+Nested",
				"Genre+Nested",
				"CFGSSPath",
				"AnaphorSFForm+AntecedentHdForm",
				"AntecedentSPrForm",
				"CFGDSPath",
				"AnaphorSPrPos",
				"Genre+AntecedentSFForm",
				"AntecedentCFGSubCat",
				"CFGSSPath+AnaphorPronounForm",
				"AnaphorWholeSpanForm+AntecedentWholeSpanForm",
				"DistanceBucketed+AnaphorHdForm",
				"ExactStringMatch+DistanceBucketed",
				"AntecedentCFGParentSubCat",
				"Genre+AntecedentHdForm",
				"Nested",
				"CFGSSPath+Genre",
				"AntecedentCFGSubCat+Nested",
				"Genre+Nested+AnaphorHdFormSubStringMatch+AnaphorProperName+AntecedentProperName",
				"AntecedentCFGParentSubCat+AnaphorSPrPos",
				"AnaphorHdFormSubStringMatch+AnaphorProperName+AntecedentProperName+MentionDistBigBuckets+AntecedentPronoun",
				"AntecedentCFGParentSubCat+AnaphorHdForm"
		};
//		String[] names={
//				"AntecedentHdForm+AnaphorHdForm",
//				"AntecedentHdPos",
//				"ExactStringMatch",
//				"DistanceBucketed",
//				"DistanceBucketed+AnaphorPronoun",
//				"SameSpeaker+AntecedentPronounForm+AnaphorPronounForm",
//				"AntecedentWholeSpanForm",
//				"AntecedentSPrPos",
//				"AnaphorCFGSubCat+Nested",
//				"Genre+Nested",
//				"CFGSSPath",
//				"AnaphorSFForm+AntecedentHdForm",
//				"AntecedentSPrForm",
//				"CFGDSPath",
//				"AnaphorSPrPos",
//				"Genre+AntecedentSFForm",
//				"AntecedentCFGSubCat",
//				"CFGSSPath+AnaphorPronounForm",
//				"AnaphorWholeSpanForm+AntecedentWholeSpanForm",
//				"AntecedentNamedEntity",
//				"DistanceBucketed+AnaphorHdForm",
//				"ExactStringMatch+DistanceBucketed"};


		return FeatureSet.getFromNameArray(names);
	}

	@Override
	public boolean cleverStringMatch(Span ant,Span ana) {
		throw new Error("not implemented");
	}

	@Override
	public boolean isAlias(Span ant,Span ana) {
		throw new Error("not implemented");
	}

	@Override
	public void computeAtomicSpanFeatures(Span s) {
		s.isPronoun=(s.size()==1 && s.s.tags[s.start].equals("PN"));
		s.isProperName=isProperName(s);
		s.isDemonstrative=isDemonstrative(s);
	}

	
	private static final Set<String> DEMONSTRATIVES=new HashSet<String>();
	static {
		Collections.addAll(DEMONSTRATIVES,
			"这","这些",
			"该", //Not sure if this is the 'that' from the annotation guidelines
			"本", //I'm not entirely sure -- is this the same character that's used for 'our' in the pos annotation guidelines?
			"那","那些");
	}
	private boolean isDemonstrative(Span s) {
		return DEMONSTRATIVES.contains(s.s.forms[s.start]);
	}

	private boolean isProperName(Span s) {
		for(int i=s.start;i<=s.end;++i){
			if(!s.s.tags[i].equals("NR"))
				return false;
		}
		return true;
	}

	@Override
	public int findNonTerminalHead(Sentence s, Node n) {
		if(n==null)
			return -1;
		if(n instanceof Terminal)
			return n.beg;
		NonTerminal nt=(NonTerminal) n;
		int h=headFinder.findHead(s, nt);
		if(h<1)
			return nt.end;
		else
			return h;
	}
	
	static final HeadFinder headFinder;
	
	static {
		Map<String,HeadRules> m=new HashMap<String,HeadRules>();
		String[] zParRules={
		"ADJP:r ADJP|JJ|AD;r .*",
		"ADVP:l CS; r ADVP|AD|JJ|NP|PP|P|VA|VV;r .*",
		"CLP:r CLP|M|NN|NP;r .*",
		"CP:r DEC|CP|ADVP|IP|VP;r .*",
		"DNP:r DEG|DNP|DEC|QP;r .*",
		"DP:r QP|M|CLP;l DP|DT|OD;l .*",
		"DVP:r DEV|AD|VP;r .*",
		"IP:r VP|IP|NP;r .*",
		"LCP:r LCP|LC;r .*",
		"LST:r CD|NP|QP;r .*",
		"NP:r NP|NN|IP|NR|NT;r .*",
		"NN:r NP|NN|IP|NR|NT;r .*",
		"PP:l P|PP;l .*",
		"PRN:l PU;l .*",
		"QP:r QP|CLP|CD|OD;r .*",
		"UCP:r IP|NP|VP;r .*",
		"VCD:r VV|VA|VE;r .*",
		"VP:l VE|VC|VV|VNV|VPT|VRD|VSB|VCD|VP|IP;l .*",
		"VPT:l VA|VV;l .*",
		"VRD:l VV|VA;l .*",
		"VSB:r VV|VE;r .*",
		"FRAG:r VP|VV|NP|NR|NN|NT;r .*",};
		for(String line:zParRules){
			String[] a=line.split(":");
			String[] b=a[1].split(";");
			Rule[] rules=new Rule[b.length];
			int i=0;
			for(String s:b){
				String[] c=s.split(" ");
				Direction d=(c[0].equals("r")?Direction.RightToleft:Direction.LeftToRight);
				rules[i++]=new Rule(d,Pattern.compile(c[1]));
			}
			m.put(a[0], new HeadRules(a[0],rules));
		}
		headFinder=new HeadFinder(m);
	}

	@Override
	public String getDefaultMarkableExtractors() {
		return "NT-NP,T-PN,T-NR";//,NonReferential";
	}

}
