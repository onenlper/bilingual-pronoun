package ims.coref.data;

import ims.coref.data.CFGTree.Node;
import ims.util.IntPair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.Common;

import align.DocumentMap.Unit;

public class Sentence {

	public final int sentenceIndex;

	public final String[] forms;
	public final String[] lemmas;
	public final String[] tags;
	public final String[][] feats;
	public final DepTree dt;
	public final String[] cfgCol;
	public final String[] neCol;
	public final CFGTree ct;
	public final List<NE> nes;
	public final String[] corefCol;
	public final String[] speaker;

	public final String[] wholeForm;

	public final String[] bwuv;

	public final String lastSpeaker;
	public final int[] quoteCount;

	public final int[] ids;

	public final Unit[] units;

	public final Document d;
	
	private Map<IntPair, Span> spanMap = new HashMap<IntPair, Span>();

	public Sentence(int index, String[] forms, String[] tags, String[][] feats,
			DepTree depTree, String[] corefs, String[] speaker, String[] neCol,
			String[] cfgCol, String[] lemmas, Document d, String lastSpeaker) {
		this.sentenceIndex = index;
		this.d = d;
		this.forms = forms;
		this.lemmas = lemmas;
		this.tags = tags;
		this.feats = feats;
		this.dt = depTree;
		this.corefCol = corefs;
		this.speaker = speaker;
		this.neCol = neCol;
		this.cfgCol = cfgCol;
		this.bwuv = new String[forms.length];
		ct = new CFGTree(cfgCol, this);
		nes = NE.getNEs(neCol, this);
		this.lastSpeaker = lastSpeaker;
		this.wholeForm = Arrays.copyOf(forms, forms.length);
		this.quoteCount = new int[forms.length];

		// TODO
		this.ids = new int[forms.length];
		ids[0] = -1;
		for (int i = 1; i < forms.length; i++) {
			this.ids[i] = Document.index++;
		}
		this.units = new Unit[forms.length];
		if (d.itself != null) {
			units[0] = null;
			for (int i = 1; i < forms.length; i++) {
				units[i] = this.d.itself.getUnit(ids[i]);
				
				if(units[i]!=null) {
					units[i].sentence = this;
					units[i].indexInSentence = i;
				}
				if (units[i] != null
						&& !units[i].getToken().equalsIgnoreCase(this.forms[i]) && !this.forms[i].equalsIgnoreCase("*pro*")) {
					Common.bangErrorPOS("Error: #" + units[i].getToken() + "#"
							+ this.forms[i] + "#");
				}
			}
		}
	}

	public Span getSpan(int beg, int end) {
		// XXX
		// IntPair ip=IntPair.intPairPool.get(beg, end);
		IntPair ip = new IntPair(beg, end);
		synchronized (spanMap) {
			Span s = spanMap.get(ip);
			if (s == null) {
				Node cfgNode = (ct == null ? null : ct.getExactNode(beg, end));
				s = new Span(this, beg, end, cfgNode);
				spanMap.put(ip, s);
				return s;
			} else {
				return s;
			}
		}
	}

	private static final String HYPHEN = "-";
	private static final String BAR = "|";

	public void clearCorefCol() {
		for (int i = 1; i < corefCol.length; ++i) {
			corefCol[i] = HYPHEN;
		}
	}

	public void addCoref(Span s, Integer key) {
		if (s.start == s.end) { // Single token
			String c = "(" + key + ")";
			// if(corefCol[s.start].contains(c)) //XXX remove this wehn done
			// debugging
			// System.out.println("HERE!");
			if (corefCol[s.start].equals(HYPHEN)) {
				corefCol[s.start] = c;
			} else {
				corefCol[s.start] += BAR + c;
			}

		} else { // Multiple tokens
			String b = "(" + key;
			String e = key + ")";
			// if(corefCol[s.start].contains(b) && corefCol[s.end].contains(e))
			// //XXX remove this too
			// System.out.println("HERE!");
			if (corefCol[s.start].equals(HYPHEN)) {
				corefCol[s.start] = b;
			} else {
				corefCol[s.start] += BAR + b;
			}
			if (corefCol[s.end].equals(HYPHEN)) {
				corefCol[s.end] = e;
			} else {
				corefCol[s.end] += BAR + e;
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < forms.length; ++i) {
			sb.append(forms[i]).append(" ");
		}
		return sb.toString();
	}
}
