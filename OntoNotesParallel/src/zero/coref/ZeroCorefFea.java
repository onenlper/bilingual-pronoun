package zero.coref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import model.Entity;
import model.EntityMention;
import model.SemanticRole;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTreeNode;
import util.Common;
import util.Common.Feature;
import util.YYFeature;
import zero.detect.ZeroUtil;
import align.DocumentMap;
import align.DocumentMap.Unit;

public class ZeroCorefFea extends YYFeature {

	ArrayList<EntityMention> zeros;
	ArrayList<EntityMention> candidates;

	EntityMention cand;
	EntityMention zero;

	DocumentMap documentMap;

	CoNLLPart part;
	CoNLLPart xPart;

	public ZeroCorefFea(boolean train, String name) {
		super(train, name);
	}

	ArrayList<ArrayList<Double>> lists = new ArrayList<ArrayList<Double>>();

	@Override
	public ArrayList<Feature> getCategoryFeatures() {

		ArrayList<Feature> feas = new ArrayList<Feature>();

		feas.addAll(this.getNPFeature());
		feas.addAll(this.getZeroFeature());
		feas.addAll(this.getZeroAnaphorFeature());

		return feas;
	}

	HashMap<String, Integer> chainMap;

	public String getObjectNP(EntityMention zero) {
		MyTreeNode vp = zero.V;
		ArrayList<MyTreeNode> leaves = vp.getLeaves();
		for (MyTreeNode leaf : leaves) {
			if (leaf.parent.value.startsWith("V")) {
				ArrayList<MyTreeNode> possibleNPs = leaf.parent
						.getRightSisters();
				for (MyTreeNode tmp : possibleNPs) {
					if (tmp.value.startsWith("NP")
							|| tmp.value.startsWith("QP")) {
						return tmp.getLeaves().get(tmp.getLeaves().size() - 1).value;
					}
				}
			}
		}
		return "";
	}

	public String getObjectNP2(EntityMention zero) {
		MyTreeNode tmp = zero.V;
		while (true) {
			boolean haveVP = false;
			for (MyTreeNode child : tmp.children) {
				if (child.value.equalsIgnoreCase("VP")) {
					haveVP = true;
					tmp = child;
					break;
				}
			}
			if (!haveVP) {
				break;
			}
		}

		ArrayList<MyTreeNode> possibleNPs = tmp.children;
		for (MyTreeNode tm : possibleNPs) {
			if (tm.value.startsWith("NP") || tm.value.startsWith("QP")) {
				return tm.getLeaves().get(tm.getLeaves().size() - 1).value;
			}
		}
		return "";
	}

	@Override
	public ArrayList<String> getStrFeatures() {
		ArrayList<String> strFeas = new ArrayList<String>();
//		strFeas.addAll(this.lexicalFeas());
		// strFeas.addAll(this.getParallelFeatures());
		return strFeas;
	}

	private ArrayList<String> getParallelFeatures() {

		ArrayList<String> strs = new ArrayList<String>();

		EntityMention mapArg0 = this.getChiMapedArg0();

		// if(rules()) {
		// strs.add("YES");
		// } else {
		// strs.add("NO");
		// }

		// if (this.coref)
		// if (mapArg0 != null && ZeroUtil.intersect(this.cand, mapArg0))
		// printParallel();
		return strs;
	}

	public boolean rules() {
		boolean qualify = false;
		if (rule1()) {
			qualify = true;
		} 
//		else if (rule2()) {
//			qualify = true;
//		}
//		else if (rule3()) {
//			qualify = true;
//		}
//		else if (rule4()) {
//			qualify = true;
//		}
//		else if (rule5()) {
//			qualify = true;
//		}
//		else if (rule6()) {
//			qualify = true;
//		}
//		else if (rule7()) {
//			// qualify = true;
//		}

		else if (!qualify) {
			if (this.coref) {
				// printParallel();
			}
		}
		return qualify;
	}

	public boolean rule1() {
		EntityMention mapArg0 = this.getChiMapedArg0();
		if (mapArg0 != null && this.cand.end == mapArg0.end) {
			return true;
		} else {
			if(mapArg0!=null && this.coref) {
				printParallel();
				System.out.println("@@@\t" + mapArg0.getText(part));
			}
		}
		return false;
	}

	/*
	 * 头发 特别 长 ， 要不然 就 剃 光头 就 剃 光头 剃 VV shave VBP role!! ARG0:they ARG1:their
	 * head 光头 ARGM-ADV:otherwise they shave their heads long hair , or
	 * otherwise they shave their heads
	 */
	public boolean rule2() {
		EntityMention engArg0 = this.getEngArg0();
		if (engArg0 == null) {
			return false;
		}
		Collections.sort(engArg0.entity.mentions);
		int idx = ZeroUtil.getEMIndex(engArg0.entity, engArg0);
		for (int i = idx - 1; i >= 0; i--) {
			EntityMention xArg = engArg0.entity.mentions.get(i);

			ZeroUtil.assignHead(xArg, xPart);
			EntityMention chiM = ZeroUtil.getXMention(xArg, xPart,
					this.documentMap.engDoc);
			if (chiM != null && chiM.end == this.cand.end) {
				return true;
			}
		}
		return false;
	}

	// noun
	/*
	 * wb/e2c/00/e2c_0016 wb/eng/00/eng_0016 476 =======true============ 瑟尔 #
	 * 948,948 监管者们 坚定 地 抵制 瑟尔 重金 资助 重金 资助 资助 VV role!! ARG1:游说 ARGM-ADV:重金
	 * campaign NNS campaigns against Searle 's heavily financed campaigns
	 */
	public boolean rule3() {
		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW == null) {
			return false;
		}

		if (!xPredW.posTag.startsWith("N")) {
			return false;
		}

		// find previous posses
		MyTreeNode leaf = xPredW.sentence.getSyntaxTree().root.getLeaves().get(
				xPredW.indexInSentence);

		ArrayList<MyTreeNode> npances = Common.sameEndAncestors(leaf, "NP");
		EntityMention engArg0 = null;
		loop: for (MyTreeNode node : npances) {
			ArrayList<MyTreeNode> leaves = node.getLeaves();
			for (MyTreeNode tmp : leaves) {
				if (tmp.parent.value.startsWith("PRP")) {
					int start = xPredW.sentence.getWord(tmp.leafIdx).index;
					int end = start;
					engArg0 = new EntityMention(start, end);
					ZeroUtil.assignHead(engArg0, xPart);
					break loop;
				}
				if (tmp.value.equalsIgnoreCase("'s")) {
					int start = xPredW.sentence.getWord(tmp.leafIdx - 1).index;
					int end = start;
					engArg0 = new EntityMention(start, end);
					ZeroUtil.assignHead(engArg0, xPart);
					break loop;
				}
			}
		}
		if (engArg0 == null) {
			return false;
		}
		ZeroUtil.assignEntity(engArg0, xPart);
		int idx = ZeroUtil.getEMIndex(engArg0.entity, engArg0);
		for (int i = idx; i >= 0; i--) {
			EntityMention xArg = engArg0.entity.mentions.get(i);
			ZeroUtil.assignHead(xArg, xPart);
			EntityMention chiM = ZeroUtil.getXMention(xArg, xPart,
					this.documentMap.engDoc);
			if (chiM != null && chiM.end == this.cand.end) {
				return true;
			}
		}
		return false;
	}

	// map to arg1
	/*
	 * 波黑 问题 。 双方 表示 希望 在 和平 计划 的 基础 上 “ 能 和平 地 解决 波黑 问题 ” 希望 在 和平 计划 的 基础 上 “ 能
	 * 和平 地 解决 波黑 问题 ” 希望 VV role!! ARG1:在 和平 计划 的 基础 上 “ 能 和平 地 解决 波黑 问题 ” hope
	 * NNS hopes to " peacefully resolve the Bosnia - Herzegovina issue " on the
	 * basis of a peace plan Germany . Both sides expressed hopes to
	 * " peacefully resolve the Bosnia - Herzegovina issue " on the basis of a
	 * peace plan
	 */
	public boolean rule4() {
		// TODO
		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW == null) {
			return false;
		}
		if (!xPredW.posTag.startsWith("N")) {
			return false;
		}
		CoNLLSentence engS = xPredW.sentence;
		ArrayList<SemanticRole> engSRLs = engS.roles;

		for (SemanticRole role : engSRLs) {
			ArrayList<EntityMention> args1 = role.args.get("ARG1");
			ArrayList<EntityMention> args0 = role.args.get("ARG0");
			if (args1 == null || args0 == null) {
				continue;
			}
			EntityMention engArg0 = args0.get(0);
			EntityMention engArg1 = args1.get(0);
			ZeroUtil.assignHead(engArg1, xPart);
			if (engArg1.headStart != xPredW.index) {
				continue;
			}

			ZeroUtil.assignEntity(engArg0, xPart);
			int idx = ZeroUtil.getEMIndex(engArg0.entity, engArg0);
			for (int i = idx; i >= 0; i--) {
				EntityMention xArg = engArg0.entity.mentions.get(i);
				ZeroUtil.assignHead(xArg, xPart);
				EntityMention chiM = ZeroUtil.getXMention(xArg, xPart,
						this.documentMap.engDoc);
				if (chiM != null && chiM.end == this.cand.end) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * =======true============ 我 # 825,825 我 来讲 是 一 个 特 郁闷 特 郁闷 郁闷 VA depressing
	 * JJ especially depressing for me , was an especially depressing
	 */
	public boolean rule5() {
		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW == null || !xPredW.posTag.startsWith("JJ")) {
			return false;
		}
		CoNLLSentence engS = xPredW.sentence;
		MyTreeNode leaf = engS.getSyntaxTree().leaves
				.get(xPredW.indexInSentence);
		// nsubj
		MyTreeNode lowS = Common.getLowestAnce(leaf, "S");
		if (lowS == null) {
			return false;
		}
		boolean hasBe = false;
		int left = lowS.getLeaves().get(0).leafIdx;
		for (int i = leaf.leafIdx - 1; i >= left; i--) {
			CoNLLWord engWord = engS.words.get(i);
			if (engWord.word.equalsIgnoreCase("be")) {
				hasBe = true;
				break;
			}
		}
		if (!hasBe) {
			return false;
		}
		CoNLLWord engArg0W = null;
		for (int i = leaf.leafIdx - 1; i >= left; i--) {
			if (engS.words.get(i).posTag.startsWith("N")
					|| engS.words.get(i).posTag.startsWith("PRP")) {
				engArg0W = engS.words.get(i);
				break;
			}
		}
		if (engArg0W == null) {
			return false;
		}

		int start = engArg0W.index;
		int end = start;
		EntityMention engArg0 = new EntityMention(start, end);
		ZeroUtil.assignHead(engArg0, xPart);
		ZeroUtil.assignEntity(engArg0, xPart);
		int idx = ZeroUtil.getEMIndex(engArg0.entity, engArg0);
		for (int i = idx; i >= 0; i--) {
			EntityMention xArg = engArg0.entity.mentions.get(i);
			ZeroUtil.assignHead(xArg, xPart);
			EntityMention chiM = ZeroUtil.getXMention(xArg, xPart,
					this.documentMap.engDoc);
			if (chiM != null && chiM.end == this.cand.end) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 我 是 危言耸听 ， 那么 就 闭嘴 ， 别 再 无聊 的 浪费 你 自己 和 别人 的 时间 就 闭嘴 ， 别 再 无聊 的 浪费 你 自己 和
	 * 别人 的 时间 闭嘴 VV shut VB role!! ARGM-ADV:if you think i be exaggerate thing
	 * to scare people then , then shut up . No more wasting your own and other
	 * people 's time
	 */
	public boolean rule6() {
		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW == null || !xPredW.posTag.startsWith("V")) {
			return false;
		}
		CoNLLSentence engS = xPredW.sentence;
		MyTreeNode leaf = engS.getSyntaxTree().leaves
				.get(xPredW.indexInSentence);
		MyTreeNode lowS = Common.getLowestAnce(leaf, "S");
		if (lowS == null) {
			return false;
		}
		if (!xPredW.word.equalsIgnoreCase(xPredW.orig)) {
			return false;
		}
		boolean qishi = true;
		for (int i = leaf.leafIdx - 1; i >= lowS.getLeaves().get(0).leafIdx; i--) {
			CoNLLWord word = engS.getWord(i);
			if (word.word.equals(",") || word.word.equals("．")
					|| word.word.equals(".") || word.word.equals("/.")) {
				break;
			}
			if (word.posTag.startsWith("N") || word.posTag.startsWith("V")
					|| word.posTag.startsWith("PRP")) {
				qishi = false;
				break;
			}
		}
		if (!qishi) {
			return false;
		}
		// nsubj
		if (this.cand.getText(part).equalsIgnoreCase("你")
				&& part.getWord(this.cand.start).speaker
						.equalsIgnoreCase(predW.speaker)
				&& !xPredW.word.equalsIgnoreCase("thank")) {
			// printParallel();
			return true;
		}
		return false;
	}

	public boolean rule7() {
		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW == null) {
			return false;
		}
		if (coref && this.cand.getText(part).equals("你")) {
			// printParallel();
		}
		return false;
	}

	public EntityMention getChiMapedArg0() {
		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW == null) {
			return null;
		}
		xPart = xPredW.getSentence().part;
		SemanticRole xRole = ZeroUtil.getSRL(xPredW);
		if (xRole == null) {
			return null;
		}
		if (!xRole.args.containsKey("ARG0")) {
			return null;
		}
		EntityMention xArg0 = xRole.args.get("ARG0").get(0);
		ZeroUtil.assignHead(xArg0, xPart);
		
		if(!xPart.getWord(xArg0.headStart).posTag.startsWith("N") && 
				!xPart.getWord(xArg0.headStart).posTag.startsWith("P")) {
			return null;
		}

		if(xArg0!=null) {
			ZeroCorefTestRule.findArg0++;
		}
		
		EntityMention chiM = ZeroUtil.getXMention(xArg0, xPart,
				this.documentMap.engDoc);
		
		if(!xPart.getWord(xArg0.headStart).posTag.startsWith("N") && chiM!=null) {
//			if (this.coref && this.cand.end == chiM.end)
//				printParallel();
//			return null;
		}
		
		return chiM;
	}

	public EntityMention getEngArg0() {
		CoNLLWord predW = ZeroUtil.getPredicateWord(zero, part);
		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW == null) {
			return null;
		}
		xPart = xPredW.getSentence().part;
		SemanticRole xRole = ZeroUtil.getSRL(xPredW);
		if (xRole == null) {
			return null;
		}
		if (!xRole.args.containsKey("ARG0")) {
			return null;
		}
		EntityMention xArg0 = xRole.args.get("ARG0").get(0);
		ZeroUtil.assignEntity(xArg0, xPart);
		return xArg0;
	}

	static double alighTh = .2;
	boolean coref = false;

	private void printParallel() {
		int start = Integer.MAX_VALUE;
		int end = 0;
		CoNLLWord w = this.part.getWord(this.zero.start);
		for (int i = 0; i < this.zero.V.getLeaves().size(); i++) {
			Unit unit = this.documentMap.chiDoc.getUnit(w.indexInDocument + i);
			ArrayList<Unit> xUnit = unit.getMapUnit();
			ArrayList<Double> probs = unit.getMapProb();
			if (xUnit.size() > 0 && probs.get(0) > alighTh) {
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
		System.out.println("=======" + coref + "============");
		System.out.println(this.cand.getText(part) + " # " + this.cand.start
				+ "," + this.cand.end);
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
		StringBuilder sb3 = new StringBuilder();
		sb3.append(predW.word).append(" ").append(predW.posTag);
		SemanticRole role = ZeroUtil.getSRL(zero, part);
		if (role != null) {
			sb3.append(" role!!");
			for (String roleStr : role.args.keySet()) {
				sb3.append("\t" + roleStr + ":");
				for (EntityMention m : role.args.get(roleStr)) {
					sb3.append(m.getText(part)).append(" ");
				}
			}
		}
		System.out.println(sb3.toString());

		CoNLLWord xPredW = ZeroUtil.getXCoNLLWord(predW, this.documentMap,
				this.part);
		if (xPredW != null) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append(xPredW.word).append(" ").append(xPredW.posTag);
			SemanticRole xRole = ZeroUtil.getSRL(xPredW);
			CoNLLPart xPart = xPredW.sentence.part;
			boolean get = false;
			if (xRole != null) {
				sb2.append(" role!!");
				for (String xRoleStr : xRole.args.keySet()) {
					sb2.append("\t" + xRoleStr + ":");
					for (EntityMention m : xRole.args.get(xRoleStr)) {
						sb2.append(m.getText(xPart)).append(" ");
						ZeroUtil.assignHead(m, xPart);

						EntityMention chiM = ZeroUtil.getXMention(m, xPart,
								this.documentMap.engDoc);
						if (chiM != null) {
							sb2.append(chiM.getText(part)).append(" ");
							get = true;
						}
					}
				}
			}
			System.out.println(sb2.toString());
		} else {
			System.out.println("=========");
		}

		sb = new StringBuilder();
		for (int i = start; i <= end + 5
				&& this.part.getDocument().getxDoc().getWord(i) != null; i++) {
			CoNLLWord xW = this.part.getDocument().getxDoc().getWord(i);
			sb.append(xW.orig).append(" ");
		}
		System.out.println(sb.toString().trim());
		sb = new StringBuilder();
		for (int i = start - 5; i <= end + 5
				&& this.part.getDocument().getxDoc().getWord(i) != null; i++) {
			if (i < 0) {
				continue;
			}
			CoNLLWord xW = this.part.getDocument().getxDoc().getWord(i);
			sb.append(xW.orig).append(" ");
		}
		System.out.println(sb.toString().trim());
	}

	private ArrayList<String> lexicalFeas() {
		ArrayList<String> strFeas = new ArrayList<String>();
		String canHead = cand.head;
		MyTreeNode v1 = cand.V;
		MyTreeNode v2 = zero.V;
		strFeas.add(canHead);

		// if (v1 != null && v2 != null) {
		// String pred1 = ZeroUtil.getPredicateWord(v1,
		// part.getWord(cand.start).sentence, cand.start).word;
		// String pred2 = ZeroUtil.getPredicateWord(v2,
		// part.getWord(zero.start).sentence, zero.start).word;
		//
		// strFeas.add(pred1 + "#" + pred2);
		// } else {
		// strFeas.add("#");
		// }
		// strFeas.clear();
		return strFeas;
	}

	public void set(ArrayList<EntityMention> zeros,
			ArrayList<EntityMention> npMentions, EntityMention np,
			EntityMention zero, CoNLLPart part) {
		this.zeros = zeros;
		this.candidates = npMentions;
		this.cand = np;
		this.zero = zero;
		this.part = part;
	}

	public ArrayList<Feature> getZeroAnaphorFeature() {
		ArrayList<Feature> features = new ArrayList<Feature>();
		int sentenceDis = zero.sentenceID - cand.sentenceID;
		sentenceDis = sentenceDis > 30 ? 30 : sentenceDis;
		features.add(new Feature(sentenceDis, 1, 31));

		int segmentDis = 0;
		for (int i = cand.start; i <= zero.start; i++) {
			String word = part.getWord(i).word;
			if (word.equals("，") || word.equals("；") || word.equals("。")
					|| word.equals("！") || word.equals("？")) {
				segmentDis++;
			}
		}
		segmentDis = segmentDis > 30 ? 30 : segmentDis;
		features.add(new Feature(segmentDis, 1, 31));

		// sibling
		if (cand.end != -1) {
			CoNLLSentence s = part.getCoNLLSentences().get(zero.sentenceID);
			MyTreeNode root = s.syntaxTree.root;
			MyTreeNode V = zero.V;
			boolean sibling = false;

			if (sentenceDis == 0) {
				MyTreeNode leftLeaf = root.getLeaves().get(
						part.getWord(cand.start).indexInSentence);
				MyTreeNode rightLeaf = root.getLeaves().get(
						part.getWord(cand.end).indexInSentence);
				MyTreeNode NPNode = Common.getLowestCommonAncestor(leftLeaf,
						rightLeaf);
				if (V.parent == NPNode.parent) {
					if (V.childIndex - 1 == NPNode.childIndex) {
						sibling = true;
					}
					if (V.childIndex - 2 == NPNode.childIndex
							&& V.parent.children.get(V.childIndex - 2).children
									.get(0).value.equalsIgnoreCase("，")) {
						sibling = true;
					}
				}
			}
			if (sibling) {
				features.add(new Feature(0, 1, 2));
			} else {
				features.add(new Feature(1, 1, 2));
			}
		} else {
			features.add(new Feature(1, 0, 2));
		}

		// closet np
		int npIndex = this.candidates.indexOf(cand);
		if (npIndex == this.candidates.size() - 1
				|| this.candidates.get(npIndex + 1).compareTo(zero) > 0) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		return features;
	}

	private ArrayList<Feature> getZeroFeature() {
		ArrayList<Feature> features = new ArrayList<Feature>();
		CoNLLSentence s = part.getCoNLLSentences().get(zero.sentenceID);
		MyTreeNode root = s.syntaxTree.root;

		MyTreeNode V = zero.V;

		// 0. Z_Has_anc_NP
		if (V.getXAncestors("NP") == null) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		// if(!ancestors.get(0).value.contains("IP")) {
		// System.out.println();
		// }

		// 1. Z_Has_Anc_NP_In_IP
		if (V.getFirstXAncestor("NP") != null
				&& Common.isAncestor(V.getFirstXAncestor("NP"),
						V.getFirstXAncestor("IP"))) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 2. Z_Has_Anc_VP
		if (V.getXAncestors("VP") == null) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 3. Z_Has_Anc_VP_In_IP
		if (V.getFirstXAncestor("VP") != null
				&& Common.isAncestor(V.getFirstXAncestor("VP"),
						V.getFirstXAncestor("IP"))) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 4. Z_Has_Anc_CP
		if (V.getXAncestors("CP") == null) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 10. SUBJECT
		if (V.parent.value.equalsIgnoreCase("ip")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		// 11. Clause
		int IPCounts = 0;
		MyTreeNode temp = V;
		while (temp != root) {
			if (temp.value.toLowerCase().startsWith("ip")) {
				IPCounts++;
			}
			temp = temp.parent;
		}
		if (IPCounts > 1) {
			// subordinate clause
			features.add(new Feature(2, 1, 3));
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
				features.add(new Feature(0, 1, 3));
			} else {
				features.add(new Feature(1, 1, 3));
			}
		}
		// headline feature
		if (zero.sentenceID == 0) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 7. IS_FIRST_ZP
		int zeroIdx = this.zeros.indexOf(zero);
		if (zeroIdx == 0
				|| this.zeros.get(zeroIdx - 1).sentenceID != zero.sentenceID) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		if (zeroIdx == this.zeros.size() - 1
				|| this.zeros.get(zeroIdx + 1).sentenceID != zero.sentenceID) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		return features;
	}

	private ArrayList<Feature> getNPFeature() {
		ArrayList<Feature> features = new ArrayList<Feature>();

		CoNLLSentence s = part.getCoNLLSentences().get(cand.sentenceID);
		MyTreeNode root = s.syntaxTree.root;

		MyTreeNode NPNode = cand.NP;
		// 0. A_HAS_ANC_NP
		if (NPNode.getFirstXAncestor("NP") != null) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 5. A_HAS_ANC_NP_IN_IP
		if (NPNode.getFirstXAncestor("NP") != null
				&& Common.isAncestor(NPNode.getFirstXAncestor("NP"),
						NPNode.getFirstXAncestor("IP"))) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 6. HAS_ANC_VP
		if (NPNode.getFirstXAncestor("VP") != null) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 7. A_HAS_ANC_VP_IN_IP
		if (NPNode.getFirstXAncestor("VP") != null
				&& Common.isAncestor(NPNode.getFirstXAncestor("VP"),
						NPNode.getFirstXAncestor("IP"))) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 8. HAS_ANC_CP
		if (NPNode.getFirstXAncestor("CP") != null) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		boolean object = false;
		boolean subject = false;
		ArrayList<MyTreeNode> rightSisters = NPNode.getRightSisters();
		ArrayList<MyTreeNode> leftSisters = NPNode.getLeftSisters();
		for (MyTreeNode node : rightSisters) {
			if (node.value.equalsIgnoreCase("VP")) {
				subject = true;
				break;
			}
		}

		for (MyTreeNode node : leftSisters) {
			if (node.value.equalsIgnoreCase("VV")) {
				object = true;
				break;
			}
		}

		// 9. A_GRAMMATICAL_ROLE
		if (subject) {
			features.add(new Feature(0, 1, 3));
		} else if (object) {
			features.add(new Feature(1, 1, 3));
		} else {
			features.add(new Feature(2, 1, 3));
		}
		// 10. A_CLAUSE
		int IPCounts = NPNode.getXAncestors("IP").size();
		if (IPCounts > 1) {
			// subordinate clause
			features.add(new Feature(0, 1, 3));
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
				// matrix clause
				features.add(new Feature(1, 1, 3));
			} else {
				// independent clause
				features.add(new Feature(2, 1, 3));
			}
		}

		// A is an adverbial NP
		// if (NP.value.toLowerCase().contains("adv")) {
		// fea[7] = 0;
		// } else {
		// fea[7] = 1;
		// }

		// 12. A is a temporal NP
		if (NPNode.getLeaves().size() != 0
				&& NPNode.getLeaves().get(NPNode.getLeaves().size() - 1).parent.value
						.equals("NT")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// is pronoun
		if (cand.isPronoun) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 14. A is a named entity
		if (!cand.ner.equalsIgnoreCase("other")) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}

		// 15. if in headline
		if (cand.sentenceID == 0) {
			features.add(new Feature(0, 1, 2));
		} else {
			features.add(new Feature(1, 1, 2));
		}
		return features;
	}

}
