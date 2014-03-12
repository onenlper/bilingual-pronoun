package zero.coref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.CoNLL.OntoCorefXMLReader;
import util.Common;
import util.Util;
import zero.detect.ZeroUtil;

public class AppendZeroToCoNLL {

	static String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/";

	static ArrayList<String> lineNos = new ArrayList<String>();
	static ArrayList<String> allLins = new ArrayList<String>();
	static ArrayList<String> paraMap = new ArrayList<String>();

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("zero.test.1");
		for (int i = 0; i < lines.size(); i++) {
			currentDoc.clear();
			String line = lines.get(i);
			String ID = line.trim().split("#")[0].trim();
			appendOneDoc(ID, i);
			
			lineNos.add(Integer.toString(currentDoc.size()));
			allLins.addAll(currentDoc);
		}
		Common.outputLines(all, "systemAZP/chiCoNLL.test.1.zero");

		Common.outputLines(lineNos, base + "lineNos");
		Common.outputLines(allLins, base + "/docs/docs.chi");
		Common.outputLines(lines, base + "align/parallelMap");
	}

	static ArrayList<String> all = new ArrayList<String>();

	static ArrayList<String> currentDoc = new ArrayList<String>();
	
	private static void appendOneDoc(String ID, int line) {
		String path = Util.getFullPath(ID, "chi", true);
		CoNLLDocument d = new CoNLLDocument(path);

		// add zeros
		OntoCorefXMLReader.addGoldZeroPronouns(d, false);

		for (CoNLLPart part : d.getParts()) {
			// ArrayList<EntityMention> goldInChainZeroses = ZeroUtil
			// .getAnaphorZeros(part.getChains());

			ArrayList<EntityMention> anaphorZeros = null;
			try {
				anaphorZeros = ZeroUtil.loadClassifiedMention(part, "1");
				HashSet<EntityMention> set = new HashSet<EntityMention>(
						anaphorZeros);
				ArrayList<EntityMention> list = new ArrayList<EntityMention>(set);
				ZeroUtil.assignVNode2(list, part);
				Collections.sort(list);
				for (int i = list.size() - 1; i >= 0; i--) {
					EntityMention zero = list.get(i);
					CoNLLWord zw = new CoNLLWord();
					zw.orig = "*pro*";
					zw.posTag = "-NONE-";
					zw.setPredicateLemma("-");
					zw.setPredicateFramesetID("-");
					zw.setWordSense("-");
					zw.setSpeaker(part.getWord(zero.start).speaker);
					zw.origNamedEntity = "*";
					zw.setRawCoreference("(" + zero.entity.entityIdx + ")");

					// insert to sentence
					CoNLLSentence s = part.getWord(zero.start).sentence;
					int index = part.getWord(zero.start).indexInSentence;
					s.words.add(index, zw);
					ZeroUtil.addEmptyCategoryNode(zero);
				}
			} catch (Exception e) {
				return;
			}
		}
		outputCoNLL(d, "systemAZP/" + line + ".conll");
	}

	private static void outputCoNLL(CoNLLDocument d, String path) {
		ArrayList<String> lines = new ArrayList<String>();
		for (CoNLLPart part : d.getParts()) {
			lines.add(part.label);
		
			for (CoNLLSentence s : part.getCoNLLSentences()) {
				String parseTree = s.syntaxTree.root.getTreeBankStyle(true);
				int from = 0;
				int to = 0;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < s.words.size(); i++) {
					CoNLLWord w = s.words.get(i);
					
					if(w.orig.equals("*pro*")) {
						sb.append("他").append(" ");
					} else {
						sb.append(w.orig).append(" ");
					}
					
					ArrayList<String> tokens = new ArrayList<String>();
					tokens.add(d.getDocumentID());// Document ID
					tokens.add(Integer.toString(part.getPartID()));// Part
																	// number
					tokens.add(Integer.toString(i));// Word number
					tokens.add(w.orig);// Word itself
					tokens.add(w.posTag);// Part-of-Speech
					// tokens.add(w.parseBit);// TODO parse bit

					String key = "(" + w.posTag + " " + w.orig.toLowerCase()
							+ ")";
					to = parseTree.indexOf(key, from);

					if (to == -1) {
						System.out.println(s.syntaxTree.root
								.getTreeBankStyle(true));
						System.out.println(path);
						Common.bangErrorPOS(key + "\n" + parseTree);
					}

					String segTree = parseTree.substring(0, to).trim() + "*";
					int till = parseTree.indexOf("(", to + key.length());
					if (till == -1) {
						till = parseTree.length();
					}
					segTree += parseTree.substring(to + key.length(), till);
					tokens.add(segTree.replaceAll("\\s+", ""));
					parseTree = parseTree.substring(till);

					tokens.add(w.getPredicateLemma());// Predicate lemma
					tokens.add(w.getPredicateFramesetID());// Predicate Frameset
															// ID
					tokens.add(w.getWordSense());// Word sense
					tokens.add(w.getSpeaker());// Speaker Author
					tokens.add(w.origNamedEntity); // named entity
					tokens.add(w.getRawCoreference()); // Coreference

					lines.add(getStr(tokens));
				}
				lines.add("");
				currentDoc.add(sb.toString());
			}
			lines.add("#end document");
		}
		Common.outputLines(lines, path);
		all.addAll(lines);
	}

	private static String getStr(ArrayList<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String s : list) {
			sb.append(s).append("\t");
		}
		return sb.toString().trim();
	}
}
