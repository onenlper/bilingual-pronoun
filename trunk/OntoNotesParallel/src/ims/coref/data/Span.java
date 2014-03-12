package ims.coref.data;

import ims.coref.Parallel;
import ims.coref.data.CFGTree.Node;
import ims.coref.features.enums.Gender;
import ims.coref.features.enums.Num;
import ims.coref.features.enums.SemanticClass;
import ims.coref.lang.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import util.Common;
import align.DocumentMap.SentForAlign;
import align.DocumentMap.Unit;

public class Span implements Comparable<Span> {

	public final Sentence s;
	public final int start;
	public final int end;
	public final NE ne;

	public int sid = -1;

	public int hd = -1;
	public int hdgov = -1;
	public int hdlmc = -1;
	public int hdrmc = -1;
	public int hdls = -1;
	public int hdrs = -1;

	public Span miniOne;

	public static boolean CC = false;

	public int xSpanType = 0;

	public double alignProb = 0;

	public CFGTree.Node cfgNode;

	// Atomic span features below:
	public boolean isProperName;
	public boolean isPronoun;
	public boolean isDefinite;
	public boolean isDemonstrative;
	public boolean isQuoted;

	public boolean getSinglePronoun() {
		if (Parallel.testzero) {
			if (this.s.tags[this.start].equalsIgnoreCase("-none-")
					&& this.start == this.end) {
				return true;
			} else {
				return false;
			}
		}
		return (this.isPronoun && this.start == this.end);
	}

	public Gender gender = Gender.Unknown;
	public Num number = Num.Unknown;
	public SemanticClass semanticClass = SemanticClass.Unknown;

	public double anaphoricityPr = -1;

	public boolean isAnaphor = false;

	public Span(boolean empty) {
		s = null;
		start = 0;
		end = 0;
		ne = null;
		this.empty = empty;
	}

	public boolean empty = false;

	public Span(int sid, int start, int end) {
		this.sid = sid;
		this.start = start;
		this.end = end;
		this.s = null;
		this.ne = null;
	}

	Span(Sentence s, int st, int e, Node cfgNode) {
		this.s = s;
		this.start = st;
		this.end = e;
		this.cfgNode = cfgNode;
		assignHeadsEtc();
		ne = getNamedEntity();
		Language.getLanguage().computeAtomicSpanFeatures(this);

		if (Language.getLanguage().equals(s.d.lang)) {
			Common.bangErrorPOS("");
		}

		// here, assign unit with span
		if (s.d.itself != null) {
			for (int i = this.start; i <= this.end; i++) {
				int id = s.ids[i];
				Unit unit = s.d.itself.getUnit(id);
				if (unit != null) {
					unit.addSpan(this);
				}
			}
			// Span xSpan = this.getXSpan();
			// if (xSpan != null) {
			// // System.out.println(this.getText() + " # " + xSpan.getText());
			// }
			// if (xSpan != null) {
			// String spanText = this.getText().toLowerCase();
			// String xspanText = xSpan.getText().toLowerCase();
			//
			// String spanHead = this.s.forms[this.hd].toLowerCase();
			// String xspanHead = xSpan.s.forms[xSpan.hd]
			// .toLowerCase();
			//
			// HashSet<String> xspanTextSet = s.d.itself.spanMatch
			// .get(spanText);
			// if (xspanTextSet == null) {
			// xspanTextSet = new HashSet<String>();
			// s.d.itself.spanMatch.put(spanText, xspanTextSet);
			// }
			// xspanTextSet.add(xspanText);
			//
			// HashSet<String> xspanHeadSet = s.d.itself.headMatch
			// .get(spanHead);
			// if (xspanHeadSet == null) {
			// xspanHeadSet = new HashSet<String>();
			// s.d.itself.headMatch.put(spanHead, xspanHeadSet);
			// }
			// xspanHeadSet.add(xspanHead);
			// }
		}
	}

	public String getPosition() {
		if (Parallel.testzero) {
			int sid = this.s.sentenceIndex;
			int start = this.start;
			int end = this.end;
			int offset = 0;
			for (int i = this.start - 1; i >= 0; i--) {
				if (this.s.forms[i].equalsIgnoreCase("*pro*")) {
					offset++;
				}
			}
			start -= offset;

			if (this.getSinglePronoun()) {
				end = 0;
			} else {
				offset = 0;
				for (int i = this.end - 1; i >= 0; i--) {
					if (this.s.forms[i].equalsIgnoreCase("*pro*")) {
						offset++;
					}
				}
				end -= offset;
			}
			return sid + ":" + start + "," + end;
		}
		if (this.s == null) {
			return this.sid + ":" + this.start + "," + this.end;
		} else {
			return this.s.sentenceIndex + ":" + this.start + "," + this.end;
		}
	}

	public String getReadName() {
		int sid = this.s.ids[this.start];
		int eid = this.s.ids[this.end];
		return this.s.d.docName + ":" + this.s.d.lang + ":" + sid + "," + eid;
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (int i = this.start; i <= this.end; i++) {
			sb.append(this.s.forms[i]).append(" ");
		}
		return sb.toString().trim();
	}

	private Span getExactMatchXSpan() {
		// match head id
		Document d = this.s.d;
		if (d.itself == null) {
			return null;
		}
		int hdID = this.s.ids[this.hd];
		Span xSpan = null;
		if (d.itself.getUnit(hdID) != null) {
			Unit unit = d.itself.getUnit(hdID);
			// ordered
			ArrayList<Unit> xUnits = unit.getMapUnit();
			loop: for (int i = 0; i < xUnits.size(); i++) {
				Unit xUnit = xUnits.get(i);
				double prob = 1;
				if (unit.getMapProb().size() != 0) {
					prob = unit.getMapProb().get(i);
					if (prob < th) {
						continue;
					}
				}
				for (Span xs : xUnit.spans) {
					int hdId = xs.s.ids[xs.hd];
					if (hdId == xUnit.getId()
							&& xs.ccStruct() == this.ccStruct()) {
						xSpan = xs;
						xSpan.xSpanType = (int) Math.ceil((prob / 0.25));
						xSpan.alignProb = prob;
						// TODO
						break loop;
					}
				}
			}
		}
		return xSpan;
	}

	private boolean ccStruct() {
		boolean cc = false;
		for (int i = this.start; i <= this.end; i++) {
			String tag = this.s.tags[i];
			if (tag.equalsIgnoreCase("CC")) {
				cc = true;
				break;
			}
		}
		return cc;
	}

	private Span getPartialMatchXSpan() {
		Document d = this.s.d;
		if (d.itself == null) {
			return null;
		}
		Span xSpan = null;
		HashMap<Span, Integer> candXSpans = new HashMap<Span, Integer>();
		int startID = this.s.ids[this.start];
		int endID = this.s.ids[this.end];
		for (int id = startID; id <= endID; id++) {
			Unit unit = d.itself.getUnit(id);
			if (unit == null) {
				continue;
			}
			ArrayList<Unit> xUnits = unit.getMapUnit();
			HashSet<Span> overlapXSpans = new HashSet<Span>();
			for (Unit xUnit : xUnits) {
				for (Span xs : xUnit.spans) {
					overlapXSpans.add(xs);
				}
			}
			for (Span xs : overlapXSpans) {
				Integer num = candXSpans.get(xs);
				if (num == null) {
					candXSpans.put(xs, 1);
				} else {
					candXSpans.put(xs, num.intValue() + 1);
				}
			}
		}
		// find the max one
		int maxNum = 0;
		for (Span xs : candXSpans.keySet()) {
			int num = candXSpans.get(xs);
			if (num > maxNum) {
				xSpan = xs;
				maxNum = num;
			}
		}
		return xSpan;
	}

	public Span getSameTextMapSpan() {
		if (this.s.d.itself == null) {
			return null;
		}
		String head = this.s.forms[this.hd].toLowerCase();
		int hdID = this.s.ids[this.hd];
		Span xSpan = null;
		HashSet<String> xHeads = headMaps.get(head);
		Unit unitHd = this.s.d.itself.getUnit(hdID);
		SentForAlign xSentForAlign = unitHd.sentForAlign.mapSentence;
		if (xHeads != null) {
			loop: for (String xHead : xHeads) {
				for (Unit xunit : xSentForAlign.units) {
					if (xunit.getToken().equalsIgnoreCase(xHead)) {
						for (Span xs : xunit.spans) {
							if (xs.s.ids[xs.hd] == xunit.getId()) {
								xSpan = xs;
								break loop;
							}
						}
					}
				}
			}
		}
		return xSpan;
	}

	// best chi th = 0.2
	// best eng th =
	// best eng train: .9, test,0.5

	double th = 0.2;

	public Span getCreatedSpan() {
		Span xSpan = null;
		// int hdID = this.s.ids[this.hd];

		int leftID = this.s.ids[this.start];
		int rightID = this.s.ids[this.end];

		Unit xStartU = null;
		if (this.s.d.itself.getUnit(leftID) != null) {
			Unit unit = this.s.d.itself.getUnit(leftID);
			ArrayList<Unit> xUnits = unit.getMapUnit();
			for (int i = 0; i < xUnits.size(); i++) {
				Unit xUnit = xUnits.get(i);
				if (xUnit.sentence == null) {
					continue;
				}
				double prob = unit.getMapProb().get(i);
				if (prob < th) {
					continue;
				}
				xStartU = xUnit;
				break;
			}
		}

		if (xStartU != null) {
			if (this.s.d.itself.getUnit(rightID) != null) {
				Unit unit = this.s.d.itself.getUnit(rightID);
				ArrayList<Unit> xUnits = unit.getMapUnit();
				for (int i = 0; i < xUnits.size(); i++) {
					Unit xUnit = xUnits.get(i);
					if (xUnit.sentence == null
							|| xUnit.sentence != xStartU.sentence) {
						continue;
					}
					double prob = unit.getMapProb().get(i);
					if (prob < th) {
						continue;
					}
					xSpan = xUnit.sentence.getSpan(xStartU.indexInSentence,
							xUnit.indexInSentence);
					xSpan.alignProb = prob;
					break;
				}
			}
		}

		return xSpan;
	}

//	public static HashSet<String> mtTestSet = Common.readFile2Set("MTTestMapChiMention");
//	public static HashSet<String> parallelTestSet = Common.readFile2Set("goldParallelTestMapChiMention");
	
	public static HashSet<String> mtTestSet = Common.readFile2Set("MTTrainMapChiMention");
	public static HashSet<String> parallelTestSet = Common.readFile2Set("MixMTTrainMapChiMention");
	
	// find mapped span
	public Span getXSpan() {
		if(!mtTestSet.contains(this.getMK()) || !parallelTestSet.contains(this.getMK())) {
			if(this.s.d.lang.equals("chi")) {
//				return null;
			}
		}
		
		if (this.empty) {
			return new Span(true);
		}

		if (!CC) {
			return null;
		}

		if (this.s.d.documentMap == null) {
			return null;
		}

		Span xSpan = this.getXSpanFromCache();
		if (xSpan == null && assignMode >= 1 && assignMode <= 4) {
			assignXSpan();
		}
		
		if(xSpan!=null && this.s.d.lang.equals("chi")) {
//			mappedChiMs.add(this.getMK());
		}
		
		return xSpan;
	}

	public String getMK() {
		return this.s.d.docName + "#" + this.s.d.docNo + ":" + this.s.sentenceIndex + ":"
				+ this.start + "," + this.end;
	}

	public static HashSet<String> mappedChiMs = new HashSet<String>();

	private Span getXSpanFromCache() {
		if (this.s.d.lang.equals("chi")) {
			Span xSpan = chiSpanMaps.get(this.getReadName());
			if (xSpan != null) {
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
			return xSpan;
		} else {
			Span xSpan = engSpanMaps.get(this.getReadName());
			if (xSpan != null) {
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
			return xSpan;
		}
	}

	public static int assignMode = 0;

	// enforce one-one map
	public static HashMap<String, Span> chiSpanMaps = new HashMap<String, Span>();
	public static HashMap<String, Span> engSpanMaps = new HashMap<String, Span>();

	public static HashMap<String, HashSet<String>> headMaps = new HashMap<String, HashSet<String>>();

	public static int a = 0;
	
	private void assignXSpan() {
		Span xSpan = null;
		if (assignMode == 1) {
			xSpan = this.getExactMatchXSpan();
			if (xSpan != null) {
				// xSpan.xSpanType = 1;
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
		}
		if (assignMode == 2) {
			// xSpan = this.getPartialMatchXSpan();
			if (xSpan != null) {
				xSpan.xSpanType = 5;
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
		}
		if (assignMode == 3) {
			// xSpan = this.getSameTextMapSpan();
			if (xSpan != null) {
				xSpan.xSpanType = 6;
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
		}
		if (assignMode == 4) {
			xSpan = this.getCreatedSpan();
			if (xSpan != null) {
				xSpan.xSpanType = 7;
				this.xSpanType = xSpan.xSpanType;
				this.alignProb = xSpan.alignProb;
			}
		}

		if (xSpan != null) {
			if(this.s.d.lang.equalsIgnoreCase("chi")) {
//				System.out.println(this.getText() + "#" + xSpan.getText() + "#");
//				System.out.println(this.s.toString());
//				System.out.println(xSpan.s.toString());
//				System.out.println("==" + (a++) +"==");
			}
			
			boolean put = false;
			if (this.s.d.lang.equals("eng")
					&& (chiSpanMaps.get(xSpan.getReadName()) == null || chiSpanMaps
							.get(xSpan.getReadName())
							.equals(this.getReadName()))) {
				put = true;
			} else if (this.s.d.lang.equals("chi")
					&& (engSpanMaps.get(xSpan.getReadName()) == null || engSpanMaps
							.get(xSpan.getReadName())
							.equals(this.getReadName()))) {
				put = true;
			}
			if (put) {
				if (this.s.d.lang.equals("eng")) {
					engSpanMaps.put(this.getReadName(), xSpan);
					chiSpanMaps.put(xSpan.getReadName(), this);
				} else {
					chiSpanMaps.put(this.getReadName(), xSpan);
					engSpanMaps.put(xSpan.getReadName(), this);
				}
				String head = this.s.forms[this.hd].toLowerCase();
				HashSet<String> xHeads = headMaps.get(head);
				if (xHeads == null) {
					xHeads = new HashSet<String>();
					headMaps.put(head, xHeads);
				}
				String xHead = xSpan.s.forms[xSpan.hd].toLowerCase();
				xHeads.add(xHead);
			}
		}

	}

	//
	// Maybe we should also consider spans embedding an NE, or an NE embedding a
	// span... not sure. Look at this later.
	private NE getNamedEntity() {
		for (NE ne : s.nes) {
			if (ne.b == start && ne.e == end)
				return ne;
		}
		return null;
	}

	private void assignHeadsEtc() {
		if (s.dt == null) {
			if (start == end)
				hd = start;
			else {
				Node n = s.ct.getExactNode(start, end);
				hd = (n == null ? -1 : n.getHead());
				if (hd > end || hd < start)
					hd = end;
			}
			return;
		}
		// Head
		hd = end;
		int newHd = s.dt.heads[hd];
		while (newHd <= end && newHd >= start) {
			hd = newHd;
			newHd = s.dt.heads[hd];
		}
		// Head Gov
		hdgov = s.dt.heads[hd];
		// Head Lmc
		for (int i = 1; i < hd; ++i) {
			if (s.dt.heads[i] == hd) {
				hdlmc = i;
				break;
			}
		}
		// Head Rmc
		for (int i = s.forms.length - 1; i > hd; --i) {
			if (s.dt.heads[i] == hd) {
				hdrmc = i;
				break;
			}
		}
		// Hd ls
		for (int i = hd - 1; i > 0; --i) {
			if (s.dt.heads[i] == s.dt.heads[hd]) {
				hdls = i;
				break;
			}
		}
		// Hd rs
		for (int i = hd + 1; i < s.forms.length; ++i) {
			if (s.dt.heads[i] == s.dt.heads[hd]) {
				hdrs = i;
				break;
			}
		}
	}

	public int hashCode() {
		return 19 * start + 31 * end + 2 * s.sentenceIndex;
	}

	public boolean equals(Object other) {
		if (other instanceof Span)
			return equals((Span) other);
		else
			return false;
	}

	public boolean equals(Span other) {
		if (other == this)
			return true;
		else if (s != null && other.s == s && other.start == start
				&& other.end == end) {
			Common.bangErrorPOS("! -- two identical spans as separate objects. This shouldn't happend.");
			throw new Error(
					"! -- two identical spans as separate objects. This shouldn't happend.");
		} else
			return false;
	}

	@Override
	public int compareTo(Span other) {
		if (equals(other))
			return 0;
		int senA = this.sid;
		int senB = other.sid;

		if (s != null && other.s != null) {
			senA = s.sentenceIndex;
			senB = other.s.sentenceIndex;
		}
		if (senA < senB) {
			return -1;
		} else if (senB < senA) {
			return 1;
		}
		int begA = start;
		int begB = other.start;
		if (begA < begB) {
			return -1;
		} else if (begB < begA) {
			return 1;
		}
		int endA = end;
		int endB = other.end;
		if (endA < endB) {
			return 1;
		} else {
			return -1;
		}
	}

	public String getKey() {
		String key = s.sentenceIndex + "-" + start + "-" + end;
		return key;
	}

	public int size() {
		return end - start + 1;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i <= end; ++i) {
			sb.append(s.forms[i]).append(" ");
		}
		sb.append('\n');
		if (hd != 1)
			sb.append("Hd\t" + hd + "\t" + s.forms[hd]).append('\n');
		if (hdgov != -1)
			sb.append("Hdgov\t" + hdgov + "\t" + s.forms[hdgov]).append('\n');
		if (hdrmc != -1)
			sb.append(
					"HdRmc\t" + hdrmc + "\t"
							+ (hdrmc == -1 ? "-1" : s.forms[hdrmc])).append(
					'\n');
		if (hdlmc != -1)
			sb.append(
					"HdLmc\t" + hdlmc + "\t"
							+ (hdlmc == -1 ? "-1" : s.forms[hdlmc])).append(
					'\n');
		if (hdrs != -1)
			sb.append(
					"HdRs\t" + hdrs + "\t"
							+ (hdrs == -1 ? "-1" : s.forms[hdrs])).append('\n');
		if (hdls != -1)
			sb.append(
					"HdLs\t" + hdls + "\t"
							+ (hdls == -1 ? "-1" : s.forms[hdls])).append('\n');
		sb.append("NE\t" + "\t" + (ne == null ? "null" : ne.getLabel()));
		return sb.toString();
	}

	public boolean embeds(Span other) {
		return s == other.s && other.start >= start && other.end <= end;
	}

	public boolean isEmbeddedIn(Span other) {
		return other.embeds(this);
	}

	static final int MAX_KEY_PART = (1 << 10);
	static final int MAX_KEY_PART_M1 = MAX_KEY_PART - 1;

	public int getUniqueIntKey() {
		// we have 31 bits to play with, so lets give each number 10 bits.
		// Basically this contrains the number of sentences per document and the
		// number of tokens per sentence to 1023
		if (s.sentenceIndex > MAX_KEY_PART || end > MAX_KEY_PART)
			throw new Error("Sentence index or token index to big!");
		int key = s.sentenceIndex;
		key <<= 10;
		key |= start;
		key <<= 10;
		key |= end;
		return key;
	}
}
