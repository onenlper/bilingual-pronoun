package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class F_Pattern extends AbstractMultiDataDrivenFeature {

	protected F_Pattern(String name, int cutOff) {
		// TODO Auto-generated constructor stub
		super(name, cutOff);
	}

	private static final long serialVersionUID = 1L;
	public static HashMap<String, HashSet<String>> patternCache = new HashMap<String, HashSet<String>>();
	public static HashMap<String, HashSet<String>> pairPatternCache = new HashMap<String, HashSet<String>>();

	public String getCleverExtent(Span sp) {
		StringBuilder sb = new StringBuilder();
		for (int i = sp.start; i <= sp.end; ++i) {
			if (this.skipToken(sp, i))
				continue;
			sb.append(sp.s.forms[i]).append(" ");
		}
		return sb.toString().trim();
	}

	public HashSet<String> getPairPattern(Span sp1, Span sp2) {
		HashSet<String> patterns = new HashSet<String>();
		// container.addAll(getPairPattern(sp1, sp2));
		ArrayList<Atom> atoms1 = this.getAtoms(sp1);
		ArrayList<Atom> atoms2 = this.getAtoms(sp2);
		if (atoms1.size() + atoms2.size() > 9) {
			return patterns;
		}
		// System.out.println(this.getCleverExtent(sp1) + "#" +
		// this.getCleverExtent(sp2));
		ArrayList<String[]> ps1 = this.getPatterns(atoms1);
		ArrayList<String[]> ps2 = this.getPatterns(atoms2);
		HashMap<String, Integer> localCache;
		for (String[] p1 : ps1) {
			for (String[] p2 : ps2) {
				localCache = new HashMap<String, Integer>();
				// form pattern
				StringBuilder sb1 = new StringBuilder();
				for (int k = 0; k < p1.length; k++) {
					String t = p1[k];
					if (t.equalsIgnoreCase(atoms1.get(k).orig)) {
						sb1.append(t).append(" ");
					} else {
						// boost with an index suffix
						int idx = localCache.size();
						sb1.append(t).append(idx).append(" ");
						localCache.put(atoms1.get(k).orig, idx);
					}
				}

				StringBuilder sb2 = new StringBuilder();
				for (int k = 0; k < p2.length; k++) {
					String t = p2[k];
					if (t.equalsIgnoreCase(atoms2.get(k).orig)) {
						sb2.append(t).append(" ");
					} else {
						// boost with an index suffix
						int idx = this.getIndex(atoms2.get(k).orig, localCache);
						sb2.append(t).append(idx).append(" ");
					}
				}
				patterns.add(sb1.toString().trim() + "#" + sb2.toString().trim());
				// System.out.println(sb1.toString().trim() + "#" +
				// sb2.toString().trim());
			}
		}
		// System.out.println("======");
		return patterns;
	}

	private ArrayList<String[]> getPatterns(ArrayList<Atom> atoms) {
		HashSet<String> patterns = new HashSet<String>();
		if (atoms.size() > 0) {
			patterns.addAll(atoms.get(0).patterns);
			for (int i = 1; i < atoms.size(); i++) {
				Atom atom = atoms.get(i);
				HashSet<String> temp = new HashSet<String>();
				for (String pattern : patterns) {
					for (int j = 0; j < atom.patterns.size(); j++) {
						String p = atom.patterns.get(j);
						temp.add(pattern + "###" + p);
					}
				}
				patterns.clear();
				patterns.addAll(temp);
			}
		}
		ArrayList<String[]> ret = new ArrayList<String[]>();
		for (String p : patterns) {
			String[] tokens = p.split("###");
			ret.add(tokens);
		}
		return ret;
	}

	private int getIndex(String str, HashMap<String, Integer> localCache) {
		for (String key : localCache.keySet()) {
			if (str.equals(key)) {
				return localCache.get(key);
			}
			if (str.startsWith(key + " ") || str.endsWith(" " + key)) {
				return localCache.get(key);
			}
			if (key.startsWith(str + " ") || key.endsWith(" " + str)) {
				return localCache.get(key);
			}
		}
		int k = localCache.size();
		localCache.put(str, k);
		return k;
	}

	@Override
	public <T extends Collection<String>> T getFeatureStrings(PairInstance pi, Document d, T container) {
		Span sp1 = pi.ant;
		Span sp2 = pi.ana;
		container.addAll(this.getPairPattern(sp1, sp2));
		return container;
	}

	private String getFixedNE(Span sp, int i) {
		if (sp.s.neCol[i].startsWith("(")) {
			return sp.s.neCol[i];
		} else if (sp.s.neCol[i].startsWith("*")) {
			for (i = i - 1; i >= 0; i--) {
				if (sp.s.neCol[i].endsWith(")")) {
					return "*";
				} else if (sp.s.neCol[i].startsWith("(")) {
					return sp.s.neCol[i];
				}
			}
			return "*";
		} else {
			System.err.println("What is it???" + sp.s.neCol[i]);
			System.exit(1);
			return "";
		}
	}

	private boolean skipToken(Span sp, int i) {
		if (sp.s.forms[i].equals("\"") || sp.s.tags[i].equals("POS") || sp.s.tags[i].equals("DT")
				|| sp.s.forms[i].equals("-") || sp.s.tags[i].equals(":") || sp.s.tags[i].equals(".")
				|| sp.s.tags[i].equals(",") || sp.s.tags[i].equals("RBS")) {
			return true;
		}
		return false;
	}

	private ArrayList<Atom> getAtoms(Span sp) {
		ArrayList<Atom> atoms = new ArrayList<Atom>();
		StringBuilder extent = new StringBuilder();
		for (int i = sp.start; i <= sp.end; i++) {
			if (this.skipToken(sp, i)) {
				continue;
			}
			String ne = getFixedNE(sp, i);
			if (ne.equalsIgnoreCase("*")) {
				Atom atom = new Atom(sp.s.forms[i]);
				// if CC, then add pattern CC
				if (sp.s.tags[i].equalsIgnoreCase("CC")) {
					atom.patterns.add("CC");
				} else if (sp.s.tags[i].startsWith("PRP")) {
					atom.patterns.add("PRP");
				} else if (sp.s.tags[i].equalsIgnoreCase("IN") || sp.s.tags[i].equalsIgnoreCase("TO")) {
					// not add more
					// atom.patterns.add(sp.s.forms[i]);
				} else {
					// TODO, map to semantics
					atom.patterns.add(sp.s.tags[i]);
				}
				atoms.add(atom);
				extent.append(sp.s.forms[i]).append(" ");
			} else if (ne.startsWith("(")) {
				StringBuilder sb = new StringBuilder();
				String NE = ne.substring(1, ne.length() - 1);

				while (!sp.s.neCol[i].endsWith(")")) {
					extent.append(sp.s.forms[i]).append(" ");
					sb.append(sp.s.forms[i]).append(" ");
					i++;
				}
				sb.append(sp.s.forms[i]).append(" ");
				Atom atom = new Atom(sb.toString().trim());
				atom.addPattern(NE);
				atoms.add(atom);
			} else {
				System.err.println("What is it???" + sp.s.neCol[i]);
				System.out.println(extent.toString().trim() + "$" + sp.ne);
				System.exit(1);
			}
		}
		// System.out.println(extent.toString().trim());
		// for (Atom atom : atoms) {
		// System.out.println(atom.orig + ":" +
		// atom.patterns.get(atom.patterns.size()-1));
		// }
		// System.out.println("==========");
		return atoms;
	}
}
