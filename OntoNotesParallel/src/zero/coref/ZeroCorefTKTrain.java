package zero.coref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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

public class ZeroCorefTKTrain extends ZeroCoref {

	String folder;

	public ZeroCorefTKTrain(String folder) {
		super();
		this.folder = folder;
		fea = new ZeroCorefFeaDepand(true, "zeroCoref" + this.folder);
	}

	public void train() {
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<String> files = Common
				.getLines("zero.train." + folder);
		lines.addAll(getInstances(files));
		Common.outputLines(lines, ZeroUtil.detectBase + "chi" + folder
				+ "/zeroTrain.corefsvm");
		fea.freeze();
	}

	HashMap<String, Integer> chainMap;

	public ArrayList<String> getInstances(ArrayList<String> files) {
		ArrayList<String> instances = new ArrayList<String>();
		for (String file : files) {
			String tokens[] = file.split("#");
			System.out.println(file);
			String docName = tokens[0].trim();
			this.fea.documentMap = DocumentMap.getDocumentMap(docName, "chi");

			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(docName,
					"chi", true));
//			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
//					tokens[1].trim(), "eng", true));
//			chiDoc.setxDoc(engDoc);
			OntoCorefXMLReader.addGoldZeroPronouns(chiDoc, false);
			if (chiDoc.noZeroAnnotation) {
				System.out.println("Skip");
			}
			for (int k = 0; k < chiDoc.getParts().size(); k++) {
				CoNLLPart part = chiDoc.getParts().get(k);
				ArrayList<Entity> goldChains = part.getChains();
				fea.part = part;
				chainMap = ZeroUtil.formChainMap(goldChains);
				fea.chainMap = chainMap;
				ParseTreeMention ptm = new ParseTreeMention();
				ArrayList<EntityMention> npMentions = ptm.getMentions(part);
//				ZeroUtil.attachToMap(npMentions, this.fea.documentMap.chiDoc, part);
				ArrayList<EntityMention> goldInChainZeroses = ZeroUtil
						.getAnaphorZeros(part.getChains());
				Collections.sort(goldInChainZeroses);

				ArrayList<EntityMention> candidates = new ArrayList<EntityMention>();
				candidates.addAll(npMentions);
				// candidates.addAll(goldInChainZeroses);
				Collections.sort(candidates);
//				ZeroUtil.assignVNode(goldInChainZeroses, part);
//				ZeroUtil.assignNPNode(npMentions, part);
				for (EntityMention zero : goldInChainZeroses) {
					buildInstance(instances, part, goldInChainZeroses,
							candidates, zero);
				}
			}
		}
		return instances;
	}

	static ArrayList<String> treeKs = new ArrayList<String>();
	
	private void buildInstance(ArrayList<String> instances, CoNLLPart part,
			ArrayList<EntityMention> goldInChainZeroses,
			ArrayList<EntityMention> candidates, EntityMention zero) {
		if (zero.notInChainZero) {
			Common.bangErrorPOS("NOt happen");
		}
		Entity zeroE = zero.entity;
		Collections.sort(zeroE.mentions);
		EntityMention antecedent = null;
		for (EntityMention m : zeroE.mentions) {
			if (m.end == -1) {
				continue;
			}
			if (m.compareTo(zero) < 0) {
				antecedent = m;
			} else {
				break;
			}
		}
		// printStuff(chiDoc, part, zero);
		for (EntityMention cand : candidates) {
			cand.sentenceID = part.getWord(cand.start).sentence
					.getSentenceIdx();
			if (cand.compareTo(zero) < 0 && cand.compareTo(antecedent) >= 0) {
				fea.set(goldInChainZeroses, candidates, cand, zero, part);
				if (chainMap.containsKey(cand.toName())
						&& chainMap.containsKey(zero.toName())) {
					boolean coref = chainMap.get(cand.toName()).intValue() == chainMap.get(
							zero.toName()).intValue()
							&& cand.end != -1;
					fea.coref = coref;
//					String feaStr = fea.getSVMFormatString();
					zero.sentenceID = part.getWord(zero.start).sentence.getSentenceIdx(); 
					if(zero.sentenceID-cand.sentenceID>4) {
						continue;
					}
//					System.out.println(zero.sentenceID  + " # " + cand.sentenceID);
					String tk = ZeroUtil.getTree(cand, zero, part);
					if(!tk.startsWith(" (")) {
						continue;
					}
					String treeKernel = "|BT|" + tk  + "|ET|";
//					System.out.println(treeKernel);
//					System.out.println(cand.getText(part));
//					System.out.println("-----------------------------");
//					Common.bangErrorPOS("");
					if (chainMap.get(cand.toName()).intValue() == chainMap.get(
							zero.toName()).intValue()
							&& cand.end != -1) {
//						instances.add("+1 " + feaStr);
						treeKs.add("+1 " + treeKernel);
					} else {
//						instances.add("-1 " + feaStr);
						treeKs.add("-1 " + treeKernel);
					}
				}
			}
		}
//		ZeroUtil.addEmptyCategoryNode(zero);

		String zeroSpeaker = part.getWord(zero.start).speaker;
		String candSpeaker = part.getWord(antecedent.start).speaker;

		if (!zeroSpeaker.equals(candSpeaker)) {
			if (antecedent.source.equals("我")) {
				zero.head = "你";
				zero.source = "你";
			} else if (antecedent.source.equals("你")) {
				zero.head = "我";
				zero.source = "我";
			}
		} else {
			zero.source = antecedent.source;
			zero.head = antecedent.head;
		}
	}

	private void printStuff(CoNLLDocument chiDoc, CoNLLPart part,
			EntityMention zero) {
		String conllPath = chiDoc.getFilePath();
		int a = conllPath.indexOf(anno);
		int b = conllPath.indexOf(".");
		String middle = conllPath.substring(a + anno.length(), b);
		String path = prefix + middle + suffix;
		System.out.println("================= " + path);
		System.out.println("================= " + conllPath);
		this.printZero(zero, part);
		System.out.println("----");
	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("java ~ folder");
			System.exit(1);
		}
		ZeroCorefTKTrain train = new ZeroCorefTKTrain(args[0]);
		train.train();
		Common.outputLines(treeKs, "treeKernel.1");
	}
}
