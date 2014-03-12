package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import model.Element;
import model.EntityMention;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.syntaxTree.MyTree;
import model.syntaxTree.MyTreeNode;
import util.Common.Animacy;
import util.Common.Numb;

public class OntoCommon {

	private String language;

	EnDictionary dict;
	
	public EnDictionary getEnDictionary() {
		if (dict == null) {
			dict = new EnDictionary();
		}
		return dict;
	}

	public boolean numberInLaterMention(EntityMention ant, EntityMention mention, CoNLLPart part) {
		Set<String> antecedentWords = new HashSet<String>();
		Set<String> numbers = new HashSet<String>(Arrays.asList(new String[] { "one", "two", "three", "four", "five",
				"six", "seven", "eight", "nine", "ten", "hundred", "thousand", "million", "billion" }));
		for (int i = ant.start; i <= ant.end; i++) {
			antecedentWords.add(part.getWord(i).orig.toLowerCase());
		}
		for (int i = mention.start; i < mention.end; i++) {
			String word = part.getWord(i).orig.toLowerCase();
			try {
				Double.parseDouble(word);
				if (!antecedentWords.contains(word))
					return true;
			} catch (NumberFormatException e) {
				if (numbers.contains(word.toLowerCase()) && !antecedentWords.contains(word))
					return true;
				continue;
			}
		}
		return false;
	}


	public OntoCommon(String language) {
		this.language = language;
	}

	public boolean isSpeaker(EntityMention ant, EntityMention m, CoNLLPart part) {
		String antSpeaker = part.getWord(ant.headStart).speaker;
		String mSpeaker = part.getWord(m.headStart).speaker;
		for (String s : antSpeaker.split("_")) {
			if (m.head.equalsIgnoreCase(s)) {
				for (int i = m.start; i <= m.end; i++) {
					if (part.getWord(i).posTag.equalsIgnoreCase("CC")) {
						return false;
					}
				}
				return true;
			}
		}
		for (String s : mSpeaker.split("_")) {
			if (ant.head.equalsIgnoreCase(s)) {
				for (int i = ant.start; i <= ant.end; i++) {
					if (part.getWord(i).posTag.equalsIgnoreCase("CC")) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	// UTD, which is .....
	public String wordBeforeHead(EntityMention mention, CoNLLPart part) {
		int commaIndex = -1;
		int wIndex = -1;
		for (int i = mention.start; i <= mention.end; i++) {
			if (part.getWord(i).word.equals(",") && commaIndex == -1 && i > mention.headStart) {
				commaIndex = i;
			}
			if (part.getWord(i).posTag.startsWith("W") && wIndex == -1 && i > mention.headStart) {
				wIndex = i;
			}
		}
		StringBuilder sb = new StringBuilder();
		if (commaIndex != -1 && mention.headStart < commaIndex) {
			for (int i = mention.start; i < commaIndex; i++) {
				sb.append(part.getWord(i).word).append(" ");
			}
		}
		if (commaIndex == -1 && wIndex != -1 && mention.headStart < wIndex) {
			for (int i = mention.start; i < wIndex; i++) {
				sb.append(part.getWord(i).word).append(" ");
			}
		}
		return sb.toString();
	}

	public void assignHeadExtent(EntityMention em, CoNLLPart part) {
		ArrayList<CoNLLSentence> sentences = part.getCoNLLSentences();
		if (this.language.equalsIgnoreCase("chinese")) {
			em.headStart = em.end;
			em.headEnd = em.end;
			em.head = part.getWord(em.headStart).orig;
		} else if (this.language.equalsIgnoreCase("english")) {
			int[] position = this.getPosition(em, sentences);
			MyTreeNode node = getNPTreeNode(em, sentences);
			// find English mention's head
			// mention ends with 's
			MyTreeNode headLeaf = node.getHeadLeaf();
			int headStart = sentences.get(position[0]).getWord(headLeaf.leafIdx).index;
			if (headStart < em.start || headStart > em.end) {
				headStart = em.end;
			}
			String head = part.getWord(headStart).orig;
			em.headStart = headStart;
			em.headEnd = headStart;
			em.head = head;
		} else if(this.language.equalsIgnoreCase("arabic")) {
			int[] position = this.getPosition(em, sentences);
			MyTreeNode node = getNPTreeNode(em, sentences);
			// find English mention's head
			// mention ends with 's
			MyTreeNode headLeaf = node.getHeadLeaf();
			int headStart = sentences.get(position[0]).getWord(headLeaf.leafIdx).index;
			if (headStart < em.start || headStart > em.end) {
				headStart = em.end;
			}
			String head = part.getWord(headStart).orig;
			em.headStart = headStart;
			em.headEnd = headStart;
			em.head = head;
		}
	}

	public ArrayList<String> getFileList(String posts[]) {
		ArrayList<String> fileLists = new ArrayList<String>();
		for (String post : posts) {
			ArrayList<String> list = Common.getLines("Onto_" + post);
			for (String str : list) {
				fileLists.add(str);
			}
		}
		return fileLists;
	}

	public String ontoDataPath = "";

	// get all the ontoNotes file, given a postfix
	public ArrayList<String> getOntoFiles(String postfix) {
		ArrayList<String> filenames = new ArrayList<String>();
		String folders[] = { "nw" };
		for (String folder : folders) {
			File subFolders[] = (new File(ontoDataPath + folder)).listFiles();
			for (File subFolder : subFolders) {
				if (subFolder.isDirectory()) {
					File subFolders2[] = subFolder.listFiles();
					for (File subFolder2 : subFolders2) {
						File files[] = subFolder2.listFiles();
						for (File file : files) {
							if (file.getName().endsWith(postfix)) {
								filenames.add(file.getAbsolutePath());
							}
						}
					}
				}
			}
		}
		return filenames;
	}

	public ArrayList<EntityMention> getLongEntityMention(ArrayList<EntityMention> allMentions) {
		Collections.sort(allMentions);
		for (int i = 0; i < allMentions.size() - 1; i++) {
			EntityMention current = allMentions.get(i);
			EntityMention next = allMentions.get(i + 1);
			if (next.headStart > current.headEnd) {
				continue;
			} else {
				int currentLen = current.headEnd - current.headStart;
				int nextLen = next.headEnd - next.headStart;
				if (currentLen < nextLen) {
					allMentions.remove(i);
				} else {
					allMentions.remove(i + 1);
				}
				i--;
			}
		}
		return allMentions;
	}

	public ArrayList<EntityMention> getShortEntityMention(ArrayList<EntityMention> allMentions) {
		Collections.sort(allMentions);
		for (int i = 0; i < allMentions.size() - 1; i++) {
			EntityMention current = allMentions.get(i);
			EntityMention next = allMentions.get(i + 1);

			if (next.headStart > current.headEnd) {
				continue;
			} else {
				int currentLen = current.headEnd - current.headStart;
				int nextLen = next.headEnd - next.headStart;
				if (currentLen > nextLen) {
					allMentions.remove(i);
				} else {
					allMentions.remove(i + 1);
				}
				i--;
			}
		}
		return allMentions;
	}

	public EntityMention formPhrase(MyTreeNode treeNode, CoNLLSentence sentence) {
		ArrayList<MyTreeNode> leaves = treeNode.getLeaves();
		int startIdx = leaves.get(0).leafIdx;
		int endIdx = leaves.get(leaves.size() - 1).leafIdx;
		int start = sentence.getWord(startIdx).index;
		int end = sentence.getWord(endIdx).index;
		StringBuilder sb = new StringBuilder();
		for (int i = startIdx; i <= endIdx; i++) {
			sb.append(sentence.getWord(i).word).append(" ");
		}
		EntityMention em = new EntityMention();
		em.start = start;
		em.end = end;
		em.source = sb.toString().trim();
		return em;
	}

	// public ArrayList<Element>
	// getAllMaximalNounPhrase(ArrayList<CoNLLSentence> prs) {
	// ArrayList<Element> nounPhrases = new ArrayList<Element>();
	// for (CoNLLSentence pr : prs) {
	// Tree tree = pr.tree;
	// TreeNode root = tree.root;
	// ArrayList<TreeNode> frontie = new ArrayList<TreeNode>();
	// frontie.add(root);
	// while (frontie.size() > 0) {
	// TreeNode tn = frontie.remove(0);
	// if (tn.value.toUpperCase().startsWith("NP")) {
	// Element element = formPhrase(tn, pr);
	// if (element != null) {
	// if (element.start == -1) {
	// System.out.println();
	// }
	// nounPhrases.add(element);
	// }
	// } else {
	// ArrayList<TreeNode> tns = tn.children;
	// frontie.addAll(tns);
	// }
	// }
	//
	// }
	// return nounPhrases;
	// }

	public ArrayList<EntityMention> getAllNounPhrase(ArrayList<CoNLLSentence> sentences) {
		ArrayList<EntityMention> nounPhrases = new ArrayList<EntityMention>();
		for (CoNLLSentence sentence : sentences) {
			MyTree tree = sentence.getSyntaxTree();
			MyTreeNode root = tree.root;
			ArrayList<MyTreeNode> frontie = new ArrayList<MyTreeNode>();
			frontie.add(root);
			while (frontie.size() > 0) {
				MyTreeNode tn = frontie.remove(0);
				String value = tn.value.toUpperCase();
				if ((this.language.equalsIgnoreCase("chinese") && (tn.value.toUpperCase().startsWith("NP") || tn.value.toUpperCase().startsWith("QP")))
						|| (this.language.equalsIgnoreCase("english") && (value.startsWith("PRP") || value
								.startsWith("NP")))) {
//					if (!value.equalsIgnoreCase("NP") && !value.equalsIgnoreCase("PRP")
//							&& !value.equalsIgnoreCase("PRP$")) {
//						System.out.println(value);
//					}
					EntityMention element = formPhrase(tn, sentence);
					if (element != null) {
						if (element.start == -1) {
//							System.out.println();
						}
						nounPhrases.add(element);
					}
				}
				ArrayList<MyTreeNode> tns = tn.children;
				frontie.addAll(tns);
			}

		}
		return nounPhrases;
	}

	// create file of file
	public void createFoF(String path, ArrayList<String> files, double curve) {
		try {
			int p = path.lastIndexOf(File.separator);
			String prefix = path.substring(0, p);
			System.out.println(path);
			FileWriter fw = new FileWriter(path);
			ArrayList<String> allLines = new ArrayList<String>();
			for (String file : files) {
				ArrayList<String> lines = Common.getLines(file);
				for (String str : lines) {
					int pos1 = str.lastIndexOf(File.separator);
					allLines.add(prefix + File.separator + str.substring(pos1 + 1) + "_0");
				}
			}
			int curveSize = (int) (curve * allLines.size());
			System.out.println(curveSize);
			for (int i = 0; i < curveSize; i++) {
				String line = allLines.get(i);
				fw.write(line + "\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// one is a child NP in the other's NP constituent
	public boolean isIWithI(EntityMention antecedent, EntityMention em, ArrayList<CoNLLSentence> CoNLLSentences) {
		if ((antecedent.start <= em.start && antecedent.end >= em.end)) {
			return true;
		}
		return false;
		// boolean iWithi = false;
		// MyTreeNode curTreeNode = this.getNewNPTreeNode(em, CoNLLSentences);
		// MyTreeNode canTreeNode = this.getNewNPTreeNode(antecedent,
		// CoNLLSentences);
		// ArrayList<MyTreeNode> curAncestors = curTreeNode.getAncestors();
		// ArrayList<MyTreeNode> canAncestors = canTreeNode.getAncestors();
		//
		// if (curTreeNode == canTreeNode) {
		// return false;
		// }
		// for (MyTreeNode tn : curAncestors) {
		// if (tn == canTreeNode) {
		// iWithi = true;
		// }
		// }
		// for (MyTreeNode tn : canAncestors) {
		// if (tn == curTreeNode) {
		// iWithi = true;
		// }
		// }
		// return iWithi;
	}

	public MyTreeNode getNPTreeNode(EntityMention np, ArrayList<CoNLLSentence> sentences) {
		int position[] = this.getPosition(np, sentences);
		ArrayList<MyTreeNode> leaves = sentences.get(position[0]).getSyntaxTree().leaves;
		MyTreeNode leftNP = leaves.get(position[1]);
		MyTreeNode rightNP = leaves.get(position[2]);
		ArrayList<MyTreeNode> leftAncestors = leftNP.getAncestors();
		ArrayList<MyTreeNode> rightAncestors = rightNP.getAncestors();
		MyTreeNode commonNode = null;
		for (int i = 0; i < leftAncestors.size() && i < rightAncestors.size(); i++) {
			if (leftAncestors.get(i) == rightAncestors.get(i)) {
				commonNode = leftAncestors.get(i);
			} else {
				break;
			}
		}
		return commonNode;
	}

	// the node can not be pos node
	public MyTreeNode getNewNPTreeNode(EntityMention np, ArrayList<CoNLLSentence> sentences) {
		int position[] = this.getPosition(np, sentences);
		ArrayList<MyTreeNode> leaves = sentences.get(position[0]).getSyntaxTree().leaves;
		MyTreeNode leftNP = leaves.get(position[1]);
		MyTreeNode rightNP = leaves.get(position[2]);
		ArrayList<MyTreeNode> leftAncestors = leftNP.getAncestors();
		ArrayList<MyTreeNode> rightAncestors = rightNP.getAncestors();
		MyTreeNode commonNode = null;
		for (int i = 0; i < leftAncestors.size() && i < rightAncestors.size(); i++) {
			if (leftAncestors.get(i) == rightAncestors.get(i) && !leftAncestors.get(i).isPOSNode()) {
				commonNode = leftAncestors.get(i);
			} else {
				break;
			}
		}
		return commonNode;
	}


	public void calChAttribute(EntityMention em, CoNLLPart part) {
		ArrayList<CoNLLSentence> sentences = part.getCoNLLSentences();
		ArrayList<Element> nerElements = part.getNameEntities();
		int position[] = getPosition(em, sentences);
		em.position = position;
		int sentenceIdx = position[0];
		int firstWordIdx = position[1];
		int lastWordIdx = position[2];
		em.sentenceID = sentenceIdx;
		em.startLeaf = firstWordIdx;
		em.endLeaf = lastWordIdx;
		assignHeadExtent(em, part);
		MyTreeNode maxTree = getMaxNPTreeNode(em, sentences);
		MyTreeNode minTree = getMinNPTreeNode(em, sentences);
		MyTreeNode tn = getNPTreeNode(em, sentences);
		em.maxTreeNode = maxTree;
		em.minTreeNode = minTree;
		em.treeNode = tn;
		if (Common.isPronoun(em.getContent())) {
			em.isPronoun = true;
		} else {
			em.isPronoun = false;
		}
		em.PRONOUN_TYPE = Common.getPronounType(em.getHead());
		em.head = em.head.replace("\n", "").replaceAll("\\s+", "");
		em.extent = em.extent.replace("\n", "").replaceAll("\\s+", "");
		em.gender = Common.getGender(em.getContent());

		// if subject
		int pos[] = getPosition(em, sentences);
		CoNLLSentence sentence = sentences.get(pos[0]);
		int k = pos[2] + 1;
		for (; k < sentence.getWordsCount(); k++) {
			String posTag = sentence.getWord(k).getPosTag();
			if (posTag.equals("-NONE-")) {
				continue;
			}
			if (posTag.equals("VV")) {
				em.isSub = true;
				break;
			} else {
				em.isSub = false;
				break;
			}
		}
		int startMaxIdx = maxTree.getLeaves().get(0).leafIdx;
		for (int i = startMaxIdx; i < minTree.getLeaves().get(0).leafIdx; i++) {
			String posTag = sentence.getWord(i).getPosTag();
			if (posTag.equals("NN") || posTag.equals("NR") || posTag.equals("OD") || posTag.equals("JJ")
					|| posTag.equals("NT") || posTag.equals("CD")) {
				em.modifyList.add(sentence.getWord(i).getWord());
			}
		}

		em.isNNP = false;
		if (tn != null) {
			MyTreeNode temp = tn.parent;
			while (temp != sentence.getSyntaxTree().root) {
				if (temp.value.toLowerCase().startsWith("np")) {
					em.isNNP = true;
				}
				temp = temp.parent;
			}
		}
		em.isNNP = false;
		ArrayList<MyTreeNode> ancestors = getMaxNPTreeNode(em, sentences).getAncestors();
		for (int i = 0; i < ancestors.size() - 1; i++) {
			if (ancestors.get(i).value.equalsIgnoreCase("np")) {
				em.isNNP = true;
				break;
			}
		}

		for (Element nerEle : nerElements) {
			if (nerEle.getEnd() == em.getE()) {
				em.ner = nerEle.getContent();
			}
		}

		// Animacy
		if (em.ner.equalsIgnoreCase("PERSON") || (em.PRONOUN_TYPE >= 0 && em.PRONOUN_TYPE <= 3)
				|| (em.PRONOUN_TYPE >= 5 && em.PRONOUN_TYPE <= 8)) {
			em.animacy = Animacy.ANIMATE;
		} else if ((Common.isSemanticAnimal(em.getContent()) < 0 && Common.isSemanticPerson(em.getContent()) < 0)
				|| (!em.ner.equalsIgnoreCase("PERSON") && !em.ner.equalsIgnoreCase("OTHER"))) {
			em.animacy = Animacy.INANIMATE;
		} else {
			em.animacy = Animacy.UNKNOWN;
		}

		// determine whether it is a proper noun
		em.isProperNoun = false;
		String posTag = sentence.getWord(pos[2]).getPosTag();
		if (posTag.toLowerCase().contains("nr")) {
			em.isProperNoun = true;
		}

		if (em.ner.equalsIgnoreCase("person")) {
			em.number = Numb.SINGULAR;
		} else if (em.head.endsWith("们")) {
			em.number = Numb.PLURAL;
		} else {
			em.number = Numb.UNKNOWN;
		}
	}

	public String[] vcArray = { "am", "are", "is", "was", "were", "'m", "'re", "'s", "be" };
	public HashSet<String> vcSet;
	public String[] vcArray2 = { "be", "been", "being" };
	public HashSet<String> vcSet2;

	public boolean isEnglishCopular(EntityMention candidate, EntityMention current,
			ArrayList<CoNLLSentence> coNLLSentences) {
		if (vcSet == null) {
			vcSet = new HashSet<String>();
			vcSet.addAll(Arrays.asList(vcArray));
		}
		if (vcSet2 == null) {
			vcSet2 = new HashSet<String>();
			vcSet2.addAll(Arrays.asList(vcArray2));
		}
		int posCan[] = getPosition(candidate, coNLLSentences);
		int posCur[] = getPosition(current, coNLLSentences);
		if (posCan[0] == posCur[0]) {
			MyTreeNode tn1 = candidate.treeNode;
			MyTreeNode tn2 = current.treeNode;
			if (tn2.parent.parent == tn1.parent) {
				MyTreeNode VP = tn2.parent;
				if (VP.value.equalsIgnoreCase("VP")) {
					MyTreeNode VBZ = VP.children.get(0);
					if (VBZ.value.startsWith("VB")) {
						String vcword = VBZ.children.get(0).value;
						if (vcSet.contains(vcword.toLowerCase())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean isCopular2(EntityMention candidate, EntityMention current, ArrayList<CoNLLSentence> coNLLSentences) {
		int posCan[] = getPosition(candidate, coNLLSentences);
		int posCur[] = getPosition(current, coNLLSentences);
		// 29 heuristic COPULAR
		if (posCan[0] == posCur[0]) {
			CoNLLSentence sentence = coNLLSentences.get(posCan[0]);
			int k = posCan[2] + 1;
			for (; k < posCur[1]; k++) {
				String pos = sentence.getWord(k).getPosTag();
				if (!pos.equals("PU") && !pos.equals("-NONE-")) {
					break;
				}
			}
			if (k < sentence.getWordsCount() && sentence.getWord(k).getPosTag().equals("VC")) {
				MyTreeNode VC = sentence.getSyntaxTree().leaves.get(k);
				MyTreeNode parent = VC.parent.parent;
				int childIdx;
				for (childIdx = 0; childIdx < parent.children.size(); childIdx++) {
					if (parent.children.get(childIdx) == VC.parent) {
						break;
					}
				}
				if (childIdx + 1 < parent.children.size()) {
					MyTreeNode next = parent.children.get(childIdx + 1);
					if (next.value.startsWith("NP")) {
						MyTreeNode lastChild = next;
						while (lastChild.children.size() != 0) {
							lastChild = lastChild.children.get(lastChild.children.size() - 1);
						}
						if (lastChild.leafIdx == posCur[2]) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	// public boolean isEnglishCopular(EntityMention candidate, EntityMention
	// current, CoNLLPart part) {
	// if (candidate.treeNode.parent == current.treeNode.parent) {
	//			
	// }
	// }

	public boolean isEnglishAppositive(EntityMention candidate, EntityMention mention, CoNLLPart part) {
		if (candidate.end == mention.end) {
			return false;
		}
		if (mention.end + 1 < part.getWordCount() && part.getWord(mention.end + 1).posTag.equalsIgnoreCase("CC")) {
			return false;
		}

		int childIndex1 = candidate.treeNode.childIndex;
		int childIndex2 = mention.treeNode.childIndex;
		if (candidate.original.equalsIgnoreCase("Mt. Ali")
				&& mention.original
						.equalsIgnoreCase("the only single - species forest of hinoki cypress surviving in the world today")) {
			// System.out.println(candidate.treeNode.children.get(0).value);
			// System.out.println(candidate.treeNode.parent ==
			// mention.treeNode.parent &&
			// mention.treeNode.parent.value.startsWith("NP"));
			// System.out.println(childIndex1);
			// System.out.println(childIndex2);
			// System.out.println(candidate.ner);
		}
		if (candidate.treeNode.parent == mention.treeNode.parent && mention.treeNode.parent.value.startsWith("NP")) {
			MyTreeNode parent = candidate.treeNode.parent;
			boolean haveCC = false;
			for (MyTreeNode tn : parent.children) {
				if (tn.value.equals("CC")) {
					haveCC = true;
				}
			}
			if (haveCC) {
				return false;
			}
			if (parent.children.size() > childIndex1 + 1
					&& parent.children.get(childIndex1 + 1).value.equals(",")
					&& childIndex1 + 2 == childIndex2
					&& (candidate.ner.equalsIgnoreCase("PERSON") || candidate.ner.equalsIgnoreCase("LOC")
							|| candidate.ner.equalsIgnoreCase("GPE") || candidate.ner.equalsIgnoreCase("ORG"))
					&& !mention.ner.equalsIgnoreCase("PERSON")) {
				// System.out.println(candidate.source + " " + candidate.start +
				// " " + candidate.end + " #1 "
				// + mention.source + " " + mention.start + " " + mention.end);
				return true;
			}
			if (parent.children.size() == 2 && (mention.ner.equalsIgnoreCase("PERSON"))) {
				// System.out.println(candidate.source + " " + candidate.start +
				// " " + candidate.end + " #2 "
				// + mention.source + " " + mention.start + " " + mention.end);
				return true;
			}
			if (parent.children.size() == 4 && childIndex1 == 0
					&& parent.children.get(childIndex1 + 1).value.equalsIgnoreCase("-LRB-") && childIndex2 == 2
					&& parent.children.get(childIndex2 + 1).value.equalsIgnoreCase("-RRB-")) {
				// System.out.println(candidate.source + " " + candidate.start +
				// " " + candidate.end + " #3 "
				// + mention.source + " " + mention.start + " " + mention.end);
				return true;
			}

		}
		return false;
	}

	public boolean isRoleAppositive(EntityMention can, EntityMention cur, ArrayList<CoNLLSentence> sentences) {
		if (can.end + 1 == cur.start && cur.ner.equalsIgnoreCase("PERSON")) {
			return true;
		} else if (cur.ner.equalsIgnoreCase("PERSON")) {
			return true;
		} else {
			return false;
		}
	}

	/** get position of sentenceIdx, wordStartIdx and wordEndIdx */
	public int[] getPosition(EntityMention em, ArrayList<CoNLLSentence> sentences) {
		int sentenceID = 0;
		CoNLLSentence sentence = null;
		for (int i = 0; i < sentences.size(); i++) {
			sentence = sentences.get(i);
			if (em.start >= sentence.getStartWordIdx() && em.end <= sentence.getEndWordIdx()) {
				sentenceID = i;
				break;
			}
		}
		int position[] = new int[3];
		position[0] = sentenceID;
		position[1] = em.start - sentence.getStartWordIdx();
		position[2] = em.end - sentence.getStartWordIdx();
		return position;
	}

	public boolean isMaximalNP(EntityMention can, EntityMention cur, ArrayList<CoNLLSentence> coNLLSentences) {
		if (can.isNNP && cur.isNNP) {
			// if(can.start==145 && can.end==146 && can.extent.equals("中国")) {
			// System.out.println();
			// }
			int[] position1 = getPosition(can, coNLLSentences);
			int[] position2 = getPosition(cur, coNLLSentences);
			if (position1[0] != position2[0]) {
				return false;
			}
			int canNPIdx = position1[1];
			int curNPIdx = position2[1];
			int sentenceIdx = position1[0];
			MyTree tree = coNLLSentences.get(sentenceIdx).getSyntaxTree();
			MyTreeNode leaf1 = null;
			try {
				leaf1 = tree.leaves.get(canNPIdx);
			} catch (Exception e) {
				System.out.println(can.toString());
				// System.exit(1);
			}
			StringBuilder sb = new StringBuilder();
			for (String modify : can.modifyList) {
				sb.append(modify);
			}
			// System.out.println(can + "#" + can.source+"#"+can.extent +
			// "#"+sb.toString());
			MyTreeNode leaf2 = tree.leaves.get(curNPIdx);
			MyTreeNode maxNP1 = leaf1;
			MyTreeNode parent1 = leaf1.parent;
			while (parent1 != tree.root) {
				if (parent1.value.equals("NP")) {
					maxNP1 = parent1;
				}
				parent1 = parent1.parent;
			}

			MyTreeNode maxNP2 = leaf2;
			MyTreeNode parent2 = leaf2.parent;
			while (parent2 != tree.root) {
				if (parent2.value.equals("NP")) {
					maxNP2 = parent2;
				}
				parent2 = parent2.parent;
			}
			if (maxNP1 == maxNP2) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void main(String args[]) {
		// Common.init();
		// ArrayList<String> files = Common.getLines("ACE_0");
		// String crfFile = "D:\\ACL12\\model\\ACE\\semantic\\FAC\\result";
		// ArrayList<ArrayList<Element>> elementses =
		// CorefACECommon.getSemanticsFromCRFFile(files, crfFile);
		// for(ArrayList<Element> elements : elementses) {
		// for(Element el:elements) {
		// System.out.println(el.start + " " + el.end + " " + el.content + " " +
		// el.confidence);
		// }
		// }
	}

	public MyTreeNode getMaxNPTreeNode(EntityMention np, ArrayList<CoNLLSentence> sentences) {
		int sentenceIdx = 0;
		int lastWordIdx = 0;
		int position[] = getPosition(np, sentences);
		sentenceIdx = position[0];
		lastWordIdx = position[2];
		ArrayList<MyTreeNode> leaves = sentences.get(sentenceIdx).getSyntaxTree().leaves;
		MyTreeNode rightNp = leaves.get(lastWordIdx);
		ArrayList<MyTreeNode> rightAncestors = rightNp.getAncestors();
		MyTreeNode NP = null;
		for (int i = rightAncestors.size() - 1; i >= 0; i--) {
			MyTreeNode tmp = rightAncestors.get(i);
			ArrayList<MyTreeNode> tmpLeaves = tmp.getLeaves();
			if ((tmp.value.toLowerCase().startsWith("np") || tmp.value.toLowerCase().startsWith("qp"))
					&& tmpLeaves.get(tmpLeaves.size() - 1).leafIdx == lastWordIdx) {
				NP = tmp;
			}
		}
		if (NP == null) {
			NP = rightNp.parent;
		}
		return NP;
	}

	public MyTreeNode getMinNPTreeNode(EntityMention np, ArrayList<CoNLLSentence> sentences) {
		int position[] = getPosition(np, sentences);
		int sentenceIdx = position[0];
		int endLeaf = position[2];
		ArrayList<MyTreeNode> leaves = sentences.get(sentenceIdx).getSyntaxTree().leaves;
		MyTreeNode rightNp = leaves.get(endLeaf);
		// System.out.println(npWordEndIdx +np.getContent());
		ArrayList<MyTreeNode> rightAncestors = rightNp.getAncestors();
		MyTreeNode NP = null;
		for (int i = 0; i < rightAncestors.size(); i++) {
			MyTreeNode tmp = rightAncestors.get(i);
			ArrayList<MyTreeNode> tmpLeaves = tmp.getLeaves();
			if ((tmp.value.toLowerCase().startsWith("np") || tmp.value.toLowerCase().startsWith("qp"))
					&& tmpLeaves.get(tmpLeaves.size() - 1).leafIdx == np.endLeaf) {
				NP = tmp;
			}
		}
		if (NP == null) {
			NP = rightNp.parent;
		}
		return NP;
	}

	public String getSemanticSymbol(EntityMention em, String head) {
		if (head.charAt(0) == '副') {
			head = head.substring(1);
		}
		if (!em.head.endsWith(head)) {
			head = em.head;
			// System.out.println(head + " " + em.head + "######");
		} else {

		}
		String semantics[] = Common.getSemanticDic().get(head);
		String semantic = "";
		if (semantics != null) {
			semantic = semantics[0];
		} else {
			boolean findNer = false;
			if (em.ner.equalsIgnoreCase("PERSON")) {
				semantic = "A0000001";
			} else if (em.ner.equalsIgnoreCase("LOC")) {
				semantic = "Be000000";
			} else if (em.ner.equalsIgnoreCase("GPE")) {
				semantic = "Di020000";
			} else if (em.ner.equalsIgnoreCase("ORG")) {
				semantic = "Dm000000";
			} else {
				// System.out.println(ele.content + " " + em.head);
			}
			findNer = true;
			if (!findNer) {
				if (head.endsWith("们") || head.endsWith("人") || head.endsWith("者") || head.endsWith("哥")
						|| head.endsWith("员") || head.endsWith("弟") || head.endsWith("爸")) {
					semantic = "A0000001";
				}
			}
		}
		return semantic;
	}



}
