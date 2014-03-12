package zero.coref;

import java.io.File;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import jnisvmlight.SVMLightModel;
import mentionDetect.ParseTreeMention;
import model.Entity;
import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.OntoCorefXMLReader;
import util.Common;
import util.Util;
import zero.detect.ZeroUtil;
import align.DocumentMap;

public class ZeroCorefTest extends ZeroCoref {

	String folder;
	SVMLightModel model;

	public ZeroCorefTest(String folder) {
		super();
		this.folder = folder;
		fea = new ZeroCorefFeaDepand(false, "zeroCoref" + folder);
		try {
			this.model = SVMLightModel
					.readSVMLightModelFromURL(new java.io.File(
							"/users/yzcchen/tool/svmlight_6.0.1/zero/coref."
									+ folder).toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		DocumentMap
		.loadRealBAAlignResult("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/");

//	DocumentMap
//	.loadRealBAAlignResult("/users/yzcchen/chen3/ijcnlp2013/googleMTRED/chi_MT/align/");
	}

	public void test() {
		// Common.outputLines(this.getTestInstances(), "zeroCorefTrain." +
		// this.folder);
	}

	double good = 0;
	double bad = 0;
	
	public void getInstances() {
		ArrayList<ArrayList<EntityMention>> corefResults = new ArrayList<ArrayList<EntityMention>>();
		ArrayList<ArrayList<Entity>> goldEntities = new ArrayList<ArrayList<Entity>>();
		ArrayList<String> files = Common.getLines("zero.test." + folder);
		ArrayList<String> paraGlobal = Common.getLines("parallelMap");
		int fk = 0;
		HashSet<String> skip = new HashSet<String>();
		for (String file : files) {
			String tokens[] = file.split("#");

			System.out.println(file);

			String docName = tokens[0].trim();
			this.fea.documentMap = DocumentMap.getDocumentMap(docName, "chi");

			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(docName,
					"chi", true));
			
			String fold = "";
			fold = "eng_gold_zero";
//			fold = "eng_sys_zero";
//			fold = "eng_mt_zero";
			int id = files.indexOf(file);
			ZeroUtil.id = id;
			String path = fold + File.separator + id + ".conll";
			
			CoNLLDocument engDoc = new CoNLLDocument(path);
			
//			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
//					tokens[1].trim(), "eng", true));
			
			chiDoc.setxDoc(engDoc);
			
			ZeroUtil.check(chiDoc, engDoc, this.fea.documentMap);
			OntoCorefXMLReader.addGoldZeroPronouns(chiDoc, false);
			if (!ZeroUtil.hasZeroAnno(chiDoc)) {
				skip.add(docName);
				System.out.println("skip");
				continue;
			}
			fk++;
			for (int k = 0; k < chiDoc.getParts().size(); k++) {
				CoNLLPart part = chiDoc.getParts().get(k);
				ArrayList<Entity> goldChains = part.getChains();

				goldEntities.add(goldChains);

				fea.part = part;
				HashMap<String, Integer> chainMap = formChainMap(goldChains);
				fea.chainMap = chainMap;

				ArrayList<EntityMention> corefResult = new ArrayList<EntityMention>();
				corefResults.add(corefResult);

				ParseTreeMention ptm = new ParseTreeMention();
				ArrayList<EntityMention> npMentions = ptm.getMentions(part);
				ZeroUtil.attachToMap(npMentions, this.fea.documentMap.chiDoc, part);
				Collections.sort(npMentions);

				ArrayList<EntityMention> anaphorZeros = null;
				anaphorZeros = ZeroUtil
						.loadClassifiedMention(part, this.folder);

				anaphorZeros = ZeroUtil.getAnaphorZeros(goldChains);

				ArrayList<EntityMention> candidates = new ArrayList<EntityMention>();
				candidates.addAll(npMentions);

				// if (!file.contains("/nw/") && !file.contains("/mz/")
				// && !file.contains("/wb/")) {
//				candidates.addAll(anaphorZeros);
				// }
				Collections.sort(candidates);

				Collections.sort(anaphorZeros);
				ZeroUtil.assignVNode(anaphorZeros, part);
				ZeroUtil.assignNPNode(npMentions, part);

				findAntecedent(file, part, chainMap, corefResult, anaphorZeros,
						candidates);
			}
		}

		System.out.println("Good: " + good);
		System.out.println("Bad: " + bad);
		System.out.println("Precission: " + good / (good + bad) * 100);

		evaluate(corefResults, goldEntities);
		System.out.println(fk);
		System.out.println(skip);
	}

	private void findAntecedent(String file, CoNLLPart part,
			HashMap<String, Integer> chainMap,
			ArrayList<EntityMention> corefResult,
			ArrayList<EntityMention> anaphorZeros,
			ArrayList<EntityMention> candidates) {
		for (EntityMention zero : anaphorZeros) {
			EntityMention antecedent = null;
			double val = -1000;
			double threshold = -1110;
			for (int h = candidates.size() - 1; h >= 0; h--) {
				EntityMention cand = candidates.get(h);
				cand.sentenceID = part.getWord(cand.start).sentence
						.getSentenceIdx();
				if (cand.compareTo(zero) < 0 && cand.end<zero.start
//						&& zero.sentenceID-cand.sentenceID<5
						) {
					fea.set(anaphorZeros, candidates, cand, zero, part);
					
					boolean coref = chainMap.containsKey(cand.toName())
							&& chainMap.containsKey(zero.toName()) && chainMap.get(cand.toName()).intValue() == chainMap.get(
							zero.toName()).intValue()
							&& cand.end != -1;
					fea.coref = coref;
					
					// isCoref(chainMap, zero, cand);
					String feaStr = fea.getSVMFormatString();
					feaStr = "-1 " + feaStr;
					double d = model
							.classify(Common.SVMStringToFeature(feaStr));
					
					if(fea.rules()) {
						System.out.println(d +  " " + coref);
						double th2 = -1;
						if(d>th2) 
							d = 1000;
//						Common.bangErrorPOS("");
					}
					
					if (d > threshold && d > val) {
						antecedent = cand;
						val = d;
//						break;
					}
				}
			}
			if (antecedent != null) {
				if (antecedent.end != -1) {
					zero.antecedent = antecedent;
				} else {
					zero.antecedent = antecedent.antecedent;
				}
			}
			if (zero.antecedent != null
					&& zero.antecedent.end != -1
					&& chainMap.containsKey(zero.toName())
					&& chainMap.containsKey(zero.antecedent.toName())
					&& chainMap.get(zero.toName()).intValue() == chainMap.get(
							zero.antecedent.toName()).intValue()) {
				good++;
			} else {
				bad++;
			}
			ZeroUtil.addEmptyCategoryNode(zero);

			if (antecedent != null) {
				zero.source = antecedent.source;
				zero.head = antecedent.head;
			}
		}
		for (EntityMention zero : anaphorZeros) {
			if (zero.antecedent != null) {
				corefResult.add(zero);
			}
		}
	}

	private void isCoref(HashMap<String, Integer> chainMap, EntityMention zero,
			EntityMention cand) {
		boolean coref = chainMap.containsKey(zero.toName())
				&& chainMap.containsKey(cand.toName())
				&& chainMap.get(zero.toName()).intValue() == chainMap.get(
						cand.toName()).intValue();
	}

	public static void evaluate(ArrayList<ArrayList<EntityMention>> zeroses,
			ArrayList<ArrayList<Entity>> entitieses) {
		double gold = 0;
		double system = 0;
		double hit = 0;

		for (int i = 0; i < zeroses.size(); i++) {
			ArrayList<EntityMention> zeros = zeroses.get(i);
			ArrayList<Entity> entities = entitieses.get(i);
			ArrayList<EntityMention> goldInChainZeroses = ZeroUtil
					.getAnaphorZeros(entities);
			HashMap<String, Integer> chainMap = formChainMap(entities);
			gold += goldInChainZeroses.size();
			system += zeros.size();
			for (EntityMention zero : zeros) {
				EntityMention ant = zero.antecedent;
				Integer zID = chainMap.get(zero.toName());
				Integer aID = chainMap.get(ant.toName());
				if (zID != null && aID != null
						&& zID.intValue() == aID.intValue()) {
					hit++;
				}
			}
		}

		double r = hit / gold;
		double p = hit / system;
		double f = 2 * r * p / (r + p);
		System.out.println("============");
		System.out.println("Hit: " + hit);
		System.out.println("Gold: " + gold);
		System.out.println("System: " + system);
		System.out.println("============");
		System.out.println("Recall: " + r * 100);
		System.out.println("Precision: " + p * 100);
		System.out.println("F-score: " + f * 100);
	}

	private static HashMap<String, Integer> formChainMap(
			ArrayList<Entity> entities) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < entities.size(); i++) {
			for (EntityMention m : entities.get(i).mentions) {
				map.put(m.toName(), i);
			}
		}
		return map;
	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("java ~ folder");
			System.exit(1);
		}
		ZeroCorefTest test = new ZeroCorefTest(args[0]);
		test.getInstances();
	}
}
