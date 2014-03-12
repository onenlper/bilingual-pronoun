package ims.coref.features;

import ims.coref.Parallel;
import ims.coref.TrainCC;
import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.enums.BigBuckets;
import ims.coref.features.enums.Buckets;
import ims.coref.features.enums.IBuckets;
import ims.coref.features.enums.LeftRight;
import ims.coref.features.extractors.SpanToken;
import ims.coref.features.extractors.SpanTokenExtractor;
import ims.coref.features.extractors.TargetSpan;
import ims.coref.features.extractors.TargetSpanExtractor;
import ims.coref.features.extractors.TokenTrait;
import ims.coref.features.extractors.TokenTraitExtractor;
import ims.coref.io.DocumentReader;
import ims.coref.lang.Language;
import ims.coref.resolver.AbstractResolver;
import ims.coref.training.ITrainingExampleExtractor;
import ims.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.bwaldvogel.liblinear.FeatureNode;

public class FeatureSet implements Serializable {
	private static final long serialVersionUID = 1L;

	private final List<IFeature> features;

	private FeatureSet(List<IFeature> features) {
		this.features = features;
	}

	public void jointRegisterAll(DocumentReader engReader,
			DocumentReader chiReader, ITrainingExampleExtractor engTeex,
			ITrainingExampleExtractor chiTeex) {
		registerLang(engReader, engTeex, "eng");
		registerLang(chiReader, chiTeex, "chi");
//		Language.initLanguage("chi");
//		for (Document d : chiReader) {
//			List<PairInstance> instances = chiTeex.getInstances(d);
//			for (PairInstance pi : instances) {
//				for (IFeature f : features) {
//					if (f.getLang().equals(d.lang) && f.isLoneFea()==pi.isLone()) {
//						f.register(pi, d);
//					}
//				}
//
//				if (this.bilingual) {
//					Span xAnt = pi.ant.getXSpan();
//					Span xAna = pi.ana.getXSpan();
//					if (xAnt != null && xAna != null) {
//						Document xD = xAna.s.d;
//						PairInstance xpi = new PairInstance(xAnt, xAna,
//								pi.mentionDist, pi.nesBetween);
//						for (IFeature f : features) {
//							if (f.getLang().equals(xD.lang) && f.isLoneFea()==pi.isLone()) {
//								f.register(xpi, xD);
//							}
//						}
//					}
//				}
//			}
//		}

		for (IFeature f : features) {
			f.freeze();
		}
	}

	private void registerLang(DocumentReader engReader,
			ITrainingExampleExtractor engTeex, String lang) {
		Language.initLanguage(lang);
		for (Document d : engReader) {
			List<PairInstance> instances = engTeex.getInstances(d);
			for (PairInstance pi : instances) {
				for (IFeature f : features) {
					if (f.getLang().equals(d.lang) && f.isLoneFea()==pi.isLone()) {
						f.register(pi, d);
					}
				}

				if (this.bilingual) {
					Span xAnt = pi.ant.getXSpan();
					Span xAna = pi.ana.getXSpan();
					if (xAnt != null && xAna != null) {
						Document xD = xAna.s.d;
						PairInstance xpi = new PairInstance(xAnt, xAna,
								pi.mentionDist, pi.nesBetween);
						for (IFeature f : features) {
							if (f.getLang().equals(xD.lang) && f.isLoneFea()==pi.isLone()) {
								f.register(xpi, xD);
							}
						}
					}
				}
			}
		}
	}

	public void registerAll(DocumentReader reader,
			ITrainingExampleExtractor teex) {
		for (Document d : reader) {
			List<PairInstance> instances = teex.getInstances(d);
			for (PairInstance pi : instances) {
				for (IFeature f : features) {
					if (f.getLang().equals(d.lang)) {
						f.register(pi, d);
					}
				}
			}
		}
		for (IFeature f : features)
			f.freeze();
	}

	// public List<FeatureNode> getFeatureNodes(PairInstance instance, Document
	// d, List<String[]> feas) {
	// int offset = 0;
	// List<FeatureNode> fns = new ArrayList<FeatureNode>();
	//
	// feas.clear();
	// int lastSize = 0;
	// for (IFeature f : features) {
	// if (f.getLang().equals("eng")) {
	// if (d.lang.equals(f.getLang())) {
	// f.contributeFeatureNodes(instance, offset, fns, d);
	// } else {
	// Span xAnt = instance.ant.getXSpan();
	// Span xAna = instance.ana.getXSpan();
	// if (xAnt != null && xAna != null) {
	// Document xD = xAnt.s.d;
	// PairInstance xpi = new PairInstance(xAnt, xAna,
	// instance.mentionDist, instance.nesBetween);
	// f.contributeFeatureNodes(xpi, offset, fns, xD);
	// }
	// }
	// offset += f.size();
	// } else {
	// if (f.getLang().equals("chi")) {
	// if (d.lang.equals(f.getLang())) {
	// f.contributeFeatureNodes(instance, offset, fns, d);
	// } else {
	// Span xAnt = instance.ant.getXSpan();
	// Span xAna = instance.ana.getXSpan();
	// if (xAnt != null && xAna != null) {
	// Document xD = xAnt.s.d;
	// PairInstance xpi = new PairInstance(xAnt, xAna,
	// instance.mentionDist, instance.nesBetween);
	// f.contributeFeatureNodes(xpi, offset, fns, xD);
	// }
	// }
	// offset += f.size();
	// }
	// }
	// String[] add = new String[2];
	// add[0] = f.getName();
	// if(fns.size()!=lastSize) {
	// add[1] = Integer.toString(fns.get(fns.size()-1).index);
	// } else {
	// add[1] = "0";
	// }
	// feas.add(add);
	// lastSize = fns.size();
	// }
	// return fns;
	// }
	public boolean bilingual = false;

	public boolean thisLang = false;
	public boolean xLang = false;
	
	public boolean x2 = false;

	public List<FeatureNode> getFeatureNodes(PairInstance pi, Document d) {
		int offset = 0;
		List<FeatureNode> fns = new ArrayList<FeatureNode>();
		for (IFeature f : features) {
			if( f.isLoneFea() == pi.isLone() ) {
				if (d.lang.equals(f.getLang()) ) {
					if (this.thisLang || this.bilingual) {
						f.contributeFeatureNodes(pi, offset, fns, d);
					}
				} else if (this.bilingual || this.xLang) {
					PairInstance xpi = pi.getXInstance();
					if (xpi != null
							&& (xpi.ant.empty || xpi.ana.s.d.docNo
									.equalsIgnoreCase(xpi.ant.s.d.docNo))) {
						f.contributeFeatureNodes(xpi, offset, fns,
								xpi.ana.s.d);
					}
				}
			}
//			if (f.getLang().equals("eng")) {
//				//TODO
//				if (d.lang.equals(f.getLang()) ) {
//					if ((this.thisLang || this.bilingual)) {
//						f.contributeFeatureNodes(pi, offset, fns, d);
//					}
//				} else if (this.bilingual || this.xLang) {
//					PairInstance xpi = pi.getXInstance();
//					if (xpi != null
//							&& (xpi.ant.empty || xpi.ana.s.d.docNo
//									.equalsIgnoreCase(xpi.ant.s.d.docNo))) {
//						f.contributeFeatureNodes(xpi, offset, fns, xpi.ana.s.d);
//					}
//				}
//			} else {
//				if (f.getLang().equals("chi")) {
//				}
//			}
			offset += f.size();
		}
		// TODO add alignment feature
		if (((Parallel.jointTest || Parallel.ensemble) && this.bilingual)|| this.x2) {
//			Span ant = instance.ant;
//			Span ana = instance.ana;
//
//			int ord = ant.xSpanType < ana.xSpanType ? ana.xSpanType
//					: ant.xSpanType;
//			int i = 1 + offset + ord;
//			FeatureNode fn = new FeatureNode(i, 1.0);
//			fns.add(fn);
		}
		return fns;
	}

	public void appendFeatureNodes(PairInstance pi, Document d, int offset,
			List<FeatureNode> sink) {
		for (IFeature f : features) {
			f.contributeFeatureNodes(pi, offset, sink, d);
			offset += f.size();
		}
	}

	public static FeatureSet getFromFile(File featureSetFile)
			throws IOException {
		BufferedReader in = Util.getReader(featureSetFile);
		String line;
		List<String> names = new ArrayList<String>();
		while ((line = in.readLine()) != null) {
			if (line.startsWith("#") || line.length() == 0)
				continue;
			if (line.contains(" ")) {
				String[] a = line.split(" ");
				if (a[0].length() > 0)
					names.add(a[0]);
			} else {
				names.add(line);
			}
		}
		return getFromNameList(names);
	}

	public static FeatureSet getFromNameList(List<String> names) {
		return getFromNameArray(names.toArray(new String[names.size()]));
	}

	public static FeatureSet getFromNameArray(String... names) {
		String duplicate = getFirstDuplicate(names);
		if (duplicate != null)
			throw new RuntimeException("Duplicate feature: " + duplicate);
		List<IFeature> features = new ArrayList<IFeature>();
		for (String name : names) {
			IFeature f = getFromName(name);
			f.setLang(Language.getLanguage().getLang());
			features.add(f);
		}
		return new FeatureSet(features);
	}

	private static String getFirstDuplicate(String[] names) {
		Set<String> canonicalNames = new HashSet<String>();
		for (String n : names) {
			String cn;
			if (n.contains("+")) {
				Matcher m = CUTOFF_PATTERN.matcher(n);
				if (m.matches())
					n = n.replace("-" + m.group(1), "");
				String[] a = n.split("\\+");
				Arrays.sort(a);
				StringBuilder sb = new StringBuilder(a[0]);
				for (int i = 1; i < a.length; ++i)
					sb.append("+").append(a[i]);
				cn = sb.toString();
			} else {
				cn = n;
			}
			if (canonicalNames.contains(cn))
				return cn;
			canonicalNames.add(cn);
		}
		return null;
	}

	private static final String SPAN = "(Anaphor|Antecedent)";
	private static final String TOKEN = "(Hd|HdGov|HdLmc|HdRmc|HdRs|HdLs|HdP|HdPP|HdN|HdNN|HdIP|HdIPP|HdIN|HdINN|SF|SL|SFo|SPr)";
	private static final String TRAIT = "(Form|Pos|Fun|Lemma|Brown|BrownMid|BrownShort|FFChar|FLChar|FF2Char|FL2Char|BWUV)";
	private static final String LEFTRIGHT = "(Left|Right)";
	private static final Pattern CUTOFF_PATTERN = Pattern
			.compile("^.*-(\\d+)$");
	private static final Pattern GENDER_NUMBER_PRONOUN_PATTERN = Pattern
			.compile(SPAN
					+ "(Gender|Number|Pronoun|Demonstrative|Definite|ProperName|SemanticClass|PronounForm|NamedEntity|DominatingVerb(?:WD)?|Quoted|BarePlural|Anaphoricity)");
	private static final Pattern PATH_PATTERN = Pattern
			.compile("^(SS|DS)Path(Form|Pos|Fun|Bergsma)$");
	private static final Pattern GENERIC_PATTERN = Pattern.compile(SPAN + TOKEN
			+ TRAIT);
	private static final Pattern SUBCAT_PATTERN = Pattern.compile(SPAN + TOKEN
			+ "SubCat" + TRAIT);
	private static final Pattern WHOLESPAN_PATTERN = Pattern.compile(SPAN
			+ "WholeSpan" + TRAIT);
	private static final Pattern CFGFEAT_PATTERN = Pattern.compile(SPAN
			+ "CFG((?:Parent)?(?:Category|SubCat))");
	private static final Pattern CFGPATH_PATTERN = Pattern
			.compile("CFG((?:SS|DS)(?:Form|Pos|Lemma|BWUV)?)Path");
	private static final Pattern MATCHTT_PATTERN = Pattern.compile("Match"
			+ TOKEN + TRAIT);
	private static final Pattern SUBSTRING_MATCH_PATTERN = Pattern.compile(SPAN
			+ TOKEN + TRAIT + "SubStringMatch");
	private static final Pattern MEASURE_WORD_PATTERN = Pattern.compile(SPAN
			+ LEFTRIGHT + "MeasureWord");
	private static final Pattern EDIT_DISTANCE_PATTERN = Pattern
			.compile(SPAN
					+ "(WholeSpan(?:Form|Lemma|BWUV)|Hd(?:Form|Lemma|BWUV))"
					+ SPAN
					+ "(WholeSpan(?:Form|Lemma|BWUV)|Hd(?:Form|Lemma|BWUV))((?:Reverse)?Edit(?:Distance|Script))");
	private static final Pattern SPAN_BAG_OF_TRAIT_PATTERN = Pattern
			.compile(SPAN + "BagOf" + TRAIT);
	private static final Pattern MENTION_AND_NE_DIST_PATTERN = Pattern
			.compile("(Mention|NE)Dist((?:Big)?Buckets)");
	private static final Pattern LEFTRIGHT_VERBS = Pattern.compile(SPAN
			+ LEFTRIGHT + "Verb" + "(OO|S)");
	private static final Pattern SPEAKER_SUBSTRING_MATCH = Pattern.compile(SPAN
			+ "ContainsProperNounWithOtherSpeakerSubstringMatch");
	private static final Pattern CFGPATH_BAG_OF_LABELS_PATTERN = Pattern
			.compile("CFG(SS|DS)PathBagOf(?:Form|Pos|Lemma|BWUV|Label)");
	private static final Pattern LAST_SPEAKER_PATTERN = Pattern.compile(SPAN
			+ "SpeakerSameAsLastSpeaker");

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static IFeature getFromName(String name) {
		Matcher m = CUTOFF_PATTERN.matcher(name);
		int cutOff = 0;
		if (m.matches()) {
			cutOff = Integer.parseInt(m.group(1));
			name = name.replace("-" + m.group(1), "");
		}
		// First check if this is some kind of N-gram feature (contains +)
		if (name.contains("+")) {
			String[] nn = name.split("\\+", 2);
			IFeature af1 = getFromName(nn[0]);
			af1.setLang(Language.getLanguage().getLang());
			IFeature af2 = getFromName(nn[1]);
			af2.setLang(Language.getLanguage().getLang());
			return BigramFactory.getBigram(af1, af2, cutOff);
		}
		// Then we try for exact strings.
		if (name.equals("XExist")) {
			return new F_XExist();
		} else if (name.equals("Alias")) {
			return new F_Alias();
		} else if (name.equals("CleverStringMatch")) {
			return new F_CleverStringMatch();
		} else if (name.equals("ExactStringMatch")) {
			return new F_ExactStringMatch();
		} else if (name.equals("Distance")) {
			return new F_Distance();
		} else if (name.equals("DistanceBucketed")) {
			return new F_DistanceBucketed();
		} else if (name.equals("Genre")) {
			return new F_Genre();
		} else if (name.equals("SameSpeaker")) {
			return new F_SameSpeaker();
		} else if (name.equals("StackEnum")) {
			return new F_StackEnum();
		} else if (name.equals("Nested")) {
			return new F_Nested();
		} else if (name.equals("WordNetSynonyms")) {
			return new F_WordNetSynonyms();
		} else if (name.equals("RAND")) {
			return new F_Rand();
		} else if (name.equals("")) {
		} else if (name.equals("")) {
		} else if (name.equals("")) {
		}
		// Then we try stuff like gender/number/pronoun/demonstrative/definite
		Matcher m1 = GENDER_NUMBER_PRONOUN_PATTERN.matcher(name);
		if (m1.matches()) {
			final TargetSpanExtractor tse = new TargetSpanExtractor(
					TargetSpan.valueOf(m1.group(1)));
			String type = m1.group(2);
			if (type.equals("Gender")) {
				return new F_Gender(tse);
			} else if (type.equals("Number")) {
				return new F_Number(tse);
			} else if (type.equals("Definite")) {
				return new F_IsDefinite(tse);
			} else if (type.equals("Demonstrative")) {
				return new F_IsDemonstrative(tse);
			} else if (type.equals("Pronoun")) {
				return new F_IsPronoun(tse);
			} else if (type.equals("ProperName")) {
				return new F_IsProperName(tse);
			} else if (type.equals("SemanticClass")) {
				return new F_SemanticClass(tse);
			} else if (type.equals("PronounForm")) {
				return new F_PronounForm(tse);
			} else if (type.equals("NamedEntity")) {
				return new F_NamedEntity(tse, cutOff);
			} else if (type.startsWith("DominatingVerb")) {
				return new F_DominatingVerb(tse, type.endsWith("WD"), cutOff);
			} else if (type.equals("Quoted")) {
				return new F_Quoted(tse);
			} else if (type.equals("BarePlural")) {
				return new F_BarePlural(tse);
			} else if (type.equals("Anaphoricity")) {
				return new F_AnaphoricityBucketed(tse);
			} else {
				throw new Error("!");
			}
		}
		// Then we try path stuff
		Matcher m2 = PATH_PATTERN.matcher(name);
		if (m2.matches()) {
			if (m2.group(1).equals("SS")) { // Same sentence
				TokenTrait tt = m2.group(2).equals("Bergsma") ? null
						: TokenTrait.valueOf(m2.group(2));
				return new F_SSPathFeature(tt, cutOff);
			} else { // Diff sentence
				TokenTrait tt = TokenTrait.valueOf(m2.group(2));
				return new F_DSPathFeature(tt, cutOff);
			}
		}
		// Then we try the generic AnaphorHdForm etc
		Matcher m3 = GENERIC_PATTERN.matcher(name);
		if (m3.matches()) {
			final TargetSpanExtractor tse = new TargetSpanExtractor(
					TargetSpan.valueOf(m3.group(1)));
			final SpanTokenExtractor ste = new SpanTokenExtractor(
					SpanToken.valueOf(m3.group(2)));
			final TokenTraitExtractor tte = new TokenTraitExtractor(
					TokenTrait.valueOf(m3.group(3)));
			return new F_GenericSpanTokenTraitFeature(tse, ste, tte, cutOff);
		}
		// WholeSpan
		Matcher m4 = WHOLESPAN_PATTERN.matcher(name);
		if (m4.matches()) {
			final TargetSpanExtractor tse = new TargetSpanExtractor(
					TargetSpan.valueOf(m4.group(1)));
			final TokenTraitExtractor tte = new TokenTraitExtractor(
					TokenTrait.valueOf(m4.group(2)));
			return new F_WholeSpan(tse, tte, cutOff);
		}
		Matcher m5 = SUBCAT_PATTERN.matcher(name);
		if (m5.matches()) {
			final TargetSpanExtractor tse = new TargetSpanExtractor(
					TargetSpan.valueOf(m5.group(1)));
			final SpanTokenExtractor ste = new SpanTokenExtractor(
					SpanToken.valueOf(m5.group(2)));
			final TokenTraitExtractor tte = new TokenTraitExtractor(
					TokenTrait.valueOf(m5.group(3)));
			return new F_SubCat(tse, ste, tte, cutOff);
		}
		Matcher m6 = CFGFEAT_PATTERN.matcher(name);
		if (m6.matches()) {
			TargetSpanExtractor tse = new TargetSpanExtractor(
					TargetSpan.valueOf(m6.group(1)));
			if (m6.group(2).equals("Category")) {
				return new F_CFGCategory(tse, cutOff);
			} else if (m6.group(2).equals("ParentCategory")) {
				return new F_CFGParentCategory(tse, cutOff);
			} else if (m6.group(2).equals("SubCat")) {
				return new F_CFGSubCat(tse, cutOff);
			} else if (m6.group(2).equals("ParentSubCat")) {
				return new F_CFGParentSubCat(tse, cutOff);
			}
		}
		Matcher m7 = CFGPATH_PATTERN.matcher(name);
		if (m7.matches()) {
			if (m7.group(1).equals("DS"))
				return new F_CFGDSPath(cutOff);
			else if (m7.group(1).equals("SS"))
				return new F_CFGSSPath(cutOff);
			else {
				String type = m7.group(1).substring(0, 2);
				String trait = m7.group(1).substring(2);
				TokenTrait tt = TokenTrait.valueOf(trait);
				if (type.equals("SS")) {
					return new F_CFGSSHeadTraitPath(tt, cutOff);
				} else {
					return new F_CFGDSHeadTraitPath(tt, cutOff);
				}
			}
		}
		Matcher m8 = MATCHTT_PATTERN.matcher(name);
		if (m8.matches()) {
			SpanToken st = SpanToken.valueOf(m8.group(1));
			TokenTrait tt = TokenTrait.valueOf(m8.group(2));
			return new F_MatchSpanTokenTrait(st, tt);
		}
		Matcher m9 = SUBSTRING_MATCH_PATTERN.matcher(name);
		if (m9.matches()) {
			TargetSpan ts = TargetSpan.valueOf(m9.group(1));
			SpanToken st = SpanToken.valueOf(m9.group(2));
			TokenTrait tt = TokenTrait.valueOf(m9.group(3));
			return new F_HdSubStringMatch(ts, st, tt);
		}
		Matcher m10 = MEASURE_WORD_PATTERN.matcher(name);
		if (m10.matches()) {
			TargetSpan ts = TargetSpan.valueOf(m10.group(1));
			LeftRight lr = LeftRight.valueOf(m10.group(2));
			return new F_LeftRightMesaureWord(ts, lr, cutOff);
		}
		Matcher m11 = EDIT_DISTANCE_PATTERN.matcher(name);
		if (m11.matches()) {
			TargetSpan ts1 = TargetSpan.valueOf(m11.group(1));
			TargetSpan ts2 = TargetSpan.valueOf(m11.group(3));
			if (ts1 == ts2)
				throw new Error(
						"Same target span for edit distance, sure you want to do this? Then change the code");
			AbstractSingleDataDrivenFeature asf1 = (AbstractSingleDataDrivenFeature) getFromName(m11
					.group(1) + m11.group(2));
			AbstractSingleDataDrivenFeature asf2 = (AbstractSingleDataDrivenFeature) getFromName(m11
					.group(3) + m11.group(4));
			if (m11.group(5).equals("EditDistance")) {
				return new F_EditDistance(asf1, asf2);
			} else if (m11.group(5).equals("EditScript")) {
				return new F_EditScript(asf1, asf2, cutOff, false);
			} else if (m11.group(5).equals("ReverseEditScript")) {
				return new F_EditScript(asf1, asf2, cutOff, true);
			} else {
				throw new Error("wrong edit script.");
			}
		}
		Matcher m12 = SPAN_BAG_OF_TRAIT_PATTERN.matcher(name);
		if (m12.matches()) {
			TargetSpan ts = TargetSpan.valueOf(m12.group(1));
			TokenTrait tt = TokenTrait.valueOf(m12.group(2));
			return new F_SpanBagOfTrait(ts, tt, cutOff);
		}
		Matcher m13 = MENTION_AND_NE_DIST_PATTERN.matcher(name);
		if (m13.matches()) {
			String type = m13.group(1);
			String bucketType = m13.group(2);
			// Not the nicest way to solve it cause we have construct raw types,
			// but it works, and is extendible
			Enum<? extends IBuckets<?>>[] buckets;
			if (bucketType.equals("Buckets"))
				buckets = Buckets.values();
			else if (bucketType.equals("BigBuckets"))
				buckets = BigBuckets.values();
			else
				throw new Error("!");

			if (type.equals("Mention"))
				return new F_MentionDist(buckets);
			else if (type.equals("NE"))
				return new F_NEsBetween(buckets);
			else
				throw new Error("!");

			// Works below, but extending it with more buckets and more features
			// will be a pain with all those nested ifs
			// if(type.equals("Mention")){
			// if(bucketType.equals("Buckets")){
			// return new F_MentionDist<Buckets>(Buckets.values());
			// } else if(bucketType.equals("BigBuckets")){
			// return new F_MentionDist<BigBuckets>(BigBuckets.values());
			// }
			// } else if(type.equals("NE")){
			// if(bucketType.equals("Buckets")){
			// return new F_NEsBetween<Buckets>(Buckets.values());
			// } else if(bucketType.equals("BigBuckets")){
			// return new F_NEsBetween<BigBuckets>(BigBuckets.values());
			// }
			//
			// }

			// Sample reflection code.. Doesn't work for now though. Not sure
			// how to get ahold of the constructor.
			// Class<? extends IFeature> clazz=null;
			// if(type.equals("Mention")){
			// clazz=F_MentionDist.class;
			// } else if(type.equals("NE")){
			// clazz=F_NEsBetween.class;
			// }
			// Class<?> argClazz=null;
			// Object buckets=null;
			// if(bucketType.equals("Buckets")){
			// argClazz=Object[].class;
			// buckets=Buckets.values();
			// } else if (bucketType.equals("BigBuckets")){
			// argClazz=Object[].class;
			// buckets=BigBuckets.values();
			// }
			// try {
			// Constructor<? extends IFeature> c=clazz.getConstructor(argClazz);
			// Object f=c.newInstance(buckets);
			// return (IFeature) f;
			// } catch (Exception e) {
			// e.printStackTrace();
			// System.exit(1);
			// }
		}

		Matcher m14 = LEFTRIGHT_VERBS.matcher(name);
		if (m14.matches()) {
			TargetSpan ts = TargetSpan.valueOf(m14.group(1));
			LeftRight lr = LeftRight.valueOf(m14.group(2));
			String o = m14.group(3);
			if (o.equals("OO")) {
				return new F_LeftRightFollowingVerbS(ts, lr, true, cutOff);
			} else if (o.equals("S")) {
				return new F_LeftRightFollowingVerbS(ts, lr, false, cutOff);
			} else {
				throw new Error("!");
			}
		}

		Matcher m15 = SPEAKER_SUBSTRING_MATCH.matcher(name);
		if (m15.matches()) {
			TargetSpan ts = TargetSpan.valueOf(m15.group(1));
			return new F_ProperNounSubstringInSpeakerInOther(ts);
		}

		Matcher m16 = CFGPATH_BAG_OF_LABELS_PATTERN.matcher(name);
		if (m16.matches()) {
			throw new Error("not implemented");
		}

		Matcher m17 = LAST_SPEAKER_PATTERN.matcher(name);
		if (m17.matches()) {
			TargetSpan ts = TargetSpan.valueOf(m17.group(1));
			return new F_LastSpeakerSameAsCurrentSpeaker(ts);
		}
		throw new RuntimeException("Unknown feature: " + name);
	}

	public List<IFeature> getFeatures() {
		return features;
	}

	public int getSizeOfFeatureSpace() {
		int size = 0;
		for (IFeature f : features)
			size += f.size();
		return size;
	}

	public void registerStacked(DocumentReader reader,
			ITrainingExampleExtractor trainingExampleExtractor,
			AbstractResolver[] stage1resolvers) {
		int docCount = 0;
		for (Document d : reader) {
			docCount++;
			AbstractResolver ar1 = stage1resolvers[docCount
					% stage1resolvers.length];
			CorefSolution cs = ar1.resolve(d);
			cs.assignStackMap(d);
			List<PairInstance> is = trainingExampleExtractor.getInstances(d);
			for (IFeature f : features) {
				for (PairInstance pi : is) {
					f.register(pi, d);
				}
			}
		}
		for (IFeature f : features)
			f.freeze();
	}

	public static FeatureSet concat(FeatureSet fs1, FeatureSet fs2) {
		List<IFeature> f = new ArrayList<IFeature>(fs1.features);
		f.addAll(fs2.features);
		return new FeatureSet(f);
	}
	
	public void appendFS(List<IFeature> f) {
		this.features.addAll(f);
	}

	//TODO
	public static List<IFeature> getLoneFS(String lang) {
		List<IFeature> features = new ArrayList<IFeature>();

		IFeature l_lone = new F_loneFeas(lang, 0);
		features.add(l_lone);
//		IFeature l_genra = new Lone_genra(lang, 0);
//		IFeature l_n1 = new Lone_n1(lang, 0);
//		IFeature l_p1 = new Lone_p1(lang, 0);
//		IFeature l_current = new Lone_current(lang, 0);
//		
//		features.add(l_genra);
//		features.add(l_n1);
//		features.add(l_p1);
//		features.add(l_current);
//		
////		IFeature l_xspan = new Lone_xspan(lang, 0);
////		features.add(l_xspan);
//		
//		if(lang.equalsIgnoreCase("eng")) {
//			IFeature l_number = new Lone_number(lang, 0);
//			IFeature l_gender = new Lone_gender(lang, 0);
//			features.add(l_number);
//			features.add(l_gender);
//			
//		} else if(lang.equalsIgnoreCase("chi")) {
//			
//		}
		
		
		for(IFeature fea : features) {
			fea.setLoneFea(true);
			fea.setLang(lang);
		}
		return features;
	}
}
