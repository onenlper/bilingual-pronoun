package zero.detect;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import model.Entity;
import model.EntityMention;
import model.EventMention;
import model.SemanticRole;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.CoNLL.OntoCorefXMLReader;
import model.syntaxTree.MyTreeNode;
import util.Common;
import align.DocumentMap;
import align.DocumentMap.DocForAlign;
import align.DocumentMap.Unit;

public class ZeroUtil {

	public static String detectBase = "/users/yzcchen/chen3/ijcnlp2013/zero/detect/";

	public static int id;

	public static String getTree(EntityMention m, EntityMention zero,
			CoNLLPart part) {

		CoNLLSentence s1 = part.getWord(m.start).sentence;
		CoNLLSentence s2 = part.getWord(zero.start).sentence;

		int mS = part.getWord(m.start).indexInSentence;
		int mE = part.getWord(m.end).indexInSentence;

		int zS = part.getWord(zero.start).indexInSentence;
		int zE = zS;

		MyTreeNode bigRoot = null;
		MyTreeNode mST = null;
		MyTreeNode mET = null;
		MyTreeNode zST = null;
		MyTreeNode zET = null;
		if (s1 == s2) {
			bigRoot = s1.getSyntaxTree().root.copy();
			mST = bigRoot.getLeaves().get(mS);
			mET = bigRoot.getLeaves().get(mE);

			zST = bigRoot.getLeaves().get(zS);
			zET = bigRoot.getLeaves().get(zE);
		} else {
			bigRoot = new MyTreeNode("SS");
			for (int i = s1.getSentenceIdx(); i <= s2.getSentenceIdx(); i++) {
				MyTreeNode root = part.getCoNLLSentence(i).getSyntaxTree().root
						.copy();
				bigRoot.addChild(root);
			}
			mST = bigRoot.children.get(0).getLeaves().get(mS);
			mET = bigRoot.children.get(0).getLeaves().get(mE);

			zST = bigRoot.children.get(bigRoot.children.size() - 1).getLeaves()
					.get(zS);
			zET = zST;
		}
		bigRoot.setAllMark(false);
		MyTreeNode lowest = Common.getLowestCommonAncestor(mST, zST);
		lowest.mark = true;
		// find VP node
		ArrayList<MyTreeNode> vps = zST.getXAncestors("VP");
		for (MyTreeNode vp : vps) {
			if (vp.getLeaves().get(0) == zST) {
				// attach zp
				int vpChildID = vp.childIndex;
				MyTreeNode zeroNP = new MyTreeNode("NP");
				MyTreeNode zeroNode = new MyTreeNode("XXX");
				zeroNP.addChild(zeroNode);
				vp.parent.addChild(vpChildID, zeroNP);
				break;
			}
		}
		// mark shortest path
		for (int i = mST.getAncestors().size() - 1; i >= 0; i--) {
			MyTreeNode node = mST.getAncestors().get(i);
			if (node == lowest) {
				break;
			}
			node.mark = true;
		}
		for (int i = mET.getAncestors().size() - 1; i >= 0; i--) {
			MyTreeNode node = mET.getAncestors().get(i);
			if (node == lowest) {
				break;
			}
			node.mark = true;
		}
		for (int i = zST.getAncestors().size() - 1; i >= 0; i--) {
			MyTreeNode node = zST.getAncestors().get(i);
			if (node == lowest) {
				break;
			}
			node.mark = true;
		}
		
		int startLeaf = 0;
		int endLeaf = bigRoot.getLeaves().size()-1; 
		for(int i=0;i<bigRoot.getLeaves().size();i++) {
			if(bigRoot.getLeaves().get(i)==mST) {
				startLeaf = i;
			} else if(bigRoot.getLeaves().get(i)==zST) {
				endLeaf = i;
			}
		}
		
		// attach competitors
		for (int i = startLeaf; i <= endLeaf; i++) {
			MyTreeNode leaf = bigRoot.getLeaves().get(i);
			// if under np, mark all np
			for (int j = leaf.getAncestors().size() - 1; j >= 0; j--) {
				MyTreeNode node = leaf.getAncestors().get(j);
				if (node == lowest) {
					break;
				}
				if (node.value.equalsIgnoreCase("np")) {
					node.setAllMark(true);
					// find predicate
					for (MyTreeNode sibling : node.parent.children) {
						if (sibling.value.equalsIgnoreCase("VV")) {
							sibling.setAllMark(true);
						}
					}
					break;
				}
			}
		}

		// attach verb
		for (int i = startLeaf; i <= endLeaf; i++) {
			MyTreeNode leaf = bigRoot.getLeaves().get(i);
			if (leaf.parent.value.startsWith("V")) {
				// if predicate, see if there is subject or object
				for (int j = leaf.getAncestors().size() - 1; j >= 0; j--) {
					MyTreeNode node = leaf.getAncestors().get(j);
					if (node == lowest) {
						break;
					}
					node.mark = true;
				}
			}
		}

		// prune it!!! single in and single out , attach to grand
		ArrayList<MyTreeNode> offsprings = lowest.getDepthFirstOffsprings();
		for (MyTreeNode node : offsprings) {
			// skip pos tag
			if (node.children.size() == 1
					&& node.children.get(0).children.size() == 0) {
				continue;
			}
			if(node.children.size()==0) {
				continue;
			}
			// remove this
			if(node.parent!=null && node.parent.numberMarkChildren()==1 && node.numberMarkChildren()==1) {
				node.parent.children.clear();
				node.parent.children.addAll(node.children);
			}
		}
		// mark min-expansion
		return lowest.getTreeBankStyle(true);
	}

	public static void check(CoNLLDocument chiDoc, CoNLLDocument engDoc,
			DocumentMap dm) {

		DocForAlign chiDA = dm.chiDoc;
		DocForAlign engDA = dm.engDoc;

		for (int i = 0; i < chiDoc.wordCount; i++) {
			String word = chiDoc.getWord(i).orig;

			Unit u = chiDA.getUnit(i);

			if (!u.getToken().equalsIgnoreCase(word)) {
				Common.bangErrorPOS(word + "#" + u.getToken());
			}
		}

		for (int i = 0; i < engDoc.wordCount; i++) {
			String word = engDoc.getWord(i).orig;

			Unit u = engDA.getUnit(i);

			if (!u.getToken().equalsIgnoreCase(word)) {
				Common.bangErrorPOS(word + "#" + u.getToken());
			}
		}
	}

	public static HashMap<String, Integer> formChainMap(
			ArrayList<Entity> entities) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < entities.size(); i++) {
			for (EntityMention m : entities.get(i).mentions) {
				map.put(m.toName(), i);
			}
		}
		return map;
	}

	// TODO only work for English to Chinese
	public static EntityMention getXMention(EntityMention m, CoNLLPart part,
			DocForAlign doc) {
		int hdID = m.headStart;
		Unit unit = doc.getUnit(part.getWord(hdID).indexInDocument);
		// ordered
		ArrayList<Unit> xUnits = unit.getMapUnit();
		for (int i = 0; i < xUnits.size(); i++) {
			Unit xUnit = xUnits.get(i);
			CoNLLPart xPart = xUnit.part;
			double prob = 1;
			if (unit.getMapProb().size() != 0) {
				prob = unit.getMapProb().get(i);
				if (prob < alignTH) {
					continue;
				}
			}
			// System.out.println(xUnits.size() + " $$$$$$$$$$$$$$$$$$$$$4");
			for (int j = 0; j < xUnit.mentions.size(); j++) {
				EntityMention xm = xUnit.mentions.get(j);

				int xhdId = xPart.getWord(xm.end).indexInDocument;
				if (xhdId == xUnit.getId()) {
					return xm;
				}
			}
		}
		return null;
	}

	public static ArrayList<String> stats(String folder) {
		ArrayList<String> files = Common.getLines("chinese_list_" + folder
				+ "_train/");
		ArrayList<String> instances = new ArrayList<String>();

		double allM = 0;
		double azp = 0;

		double notInChainZero = 0;

		int chains = 0;
		int chainwithzero = 0;
		int chainallzero = 0;

		for (String file : files) {
			System.out.println(file);
			CoNLLDocument document = new CoNLLDocument(file);

			OntoCorefXMLReader.addGoldZeroPronouns(document, true);

			for (CoNLLPart part : document.getParts()) {

				ArrayList<Entity> goldChains = part.getChains();

				HashMap<String, Integer> chainMap = formChainMap(goldChains);

				chains += goldChains.size();

				for (Entity goldChain : goldChains) {
					boolean npis = false;
					boolean zerois = false;
					allM += goldChain.mentions.size();
					for (int i = 0; i < goldChain.mentions.size(); i++) {
						Collections.sort(goldChain.mentions);

						EntityMention m = goldChain.mentions.get(i);

						if (m.end == -1) {
							zerois = true;
						} else {
							npis = true;
						}

						EntityMention zero = goldChain.mentions.get(i);
						if (zero.end != -1) {
							continue;
						}
						azp++;

						if (m.notInChainZero) {
							notInChainZero++;

							System.out.println(goldChain.mentions.get(0)
									.toName()
									+ " # "
									+ goldChain.mentions.get(1).toName());

							Common.bangErrorPOS("pp");

						} else {
							if (goldChain.mentions.size() == 1) {
								Common.bangErrorPOS("NO");
							}
						}
					}

					if (zerois) {
						chainwithzero++;
					}
					if (!npis) {
						chainallzero++;
					}
				}
			}
		}
		System.out.println("allM:" + allM);
		System.out.println("azp:" + azp);
		System.out.println("azp/allM:" + azp / allM);
		System.out.println("newNPMentions:" + OntoCorefXMLReader.newNPMentions);
		System.out.println("zeroCorefNewNP:"
				+ OntoCorefXMLReader.zeroCorefNewNP);
		System.out.println("notInChainZero: " + notInChainZero);

		System.out.println(allM - azp - OntoCorefXMLReader.newNPMentions);

		System.out.println("chains: " + chains);
		System.out.println("chainwithzero: " + chainwithzero);
		System.out.println("chainallzero: " + chainallzero);
		return instances;
	}

	public static void attachToMap(ArrayList<EntityMention> mentions,
			DocForAlign doc, CoNLLPart part) {
		for (EntityMention m : mentions) {
			attachToMap(m, doc, part);
		}
	}

	public static void attachToMap(EntityMention mention, DocForAlign doc,
			CoNLLPart part) {
		for (int i = mention.start; i <= mention.end; i++) {
			CoNLLWord w = part.getWord(i);
			Unit u = doc.getUnit(w.indexInDocument);
			u.addMention(mention);
			u.part = part;
		}
	}

	public static void assignHead(EntityMention arg, CoNLLPart part) {
		if (arg.headStart != -1) {
			return;
		}
		int start = arg.start;
		int end = arg.end;
		ZeroUtil.assignNPNode(arg, part);
		MyTreeNode np = arg.NP;
		MyTreeNode headLeaf = np.getHeadLeaf();
		CoNLLSentence s = part.getWord(start).sentence;
		arg.headStart = s.getWord(headLeaf.leafIdx).index;
	}

	public static boolean intersect(EntityMention m1, EntityMention m2) {
		if (m1.end < m2.start) {
			return false;
		}
		if (m2.end < m1.start) {
			return false;
		}
		return true;
	}

	public static void assignEntity(EntityMention arg, CoNLLPart part) {
		assignHead(arg, part);
		// head equal
		for (Entity ent : part.getChains()) {
			for (EntityMention em : ent.mentions) {
				if (em.end == arg.end) {
					arg.entity = ent;
					return;
				}
			}
		}
		// overlap
		// for (Entity ent : part.getChains()) {
		// for (EntityMention em : ent.mentions) {
		// if (!((em.end < arg.start) || (arg.end < em.start))) {
		// return ent;
		// }
		// }
		// }

		Entity ent = new Entity();
		ent.addMention(arg);
		arg.entity = ent;
		sortMentionsInOneEntity(arg.entity, arg, part);
	}

	public static HashMap<String, Integer> posMap = new HashMap<String, Integer>();

	public static void increaseMap(HashMap<String, Integer> map, String key) {
		Integer i = map.get(key);
		if (i == null) {
			map.put(key, 1);
		} else {
			map.put(key, i.intValue() + 1);
		}
	}

	private static void sortMentionsInOneEntity(Entity entity, EntityMention m,
			CoNLLPart part) {
		// sort entitymention accorading to
		Collections.sort(entity.mentions);
		String head = part.getWord(m.headStart).orig;
		String extend = m.getText(part);
		ArrayList<EntityMention> olds = entity.mentions;
		ArrayList<EntityMention> sorted = new ArrayList<EntityMention>();

		String speaker = part.getWord(m.headStart).speaker;

		sorted.add(m);
		olds.remove(m);

		// for () {
		// if (olds.get(i).end < m.start) {
		// sorted.add(olds.get(i));
		// olds.remove(i);
		// i--;
		// }
		// }

		for (int i = olds.size() - 1; i >= 0; i--) {
			EntityMention tmp = entity.mentions.get(i);
			assignHead(tmp, part);
			String headTmp = part.getWord(tmp.headStart).orig;
			String extendTmp = tmp.getText(part);
			String speakerTmp = part.getWord(tmp.headStart).speaker;
			if (extendTmp.equalsIgnoreCase(extend)
					&& speaker.equalsIgnoreCase(speakerTmp)) {
				sorted.add(tmp);
				olds.remove(i);
				i++;
			}
		}

		for (int i = olds.size() - 1; i >= 0; i--) {
			EntityMention tmp = entity.mentions.get(i);
			assignHead(tmp, part);
			String headTmp = part.getWord(tmp.headStart).orig;
			String extendTmp = tmp.getText(part);
			if (headTmp.equalsIgnoreCase(head)) {
				sorted.add(tmp);
				olds.remove(i);
				i++;
			}
		}

		for (int i = olds.size() - 1; i >= 0; i--) {
			EntityMention tmp = entity.mentions.get(i);
			assignHead(tmp, part);
			String headTmp = part.getWord(tmp.headStart).orig;
			String extendTmp = tmp.getText(part);
			sorted.add(tmp);
			olds.remove(i);
			i++;
		}

		entity.mentions = sorted;
	}

	public static int getEMIndex(Entity ent, EntityMention em) {
		for (int i = 0; i < ent.mentions.size(); i++) {
			if (em.start == ent.mentions.get(i).start
					&& em.end == ent.mentions.get(i).end) {
				return i;
			}
		}
		return -1;
	}

	public static ArrayList<EntityMention> getGoldZeros(ArrayList<Entity> chains) {
		ArrayList<EntityMention> zeros = new ArrayList<EntityMention>();
		for (Entity entity : chains) {
			for (int i = 0; i < entity.mentions.size(); i++) {
				EntityMention m2 = entity.mentions.get(i);
				if (m2.end == -1) {
					zeros.add(m2);
				}
			}
		}
		return zeros;
	}

	public static SemanticRole getSRL(CoNLLWord word) {
		int position = word.index;
		CoNLLPart part = word.sentence.part;
		CoNLLSentence s = part.getWord(position).sentence;
		ArrayList<SemanticRole> roles = s.roles;
		for (SemanticRole role : roles) {
			EventMention predicate = role.predict;
			if (predicate.start == position) {
				return role;
			}
		}
		return null;
	}

	public static SemanticRole getSRL(EntityMention zero, CoNLLPart part) {
		CoNLLSentence s = part.getWord(zero.start).sentence;
		ArrayList<SemanticRole> roles = s.roles;
		for (int i = 0; i < zero.V.getLeaves().size(); i++) {
			for (SemanticRole role : roles) {
				EventMention predicate = role.predict;
				if (predicate.start == i + zero.start) {
					return role;
				}
			}
		}
		return null;
	}

	public static CoNLLWord getPredicateWord(MyTreeNode V,
			CoNLLSentence sentence, int start) {
		CoNLLPart part = sentence.part;
		CoNLLWord predW = null;
		// ArrayList<SemanticRole> roles = sentence.roles;
		// loop: for (int i = 0; i < V.getLeaves().size(); i++) {
		// for (SemanticRole role : roles) {
		// EventMention predicate = role.predict;
		// if (predicate.start == i + start) {
		// predW = part.getWord(predicate.start);
		// break loop;
		// }
		// }
		// }
		if (predW == null) {
			for (int i = 0; i < V.getLeaves().size(); i++) {
				MyTreeNode tr = V.getLeaves().get(i);
				if (tr.parent.value.toLowerCase().startsWith("v")) {
					predW = part.getWord(start + i);
					break;
				}
			}
		}
		if (predW == null) {
			for (int i = 0; i < V.getLeaves().size(); i++) {
				MyTreeNode tr = V.getLeaves().get(i);
				if (tr.parent.value.toLowerCase().startsWith("ad")) {
					predW = part.getWord(start + i);
					break;
				}
			}
		}
		if (predW == null) {
			predW = part.getWord(start);
		}
		if (predW == null) {
			Common.bangErrorPOS(V.getTreeBankStyle(true));
		}
		return predW;
	}

	public static CoNLLWord getPredicateWord(EntityMention zero, CoNLLPart part) {
		CoNLLSentence s = part.getWord(zero.start).sentence;
		return getPredicateWord(zero.V, s, zero.start);
	}

	static double alignTH = .4;

	public static double subj = 0;

	public static double allV = 0;

	public static CoNLLWord getXCoNLLWord(CoNLLWord predW,
			DocumentMap documentMap, CoNLLPart part) {
		CoNLLWord xPredW = null;
		// TODO
		Unit predU = documentMap.chiDoc.getUnit(predW.indexInDocument);
		ArrayList<Unit> xPredUnit = predU.getMapUnit();
		ArrayList<Double> xPredProbs = predU.getMapProb();
		if (xPredUnit.size() > 0 && xPredProbs.get(0) > alignTH) {
			int xID = xPredUnit.get(0).getId();
			xPredW = part.getDocument().getxDoc().getWord(xID);
		}
		return xPredW;
	}

	private static String prefix = "/shared/mlrdir1/disk1/mlr/corpora/CoNLL-2012/conll-2012-train-v0/data/files/data/chinese/annotations/";
	private static String anno = "annotations/";
	private static String suffix = ".coref";

	public static boolean hasZeroAnno(CoNLLDocument document) {
		String conllPath = document.getFilePath();
		int a = conllPath.indexOf(anno);
		int b = conllPath.indexOf(".");
		String middle = conllPath.substring(a + anno.length(), b);
		String path = prefix + middle + suffix;
		// System.out.println(path);

		// TODO
		if (!(new File(path)).exists()) {
			document.noZeroAnnotation = true;
			return false;
		}
		return true;
	}

	// gold parse tree
	static double detectTH = -0.925;

	public static ArrayList<EntityMention> loadClassifiedMention(
			CoNLLPart part, String folder) {
		String filePath = ZeroUtil.detectBase + "chi" + folder + "/test/"
				+ part.getPartName().replace(File.separator, "-");

		ArrayList<EntityMention> zeros = new ArrayList<EntityMention>();

		if (!(new File(filePath + ".detectzeros")).exists()) {
			return zeros;
		}

		ArrayList<String> mStrs = Common.getLines(filePath + ".detectzeros");
		ArrayList<String> preds = Common.getLines(filePath + ".detectpreds");
		if (mStrs.size() != preds.size()) {
			Common.bangErrorPOS("");
		}
		for (int i = 0; i < mStrs.size(); i++) {
			String mStr = mStrs.get(i);
			double pred = Double.parseDouble(preds.get(i));
			if (pred > detectTH) {
				EntityMention zero = new EntityMention();
				zero.start = Integer.parseInt(mStr.split(",")[0]);
				zero.end = -1;
				zeros.add(zero);
			}
		}
		return zeros;
	}

	public static ArrayList<EntityMention> getAnaphorZeros(
			ArrayList<Entity> chains) {
		ArrayList<EntityMention> zeros = new ArrayList<EntityMention>();
		for (Entity entity : chains) {
			for (int i = 0; i < entity.mentions.size(); i++) {
				EntityMention m2 = entity.mentions.get(i);
				if (m2.end != -1) {
					continue;
				}
				for (int j = 0; j < i; j++) {
					EntityMention m1 = entity.mentions.get(j);
					if (m1.end != -1) {
						zeros.add(m2);
						break;
					}
				}
			}
		}
		return zeros;
	}

	public static void assignVNode(ArrayList<EntityMention> zeros,
			CoNLLPart part) {
		for (EntityMention zero : zeros) {
			assignVNode(zero, part);
		}
	}

	public static void assignVNode2(ArrayList<EntityMention> zeros,
			CoNLLPart part) {
		for (EntityMention zero : zeros) {
			assignVNode2(zero, part);
		}
	}

	public static void assignNPNode(ArrayList<EntityMention> mentions,
			CoNLLPart part) {
		for (EntityMention mention : mentions) {
			assignNPNode(mention, part);
		}
	}

	public static boolean subjectNP(EntityMention np, CoNLLPart part) {
		if (np.end == -1) {
			return true;
		}
		MyTreeNode npNode = np.NP;
		ArrayList<MyTreeNode> rightSisters = npNode.getRightSisters();
		for (MyTreeNode sister : rightSisters) {
			if (sister.value.equals("VP")) {
				np.V = sister;
				return true;
			}
		}
		return false;
	}

	public static boolean objectNP(EntityMention np, CoNLLPart part) {
		MyTreeNode npNode = np.NP;
		ArrayList<MyTreeNode> leftSisters = npNode.getLeftSisters();
		for (MyTreeNode sister : leftSisters) {
			if (sister.value.startsWith("V")) {
				return true;
			}
		}
		return false;
	}

	public static void assignNPNode(EntityMention mention, CoNLLPart part) {
		CoNLLSentence s = part.getWord(mention.start).sentence;
		MyTreeNode root = s.syntaxTree.root;
		MyTreeNode leftLeaf = root.getLeaves().get(
				part.getWord(mention.start).indexInSentence);
		MyTreeNode rightLeaf = root.getLeaves().get(
				part.getWord(mention.end).indexInSentence);
		MyTreeNode NPNode = Common.getLowestCommonAncestor(leftLeaf, rightLeaf);
		mention.NP = NPNode;

		// subject or object
		mention.isSubject = subjectNP(mention, part);
		mention.isObject = objectNP(mention, part);

	}

	public static void assignVNode(EntityMention zero, CoNLLPart part) {
		MyTreeNode V = null;
		zero.sentenceID = part.getWord(zero.start).sentence.getSentenceIdx();
		CoNLLSentence s = part.getCoNLLSentences().get(zero.sentenceID);
		MyTreeNode root = s.syntaxTree.root;
		CoNLLWord word = part.getWord(zero.start);
		MyTreeNode leaf = root.getLeaves().get(word.indexInSentence);

		for (MyTreeNode node : leaf.getAncestors()) {
			if (node.value.toLowerCase().startsWith("vp")
					&& node.getLeaves().get(0) == leaf) {
				V = node;
			}
		}

		if (V == null) {
			for (MyTreeNode node : leaf.getAncestors()) {
				if (node.value.startsWith("DFL")
						&& node.getLeaves().get(0) == leaf) {
					V = node;
				}
			}
		}

		if (V == null) {
			int offset = 1;
			while (true) {
				word = part.getWord(zero.start + (offset++));
				leaf = root.getLeaves().get(word.indexInSentence);
				for (MyTreeNode node : leaf.getAncestors()) {
					if (node.value.toLowerCase().startsWith("vp")
							&& node.getLeaves().get(0) == leaf) {
						V = node;
					}
				}
				if (V != null) {
					break;
				}
				if (zero.start + offset == part.getWordCount()) {
					break;
				}
			}
		}

		if (V == null) {
			leaf = root.getLeaves().get(
					part.getWord(zero.start).indexInSentence);
			for (MyTreeNode node : leaf.getAncestors()) {
				if (node.value.startsWith("NP")
						&& node.getLeaves().get(0) == leaf) {
					V = node;
				}
			}
		}
		zero.V = V;
	}

	public static void assignVNode2(EntityMention zero, CoNLLPart part) {
		MyTreeNode V = null;
		zero.sentenceID = part.getWord(zero.start).sentence.getSentenceIdx();
		CoNLLSentence s = part.getCoNLLSentences().get(zero.sentenceID);
		MyTreeNode root = s.syntaxTree.root;
		CoNLLWord word = part.getWord(zero.start);
		MyTreeNode leaf = root.getLeaves().get(word.indexInSentence);

		for (MyTreeNode node : leaf.getAncestors()) {
			if (node.value.toLowerCase().startsWith("vp")
					&& node.getLeaves().get(0) == leaf) {
				V = node;
			}
		}

		if (V == null) {
			for (MyTreeNode node : leaf.getAncestors()) {
				if (node.value.startsWith("DFL")
						&& node.getLeaves().get(0) == leaf) {
					V = node;
				}
			}
		}

		if (V == null) {
			for (MyTreeNode node : leaf.getAncestors()) {
				if (node.value.startsWith("PP")
						&& node.getLeaves().get(0) == leaf) {
					V = node;
				}
			}
		}

		zero.V = V;
	}

	public static void addEmptyCategoryNode(EntityMention zero) {
		MyTreeNode V = zero.V;
		MyTreeNode newNP = new MyTreeNode();
		newNP.value = "NP";
		int VIdx = V.childIndex;
		V.parent.addChild(VIdx, newNP);

		MyTreeNode empty = new MyTreeNode();
		empty.value = "-NONE-";
		newNP.addChild(empty);

		MyTreeNode child = new MyTreeNode();
		child.value = "*pro*";
		empty.addChild(child);
		child.emptyCategory = true;
		zero.NP = newNP;
	}
}
