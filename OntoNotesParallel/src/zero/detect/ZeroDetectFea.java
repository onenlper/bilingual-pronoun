package zero.detect;

import java.util.ArrayList;
import java.util.HashMap;

import model.EntityMention;
import model.SemanticRole;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTree;
import model.syntaxTree.MyTreeNode;
import util.Common;
import util.Common.Feature;
import util.YYFeature;
import align.DocumentMap;
import align.DocumentMap.Unit;

public class ZeroDetectFea extends YYFeature {

	EntityMention zero;
	CoNLLPart part;

	DocumentMap documentMap;

	HashMap<String, Integer> trans;
	HashMap<String, Integer> intrans;

	ZeroDetect zeroDetect;

	boolean positive = false;

	public ZeroDetectFea(boolean train, String name) {
		super(train, name);
		this.trans = Common.readFile2Map("trans.verb");
		this.intrans = Common.readFile2Map("intrans.verb");

	}

	public int get(HashMap<String, Integer> map, String key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return 0;
	}

	public void set(EntityMention m, CoNLLPart part) {
		this.zero = m;
		this.part = part;
	}

	public ArrayList<Feature> lexicalFeatures() {
		ArrayList<Feature> features = new ArrayList<Feature>();
		// if NN
		String word = part.getWord(zero.start).word;
		if (part.getWord(zero.start).posTag.equalsIgnoreCase("NN")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		if (zero.start != 0
				&& part.getWord(zero.start - 1).posTag.equalsIgnoreCase("NN")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		if (zero.start < part.getWordCount() - 1
				&& !part.getWord(zero.start + 1).posTag.equalsIgnoreCase("NN")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		// if transitive verb
		// TODO
		boolean trans = false;
		if (this.trans.containsKey(word)) {
			if (!this.intrans.containsKey(word)) {
				trans = true;
			}
			if (this.intrans.containsKey(word)
					&& this.trans.get(word).intValue() > 3 * this.intrans
							.get(word)) {
				trans = true;
			}
		}
		if (trans) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// if proposition
		if (part.getWord(zero.start).posTag.equalsIgnoreCase("PP")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		return features;
	}

	public ArrayList<Feature> consistituentFeature() {
		ArrayList<Feature> features = new ArrayList<Feature>();
		CoNLLWord word = part.getWord(zero.start);
		MyTreeNode node = part.getCoNLLSentences().get(zero.sentenceID).syntaxTree.leaves
				.get(word.indexInSentence);
		ArrayList<MyTreeNode> ancestors = node.getAncestors();
		MyTreeNode clause = part.getCoNLLSentences().get(zero.sentenceID).syntaxTree.root;
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			if (ancestors.get(i).value.equalsIgnoreCase("ip")) {
				clause = ancestors.get(i);
				break;
			}
		}

		// current word is the first child of the clause
		if (node == clause.getLeaves().get(0)) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// the first word is verb
		if (clause.getLeaves().get(0).parent.value.equalsIgnoreCase("VV")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// whether the current word acts as subject/object
		ArrayList<MyTreeNode> subjects = new ArrayList<MyTreeNode>();
		ArrayList<MyTreeNode> objects = new ArrayList<MyTreeNode>();

		// part.semanticRoles;

		ArrayList<MyTreeNode> children = clause.children;
		for (MyTreeNode child : children) {
			if (child.value.startsWith("NP")) {
				subjects.addAll(child.getLeaves());
			}
			if (child.value.startsWith("VP")) {

				break;
			}
		}

		if (true) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		return features;
	}

	// get AZP features
	public ArrayList<Feature> getAZPFeature() {
		ArrayList<Feature> features = new ArrayList<Feature>();

		int indexInSentence = part.getWord(zero.start).indexInSentence;

		MyTree tree = part.getCoNLLSentences().get(zero.getSentenceID()).syntaxTree;
		MyTreeNode root = tree.root;
		// 0. first gap in the sentence
		boolean firstGap = indexInSentence == 0;
		if (firstGap) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		int rightIdx = indexInSentence;
		MyTreeNode Wr = tree.leaves.get(rightIdx);
		ArrayList<MyTreeNode> WrAncestors = Wr.getAncestors();
		// 1. Pl_Is_NP
		// 2. Pr_Is_VP
		// 3. Pl_IS_NP && Pr_IS_VP
		// 4. P_IS_VP
		MyTreeNode C;
		MyTreeNode Wl = null;
		if (firstGap) {
			features.add(new Feature(0, 1, 3));
			features.add(new Feature(0, 1, 3));
			features.add(new Feature(0, 1, 3));
			features.add(new Feature(0, 1, 3));
			C = root;
		} else {
			int leftIdx = rightIdx - 1;
			Wl = tree.leaves.get(leftIdx);
			ArrayList<MyTreeNode> WlAncestors = Wl.getAncestors();
			int m = 0;
			MyTreeNode P = root;
			MyTreeNode Pl = WlAncestors.get(m);
			MyTreeNode Pr = WrAncestors.get(m);
			m++;
			while (true) {
				if (Pl == Pr) {
					P = Pl;
					m++;
					Pl = WlAncestors.get(m);
					Pr = WrAncestors.get(m);
				} else {
					break;
				}
			}
			// 1. Pl_Is_NP
			if (Pl.value.toLowerCase().startsWith("np")) {
				features.add(new Feature(1, 1, 3));
			} else {
				features.add(new Feature(2, 1, 3));
			}
			// 2. Pr_Is_VP
			if (Pr.value.toLowerCase().startsWith("vp")) {
				features.add(new Feature(1, 1, 3));
			} else {
				features.add(new Feature(2, 1, 3));
			}
			// 3. Pl_IS_NP && Pr_IS_VP
			if (Pl.value.toLowerCase().startsWith("np")
					&& Pr.value.toLowerCase().startsWith("vp")) {
				features.add(new Feature(1, 1, 3));
			} else {
				features.add(new Feature(2, 1, 3));
			}
			// 4. P_IS_VP
			if (P.value.toLowerCase().startsWith("vp")) {
				features.add(new Feature(1, 1, 3));
			} else {
				features.add(new Feature(2, 1, 3));
			}
			C = P;
		}
		MyTreeNode temp = Wr;
		boolean find_IP_VP = false;
		// 5. IP_VP
		while (temp != C) {
			if (temp.value.toLowerCase().startsWith("vp")
					&& temp.parent.value.toLowerCase().startsWith("ip")) {
				find_IP_VP = true;
				break;
			}
			temp = temp.parent;
		}
		if (find_IP_VP) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		MyTreeNode V = null;
		for (MyTreeNode node : WrAncestors) {
			if (node.value.toLowerCase().startsWith("vp")
					&& node.getLeaves().get(0) == Wr) {
				V = node;
			}
		}
		// 6. Has_Ancestor_NP
		// 7. Has_Ancestor_VP
		// 8. Has_Ancestor_CP
		if (V == null) {
			// System.err.println(plainText.content.substring(position-2,
			// position+2));
			System.err.println("SPECIAL CASE. NO VP on the right");
		} else {
			boolean has_Ancestor_NP = false;
			boolean has_Ancestor_VP = false;
			boolean has_Ancestor_CP = false;
			temp = V;
			while (temp != root) {
				// try {
				if (temp.value.toLowerCase().startsWith("np")) {
					has_Ancestor_NP = true;
				}
				if (temp.value.toLowerCase().startsWith("vp")) {
					has_Ancestor_VP = true;
				}
				if (temp.value.toLowerCase().startsWith("cp")) {
					has_Ancestor_CP = true;
				}
				temp = temp.parent;
			}
			if (has_Ancestor_NP) {
				features.add(new Feature(0, 1, 2));
			} else {
				features.add(new Feature(1, 1, 2));
			}
			if (has_Ancestor_VP) {
				features.add(new Feature(0, 1, 2));
			} else {
				features.add(new Feature(1, 1, 2));
			}
			if (has_Ancestor_CP) {
				features.add(new Feature(0, 1, 2));
			} else {
				features.add(new Feature(1, 1, 2));
			}
		}
		// 9. Left_Comma
		if (firstGap) {
			features.add(new Feature(0, 1, 3));
		} else {
			if (Wl.value.equals("，")) {
				features.add(new Feature(1, 1, 3));
			} else {
				features.add(new Feature(2, 1, 3));
			}
		}
		// 10. SUBJECT
		if (V.parent.value.equalsIgnoreCase("ip")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		// 11. Clause
		if (V == null) {
			features.add(new Feature(3, 1, 4));
		} else {
			int IPCounts = 0;
			temp = V;
			while (temp != root) {
				if (temp.value.toLowerCase().startsWith("ip")) {
					IPCounts++;
				}
				temp = temp.parent;
			}
			if (IPCounts > 1) {
				// subordinate clause
				features.add(new Feature(2, 1, 4));
			} else {
				int totalIPCounts = 0;
				ArrayList<MyTreeNode> frontie = new ArrayList<MyTreeNode>();
				frontie.add(root);
				while (frontie.size() > 0) {
					MyTreeNode tn = frontie.remove(0);
					if (tn.value.toLowerCase().startsWith("ip")) {
						totalIPCounts++;
					}
					frontie.addAll(tn.children);
				}
				if (totalIPCounts > 1) {
					features.add(new Feature(0, 1, 4));
				} else {
					features.add(new Feature(1, 1, 4));
				}
			}
		}
		// headline feature
		if (zero.sentenceID == 0) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		return features;
	}

	@Override
	public ArrayList<Feature> getCategoryFeatures() {
		ArrayList<Feature> features = new ArrayList<Feature>();
		features.addAll(this.syntacticFeatures());
		features.addAll(this.getAZPFeature());

		// features.addAll(this.getPredicateArgumentFeatures());
		// features.addAll(this.lexicalFeatures());
		// features.addAll(this.consistituentFeature());
		return features;
	}

	public ArrayList<Feature> getPredicateArgumentFeatures() {
		ArrayList<Feature> features = new ArrayList<Feature>();

		CoNLLSentence s = this.part.getCoNLLSentences().get(
				this.zero.sentenceID);

		CoNLLWord word = this.part.getWord(this.zero.start);
		CoNLLWord firstV = null;
		for (int i = word.indexInSentence; i < s.getWords().size(); i++) {
			CoNLLWord w = s.getWords().get(i);
			if (w.posTag.startsWith("V")) {
				firstV = w;
				break;
			}
		}
		boolean matchV = false;
		SemanticRole v = null;

		if (firstV == null) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		if (firstV != null) {
			for (SemanticRole r : s.roles) {
				if (r.predict.start == firstV.index) {
					matchV = true;
					v = r;
					break;
				}
			}
		}

		if (matchV) {
			features.add(new Feature(0, 1, 2));
			if (v.args.containsKey("ARG0")) {
				features.add(new Feature(0, 1, 3));
			} else {
				features.add(new Feature(1, 1, 3));
			}
			if (v.args.containsKey("ARG1")) {
				features.add(new Feature(0, 1, 3));
			} else {
				features.add(new Feature(1, 1, 3));
			}
		} else {
			features.add(new Feature(1, 1, 2));
			features.add(new Feature(2, 1, 3));
			features.add(new Feature(2, 1, 3));
		}

		return features;
	}

	public ArrayList<Feature> syntacticFeatures() {
		ArrayList<Feature> features = new ArrayList<Feature>();
		MyTreeNode root = this.part.getCoNLLSentences().get(
				this.zero.sentenceID).syntaxTree.root;
		CoNLLWord word = this.part.getWord(this.zero.start);
		MyTreeNode leaf = root.getLeaves().get(word.indexInSentence);

		// 1st-ip-child
		MyTreeNode firstIP = leaf.getFirstXAncestor("IP");
		if (firstIP != null && firstIP.getLeaves().get(0) == leaf) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		// 1st word in subjectless ip
		boolean subjectlessIP = false;
		ArrayList<MyTreeNode> ipAns = leaf.getXAncestors("IP");
		outer: for (MyTreeNode ip : ipAns) {
			if (ip.getLeaves().get(0) == leaf) {
				for (MyTreeNode child : ip.children) {
					if (child.value.startsWith("NP")) {
						break;
					}
					if (child.value.startsWith("VP")) {
						subjectlessIP = true;
						break outer;
					}
				}
			}
		}
		if (subjectlessIP) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 1st VP-child-after-PU
		boolean firstVPAfterPU = false;
		if (this.zero.start > 0
				&& this.part.getWord(this.zero.start - 1).posTag.equals("PU")) {
			ArrayList<MyTreeNode> vpAns = leaf.getXAncestors("VP");
			for (MyTreeNode vp : vpAns) {
				if (vp.getLeaves().get(0) == leaf) {
					firstVPAfterPU = true;
					break;
				}
			}
		}
		if (firstVPAfterPU) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// NT-in-IP
		boolean ntInIP = false;
		if (word.posTag.equalsIgnoreCase("NT")) {
			ntInIP = true;
			MyTreeNode node = leaf.parent.parent;
			ArrayList<MyTreeNode> rightSisters = node.getRightSisters();
			boolean hasNP = false;
			for (MyTreeNode temp : rightSisters) {
				if (temp.value.startsWith("NP")) {
					hasNP = true;
				}
				if (hasNP && temp.value.startsWith("VP")) {
					ntInIP = false;
					break;
				}
			}
		}
		if (ntInIP) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// verb in NPVP
		if (word.posTag.startsWith("V")
				&& (leaf.getFirstXAncestor("NP") != null || leaf
						.getFirstXAncestor("VP") != null)) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// parent label?

		// has no object
		boolean noobject = false;
		if (leaf.leafIdx > 0) {
			String previousWord = this.part.getWord(this.zero.start - 1).word;
			boolean transitity = false;
			if (this.trans.containsKey(previousWord)
					&& this.part.getWord(this.zero.start - 1).posTag
							.startsWith("V")) {
				if (!this.intrans.containsKey(previousWord)
						|| this.trans.get(previousWord) > 3 * this.intrans
								.get(previousWord)) {
					transitity = true;
				}
			}
			if (transitity) {
				noobject = true;
				MyTreeNode vv = root.getLeaves().get(leaf.leafIdx - 1).parent;
				ArrayList<MyTreeNode> rightSisters = vv.getRightSisters();
				for (MyTreeNode temp : rightSisters) {
					if (temp.value.equalsIgnoreCase("IP")
							|| temp.value.startsWith("NP")) {
						noobject = false;
					}
				}
			}

		}
		if (noobject) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// has no subject
		boolean nosubject = false;

		for (MyTreeNode tmp : leaf.getXAncestors("VP")) {
			if (tmp.getLeaves().get(0) == leaf) {
				ArrayList<MyTreeNode> leftSisters = tmp.getLeftSisters();
				for (MyTreeNode n : leftSisters) {
					if (n.value.equalsIgnoreCase("np")) {
						nosubject = true;
					}
				}
			}
		}

		if (nosubject) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		return features;
	}

	@Override
	public ArrayList<String> getStrFeatures() {
		ArrayList<String> strs = new ArrayList<String>();
		strs.addAll(getXueLexicalFea());
		// strs.addAll(getSRLFeatures());
//		strs.addAll(getParallelFeatures());
		return strs;
	}

	static double th = 0;

	public ArrayList<String> getSRLFeatures() {
		ArrayList<String> strs = new ArrayList<String>();

		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		strs.add(predW.getWord());

		return strs;
	}

	public ArrayList<String> getParallelFeatures() {
		ArrayList<String> strs = new ArrayList<String>();

		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		// strs.add(predW.getWord());
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);

		if (xPredW == null) {
			strs.add("null");
			strs.add("noarg0");
			strs.add("nopred");
		} else {
			strs.add(xPredW.posTag);
			boolean harArg0 = false;
			SemanticRole role = ZeroUtil.getSRL(zero, part);
			if (role != null) {
				for (EntityMention arg0 : role.arg0) {
					System.out.println(arg0.getText(part));
					harArg0 = true;
				}
			} else {
			}
			if (harArg0) {
				strs.add("arg0");
			} else {
				strs.add("arg1");
			}
			if (this.positive) {
				printParallel();
			}
//			if (xPredW.orig.endsWith("ed")
//					&& (xPredW.indexInSentence == 0 || xPredW.sentence
//							.getWord(xPredW.indexInSentence - 1).word
//							.equalsIgnoreCase("be"))) {
//				strs.add("ed");
//			} else if (xPredW.orig.equals("ing")) {
//				strs.add("ing");
//			} else {
//				strs.add("-");
//			}
		}

		return strs;
	}

	private void printParallel() {
		int start = Integer.MAX_VALUE;
		int end = 0;
		CoNLLWord w = this.part.getWord(this.zero.start);
		for (int i = 0; i < this.zero.V.getLeaves().size(); i++) {
			Unit unit = this.documentMap.chiDoc.getUnit(w.indexInDocument + i);
			ArrayList<Unit> xUnit = unit.getMapUnit();
			ArrayList<Double> probs = unit.getMapProb();
			if (xUnit.size() > 0 && probs.get(0) > th) {
				int xID = xUnit.get(0).getId();
				if (xID < start) {
					start = xID;
				}
				if (xID > end) {
					end = xID;
				}
			}
		}
		System.out.println(this.part.getDocument().getDocumentID() + "\t"
				+ this.part.getDocument().getxDoc().getDocumentID() + "\t"
				+ this.documentMap.id);
		System.out.println("==========" + this.positive + "=========");
		StringBuilder sb = new StringBuilder();
		for (int i = this.zero.start - 5; i < this.zero.start
				+ this.zero.V.getLeaves().size(); i++) {
			if (i < 0) {
				continue;
			}
			CoNLLWord W = this.part.getWord(i);
			sb.append(W.orig).append(" ");
		}
		System.out.println(sb.toString().trim());
		System.out.println(this.zero.V);

		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		System.out.println(predW.word);

		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW != null) {
			System.out.println(xPredW.word);
		} else {
			System.out.println("=========");
		}

		sb = new StringBuilder();
		for (int i = start; i <= end; i++) {
			CoNLLWord xW = this.part.getDocument().getxDoc().getWord(i);
			sb.append(xW.orig).append(" ");
		}
		System.out.println(sb.toString().trim());
		sb = new StringBuilder();
		for (int i = start - 5; i <= end; i++) {
			if (i < 0) {
				continue;
			}
			CoNLLWord xW = this.part.getDocument().getxDoc().getWord(i);
			sb.append(xW.orig).append(" ");
		}
		System.out.println(sb.toString().trim());
	}

	private ArrayList<String> getXueLexicalFea() {
		ArrayList<String> strs = new ArrayList<String>();
		// word(0)
		strs.add(part.getWord(this.zero.start).word);
		// word(-1)
		if (this.zero.start > 0) {
			strs.add(part.getWord(this.zero.start - 1).word);
		} else {
			strs.add("-");
		}
		// pos(0)
		strs.add(part.getWord(this.zero.start).posTag);
		// pos(-1, 0)
		if (this.zero.start > 0) {
			strs.add(part.getWord(this.zero.start - 1).posTag + "#"
					+ part.getWord(this.zero.start).posTag);
		} else {
			strs.add("-");
		}
		// pos(0, 1)
		if (this.zero.start < this.part.getWordCount() - 1) {
			strs.add(part.getWord(this.zero.start).posTag + "#"
					+ part.getWord(this.zero.start + 1).posTag);
		} else {
			strs.add("-");
		}
		// pos(0, 1)
		if (this.zero.start < this.part.getWordCount() - 1) {
			strs.add(part.getWord(this.zero.start).posTag + "#"
					+ part.getWord(this.zero.start + 1).posTag);
		} else {
			strs.add("-");
		}
		// pos(0,1,2)
		if (this.zero.start < this.part.getWordCount() - 2) {
			strs.add(part.getWord(this.zero.start).posTag + "#"
					+ part.getWord(this.zero.start + 1).posTag + "#"
					+ part.getWord(this.zero.start + 2).posTag);
		} else {
			strs.add("-");
		}
		// pos(-2, -1)
		if (this.zero.start > 1) {
			strs.add(part.getWord(this.zero.start - 2).posTag + "#"
					+ part.getWord(this.zero.start - 1).posTag);
		} else {
			strs.add("-");
		}
		// word(-1), pos(0)
		if (this.zero.start > 0) {
			strs.add(part.getWord(this.zero.start - 1).word + "#"
					+ part.getWord(this.zero.start).posTag);
		} else {
			strs.add("-");
		}
		// pos(-1), word(0)
		if (this.zero.start > 0) {
			strs.add(part.getWord(this.zero.start - 1).posTag + "#"
					+ part.getWord(this.zero.start).word);
		} else {
			strs.add("-");
		}
		// trans
		String word = part.getWord(this.zero.start).word;
		boolean trans = false;
		boolean intrans = false;
		if (this.trans.containsKey(word)) {
			if (!this.intrans.containsKey(word)) {
				trans = true;
			}
			if (this.intrans.containsKey(word)
					&& this.trans.get(word).intValue() > 3 * this.intrans
							.get(word)) {
				trans = true;
			}
		}

		if (this.intrans.containsKey(word)) {
			if (!this.trans.containsKey(word)) {
				intrans = true;
			}
			if (this.trans.containsKey(word)
					&& this.intrans.get(word).intValue() > 3 * this.trans
							.get(word)) {
				intrans = true;
			}
		}
		if (trans) {
			strs.add("trans");
		} else if (intrans) {
			strs.add("intrans");
		} else {
			strs.add("notranorintrans");
		}
		// prep(0)
		if (part.getWord(this.zero.start).posTag.equalsIgnoreCase("PP")) {
			strs.add("isPP");
		} else {
			strs.add("notPP");
		}

		// boolean subjectlessIP = false;
		boolean subjectlessIP = false;
		MyTreeNode root = this.part.getCoNLLSentences().get(
				this.zero.sentenceID).syntaxTree.root;
		CoNLLWord conllWord = this.part.getWord(this.zero.start);
		MyTreeNode leaf = root.getLeaves().get(conllWord.indexInSentence);
		ArrayList<MyTreeNode> ipAns = leaf.getXAncestors("IP");
		outer: for (MyTreeNode ip : ipAns) {
			if (ip.getLeaves().get(0) == leaf) {
				for (MyTreeNode child : ip.children) {
					if (child.value.startsWith("NP")) {
						break;
					}
					if (child.value.startsWith("VP")) {
						subjectlessIP = true;
						break outer;
					}
				}
			}
		}

		// 1st word in subjectless ip + POS
		if (subjectlessIP) {
			strs.add(part.getWord(this.zero.start).posTag);
		} else {
			strs.add("-");
		}
		return strs;
	}
}
