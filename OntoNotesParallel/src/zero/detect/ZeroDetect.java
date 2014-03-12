package zero.detect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import align.DocumentMap;

import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.OntoCorefXMLReader;
import model.syntaxTree.MyTree;
import model.syntaxTree.MyTreeNode;
import util.Common;
import util.Util;

public class ZeroDetect {

	ZeroDetectFea detectFea;

	String folder;

	boolean train;
	
	static int zps = 0;
	static int docs = 0;
	static int azps = 0;

	public ZeroDetect(boolean train, String folder) {
		this.train = train;
		this.folder = folder;
		detectFea = new ZeroDetectFea(train, "zeroDetect." + folder);
		DocumentMap
				.loadRealBAAlignResult("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/");
	}

	public ArrayList<EntityMention> getHeuristicZeros(CoNLLPart part) {
		ArrayList<EntityMention> mentions = new ArrayList<EntityMention>();

		for (CoNLLSentence s : part.getCoNLLSentences()) {
			HashSet<Integer> candidates = new HashSet<Integer>();

			MyTree tree = s.syntaxTree;
			MyTreeNode root = tree.root;
			this.visitTreeNode(root, candidates, s);

			for (Integer can : candidates) {
				EntityMention m = new EntityMention();
				m.start = can;
				m.end = -1;
				m.sentenceID = s.getSentenceIdx();

				mentions.add(m);
			}
		}

		Collections.sort(mentions);
		return mentions;
	}

	private void visitTreeNode(MyTreeNode node, HashSet<Integer> zeros,
			CoNLLSentence s) {
		if (node.value.equalsIgnoreCase("VP")) {
			HashSet<String> filted = new HashSet<String>(Arrays.asList("ADVP"));

			boolean CC = false;
			// if in CC construct
			for (MyTreeNode temp : node.parent.children) {
				if (temp.value.equalsIgnoreCase("CC")
						&& temp.getFirstXAncestor("VP") != null) {
					CC = true;
				}
			}

			boolean advp = false;
			ArrayList<MyTreeNode> leftSisters = node.getLeftSisters();
			for (int k = leftSisters.size() - 1; k >= 0; k--) {
				MyTreeNode leftSister = leftSisters.get(k);
				if (!filted.contains(leftSister.value)) {
					break;
				}
				if (filted.contains(leftSister.value)) {
					if (leftSister.parent.value.equals("VP")) {
						advp = true;
						break;
					}
				}
			}

			if (leftSisters.size() > 0) {
				for (MyTreeNode leftSister : leftSisters) {
					if (leftSister.value.startsWith("NP")) {
						 advp = true;
						EntityMention m = new EntityMention();
						m.start = s.getWord(node.getLeaves().get(0).leafIdx).index;
						m.end = -1;
					}
				}
			}

			EntityMention m = new EntityMention();
			m.start = s.getWord(node.getLeaves().get(0).leafIdx).index;
			m.end = -1;

			if (!CC && !advp) {
				int leafIdx = node.getLeaves().get(0).leafIdx;
				zeros.add(s.getWord(leafIdx).index);
			}
		}
		for (MyTreeNode child : node.children) {
			this.visitTreeNode(child, zeros, s);
		}
	}

	public void generateInstances() {
		String folder = this.folder;
		ArrayList<String> files = null;

		if (train) {
			files = Common.getLines("zero.train." + folder);
		} else {
			files = Common.getLines("zero.test." + folder);
		}

		ArrayList<String> allLines = new ArrayList<String>();

		for (String file : files) {
			String tokens[] = file.split("#");

			System.out.println(file);
			String docName = tokens[0].trim();
			
			if(!train) {
				this.detectFea.documentMap = DocumentMap.getDocumentMap(docName,
					"chi");
			}
			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(docName,
					"chi", train));

			if(!train) {
				CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
					tokens[1].trim(), "eng", true));
				chiDoc.setxDoc(engDoc);
			}
			ArrayList<String> lines = processOneFile(chiDoc);
			allLines.addAll(lines);
			
		}
		if (train) {
			Common.outputLines(allLines, ZeroUtil.detectBase + "chi" + folder + "/zeroTrain.detectsvm");
			this.detectFea.freeze();
		} else {
			Common.outputLines(allLines, ZeroUtil.detectBase + "chi" + folder + "/zeroTest.detectsvm");
		}

	}

	public ArrayList<String> processOneFile(CoNLLDocument document) {

		ArrayList<String> lines = new ArrayList<String>();
		OntoCorefXMLReader.addGoldZeroPronouns(document, false);

		if (document.noZeroAnnotation) {
			System.out.println("Skip");
			return lines;
		}
		
		docs++;

		for (int i = 0; i < document.getParts().size(); i++) {
			CoNLLPart part = document.getParts().get(i);
			detectFea.part = part;
			ArrayList<EntityMention> goldZPs = ZeroUtil.getGoldZeros(part
					.getChains());
			ArrayList<EntityMention> goldAZP = ZeroUtil
					.getAnaphorZeros(part.getChains());
			
			zps += goldZPs.size();
			azps+= goldAZP.size();
			
			System.out.println(goldAZP.size());
			HashSet<EntityMention> goldZeros = new HashSet<EntityMention>(
					goldAZP);
			
			
			lines.addAll(processOnePart(part, goldZeros));
			
		}

		return lines;
	}

	double positive = 0;
	double negative = 0;

	public ArrayList<String> processOnePart(CoNLLPart part,
			HashSet<EntityMention> goldZeros) {
		ArrayList<EntityMention> herusiticZeros = getHeuristicZeros(part);
		HashSet<EntityMention> goldZPs = new HashSet<EntityMention>(
				ZeroUtil.getGoldZeros(part.getChains()));
		ArrayList<String> output = new ArrayList<String>();
		ArrayList<String> zeroStrs = new ArrayList<String>();
		for (EntityMention zero : herusiticZeros) {
			// if(!goldZPs.contains(zero)) {
			// continue;
			// }
			ZeroUtil.assignVNode(zero, part);
			StringBuilder sb = new StringBuilder();
			if (goldZeros != null && goldZeros.contains(zero)) {
				positive++;
				sb.append("+1 ");
				this.detectFea.positive = true;
			} else {
				negative++;
				sb.append("-1 ");
				this.detectFea.positive = false;
			}
			sb.append(getZeroDetectFea(zero));
			output.add(sb.toString().trim());
			zeroStrs.add(zero.toName());
		}
		if(!train) {
			String stem = part.getPartName().replace(File.separator, "-");
			Common.outputLines(output, ZeroUtil.detectBase + "chi" + folder + "/test/" + stem + ".detectsvm");
			Common.outputLines(zeroStrs, ZeroUtil.detectBase + "chi" + folder + "/test/" + stem + ".detectzeros");
		}
		return output;
	}

	public String getZeroDetectFea(EntityMention zero) {
		detectFea.zeroDetect = this;
		detectFea.zero = zero;
		return this.detectFea.getSVMFormatString();
	}

}
