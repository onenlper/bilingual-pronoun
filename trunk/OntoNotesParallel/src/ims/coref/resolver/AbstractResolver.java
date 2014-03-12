package ims.coref.resolver;

import ims.coref.Coref;
import ims.coref.CorefCC;
import ims.coref.Parallel;
import ims.coref.TrainCC;
import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.features.FeatureSet;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.postprocessor.IChainPostProcessor;
import ims.ml.liblinear.LibLinearModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.Common;
import de.bwaldvogel.liblinear.FeatureNode;

public abstract class AbstractResolver implements ICoreferenceResolver {

	public static enum SingleLinkConstraint {
		None, NoTransitiveEmbedding, FirstOnly
	}

	protected final IMarkableExtractor me;
	public LibLinearModel llModel;
	public final FeatureSet fs;
	public final IChainPostProcessor pp;

	public IMarkableExtractor engME;
	public IMarkableExtractor chiME;

	public FeatureSet engFs;
	public FeatureSet chiFs;

	public LibLinearModel engllModel;
	public LibLinearModel chillModel;

	public LibLinearModel chiEnsembleModel;
	public LibLinearModel engEnsembleModel;

	public LibLinearModel xChiModel;
	public LibLinearModel xEngModel;

	public FeatureSet ensembleFS;

	// public final EvaluateMarkables evalMarkables=new EvaluateMarkables();
	// private final GoldStandardChainExtractor gsce=new
	// GoldStandardChainExtractor();
	protected final SingleLinkConstraint slc;

	final int decodeWindow;

	int chainIdCounter = 0;

	protected AbstractResolver(IMarkableExtractor me, LibLinearModel llModel,
			FeatureSet fs, IChainPostProcessor pp, int decodeWindow,
			SingleLinkConstraint slc) {
		this.me = me;
		this.llModel = llModel;
		this.fs = fs;
		this.pp = pp;
		this.decodeWindow = decodeWindow;
		this.slc = slc;
	}

	public static int engDocNumber = 0;
	public static double engOverall = 0;
	public static double engMatch = 0;

	public static int chiDocNumber = 0;
	public static double chiOverall = 0;
	public static double chiMatch = 0;

	public CorefSolution resolve(Document d, String lang) {
		chainIdCounter = 0;
		Set<Span> predSpans;
		if (lang.equalsIgnoreCase("eng")) {
			predSpans = this.engME.extractMarkables(d);
		} else {
			predSpans = this.chiME.extractMarkables(d);
		}

		if (Parallel.turn) {
			double overall = 0;
			double match = 0;
			for (Span s : predSpans) {
				Span xs = s.getXSpan();
				// System.out.println(s.getText() + " # "
				// + (xs == null ? "" : xs.getText()));
				overall++;
				if (lang.equalsIgnoreCase("eng")) {
					engOverall++;
				} else {
					chiOverall++;
				}
				if (xs != null) {
					match++;
					if (lang.equalsIgnoreCase("eng")) {
						engMatch++;
					} else {
						chiMatch++;
					}
				}
			}
			System.out
					.format("%s\t%s\t%d\nOverall: %f,\t Match: %f,\t Percent:%f\n=========\n",
							d.docName, d.lang, engDocNumber++, overall, match,
							match / overall);
		}
		SpanListStruct sls = SpanListStruct.fromCollection(predSpans);
		CorefSolution solution = doResolve(sls, d);

		// cluster smaller nested np for NW folder
		if (lang.equalsIgnoreCase("chi") && d.genre.equals("nw")) {
			for (Span s : predSpans) {
				if (s.miniOne != null) {
					solution.addLink(s, s.miniOne);
				}
			}
		}

		pp.postProcess(solution, d, sls);
		return solution;
	}

	// public CorefSolution resolveChi(Document d) {
	// chainIdCounter = 0;
	// Set<Span> predSpans = this.chiME.extractMarkables(d);
	// if (TrainCC.turn) {
	// double overall = 0;
	// double match = 0;
	// for (Span s : predSpans) {
	// Span xs = s.getXSpan();
	// // System.out.println(s.getText() + " # "
	// // + (xs == null ? "" : xs.getText()));
	// overall++;
	// chiOverall++;
	// if (xs != null) {
	// match++;
	// chiMatch++;
	// }
	// }
	// System.out
	// .format("%s\t%s\t%d\nOverall: %f,\t Match: %f,\t Percent:%f\n=========\n",
	// d.docName, d.lang, chiDocNumber++, overall, match,
	// match / overall);
	// }
	// SpanListStruct sls = SpanListStruct.fromCollection(predSpans);
	//
	// CorefSolution solution = doResolve(sls, d);
	// // cluster smaller nested np for NW folder
	// if (d.genre.equals("nw")) {
	// for (Span s : predSpans) {
	// if (s.miniOne != null) {
	// solution.addLink(s, s.miniOne);
	// }
	// }
	// }
	// pp.postProcess(solution, d, sls);
	// return solution;
	// }

	@Override
	public CorefSolution resolve(Document d) {
		chainIdCounter = 0;
		Set<Span> predSpans = me.extractMarkables(d);
		//
		// List<Span> spans=new ArrayList<Span>(predSpans);
		// Collections.sort(spans);
		SpanListStruct sls = SpanListStruct.fromCollection(predSpans);
		CorefSolution solution = doResolve(sls, d);
		if (d.genre.equals("nw") && d.lang.equals("chi")) {
			for (Span s : predSpans) {
				if (s.miniOne != null) {
					solution.addLink(s, s.miniOne);
				}
			}
		}
		pp.postProcess(solution, d, sls);
		return solution;
	}

	void resolveBestLink(SpanListStruct sls, CorefSolution cs, int anaIndex,
			double th, Document d) {
		switch (slc) {
		case None:
			resolveBestLinkNoConstraint(sls, cs, anaIndex, th, d);
			return;
		case NoTransitiveEmbedding:
			resolveBestLinkNoTransitiveEmbedding(sls, cs, anaIndex, th, d);
			return;
		case FirstOnly:
			resolveBestLinkFirstOnlyConstraint(sls, cs, anaIndex, th, d);
			return;
		default:
			throw new Error("not implemented");
		}
	}

	void resolveClosestFirst(SpanListStruct sls, CorefSolution cs,
			int anaIndex, Document d) {
		switch (slc) {
		case None:
			resolveClosestFirstNoConstraint(sls, cs, anaIndex, d);
			return;
		case NoTransitiveEmbedding:
			resolveClosestFirstNoTransitiveEmbedding(sls, cs, anaIndex, d);
			return;
		case FirstOnly:
			resolveClosestFirstFirstOnlyConstraint(sls, cs, anaIndex, d);
			return;
		default:
			throw new Error("not implemented");
		}
	}

	void resolveClosestFirstNoConstraint(SpanListStruct sls, CorefSolution cs,
			int anaIndex, Document d) {
		Span anaphor = sls.get(anaIndex);
		for (int antIndex = anaIndex - 1, dist = 1; antIndex >= 0
				&& dist < decodeWindow; --antIndex, dist++) {
			Span ant = sls.get(antIndex);
			PairInstance pi = new PairInstance(ant, anaphor,
					sls.getMentionDist(antIndex, anaIndex), sls.getNesBetween(
							antIndex, anaIndex));
			List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
			int cl = llModel.getMostProbableClass(fns);
			if (cl == Coref.POSITIVE) {
				cs.addLink(ant, anaphor);
				return;
			}
		}
	}

	void resolveClosestFirstFirstOnlyConstraint(SpanListStruct sls,
			CorefSolution cs, int anaIndex, Document d) {
		Set<Integer> skip = new HashSet<Integer>();
		Span anaphor = sls.get(anaIndex);
		for (int antIndex = anaIndex - 1, dist = 1; antIndex >= 0
				&& dist < decodeWindow; --antIndex, dist++) {
			Span ant = sls.get(antIndex);
			// Integer antChainID=cs.span2int.get(ant);
			Integer antChainID = cs.getSpanChainID(ant);
			if (antChainID != null)
				if (skip.contains(antChainID))
					continue;
				else
					skip.add(antChainID);
			PairInstance pi = new PairInstance(ant, anaphor,
					sls.getMentionDist(antIndex, anaIndex), sls.getNesBetween(
							antIndex, anaIndex));
			List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
			int cl = llModel.getMostProbableClass(fns);
			if (cl == Coref.POSITIVE) {
				cs.addLink(ant, anaphor);
				return;
			}
		}
	}

	protected ArrayList<String> ilpLines;

	protected ArrayList<String> svmLines;
	protected ArrayList<String> combSvmLines;
	protected ArrayList<String> xSvmLines;

	protected ArrayList<String> svmSpans;
	protected ArrayList<String> xSvmSpans;

	static int qid = 0;

	void addLoneInstance(Span anaphor, Document d) {

		// protected ArrayList<String> svmLines;
		// protected ArrayList<String> combSvmLines;
		// protected ArrayList<String> xSvmLines;
		//
		// protected ArrayList<String> svmSpans;
		// protected ArrayList<String> xSvmSpans;

		Span empty = new Span(true);
		PairInstance pi = new PairInstance(empty, anaphor, true, 0, 0);
		pi.rankExtra = true;

		fs.thisLang = true;
		List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
		fs.thisLang = false;

		String fnsSVM = Common.getSVMRankFormat(fns, 1, qid);
		svmLines.add(fnsSVM);
		svmSpans.add(anaphor.getPosition() + "-" + anaphor.getPosition());

		if (anaphor.getXSpan() != null) {
			fs.xLang = true;
			List<FeatureNode> xFns = fs.getFeatureNodes(pi, d);
			fs.xLang = false;

			String xFnSVM = Common.getSVMRankFormat(xFns, 1, qid);
			xSvmLines.add(xFnSVM);

			this.ensembleFS.bilingual = true;
			List<FeatureNode> allFns = this.ensembleFS.getFeatureNodes(pi, d);
			this.ensembleFS.bilingual = false;

			String allFnsSVM = Common.getSVMRankFormat(allFns, 1, qid);
			combSvmLines.add(allFnsSVM);

			xSvmSpans.add(anaphor.getPosition() + "-" + anaphor.getPosition());
		}
	}

	public static int g = 0;

	void resolveClosestFirstNoTransitiveEmbedding(SpanListStruct sls,
			CorefSolution cs, int anaIndex, Document d) {
		// output classifier probability
		// write the classifier probabilities for three classifiers

		Set<Integer> skip = new HashSet<Integer>(); // store chain ids of spans
													// that embed this anaphor
													// here, so that if we visit
													// other spans from that
													// chain later on, we ignore
													// it.
		Span anaphor = sls.get(anaIndex);
		if (!anaphor.getSinglePronoun() && Parallel.onlyPronoun) {
			return;
		}

		qid++;

		if (anaphor.getSinglePronoun()) {
			if (d.lang.equalsIgnoreCase("chi")) {
				Parallel.chiPros.add(anaphor.getText());
			} else {
				Parallel.engPros.add(anaphor.getText());
			}
		}

		boolean find = false;

		Span sysAnt = null;

		// addLoneInstance(anaphor, d);

		for (int antIndex = anaIndex - 1, dist = 1; antIndex >= 0
				&& dist < decodeWindow; --antIndex, dist++) {
			Span ant = sls.get(antIndex);
			{ // This code skips
				// Integer antChainID = cs.getSpanChainID(ant);
				// if (ant.embeds(anaphor)) {
				// // Integer antChainID=cs.span2int.get(ant);
				// if (antChainID != null)
				// skip.add(antChainID);
				// } else {
				// if (!skip.isEmpty()) {
				// // Integer antChainID=cs.span2int.get(ant);
				// if (antChainID != null && skip.contains(antChainID)) {
				// this.ilpLines.add(ant.getPosition() + "-"
				// + anaphor.getPosition() + "-" + "0" + "\t"
				// + "O");
				// continue;
				// }
				// }
				// }
			}

			PairInstance pi = new PairInstance(ant, anaphor,
					sls.getMentionDist(antIndex, anaIndex), sls.getNesBetween(
							antIndex, anaIndex));

			fs.thisLang = true;
			List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
			fs.thisLang = false;

			String fnsSVM = Common.getSVMRankFormat(fns, 1, qid);
			svmLines.add(fnsSVM);
			svmSpans.add(ant.getPosition() + "-" + anaphor.getPosition());

			double p = llModel.getProbabilityForClass(fns, Coref.POSITIVE);
			this.ilpLines.add(ant.getPosition() + "-" + anaphor.getPosition()
					+ "-" + p + "\t" + "O");

			int cl = Coref.POSITIVE;
			if (p < 0.5) {
				cl = Coref.NEGATIVE;
			}

			// System.out.println(p);

			// TODO
			// protected ArrayList<String> svmLines;
			// protected ArrayList<String> combSvmLines;
			// protected ArrayList<String> xSvmLines;
			//
			// protected ArrayList<String> svmSpans;
			// protected ArrayList<String> xSvmSpans;

			// llModel.putResult(p, pi.getLabel());
			PairInstance xpi = pi.getXInstance();
			if (Parallel.jointTest
					&& xpi != null
					&& (xpi.ant.empty || xpi.ana.s.d.docNo
							.equalsIgnoreCase(xpi.ant.s.d.docNo))) {

				llModel.putResult(p, pi.getLabel());

				// change cl
				// TODO
				this.ensembleFS.bilingual = true;
				List<FeatureNode> allFns = this.ensembleFS.getFeatureNodes(pi,
						d);
				this.ensembleFS.bilingual = false;

				fs.xLang = true;
				List<FeatureNode> xfns = fs.getFeatureNodes(pi, d);
				fs.xLang = false;
				// store training instance
				double xp = llModel.xmodel.getProbabilityForClass(xfns,
						Coref.POSITIVE);
				this.ilpLines.add(ant.getPosition() + "-"
						+ anaphor.getPosition() + "-" + xp + "\t" + "X");
				llModel.xmodel.putResult(xp, pi.getLabel());

				double enP = 0;

				if (Parallel.jointTest) {
					if (d.lang.equalsIgnoreCase("chi")) {
						enP = chiEnsembleModel.getProbabilityForClass(allFns,
								Coref.POSITIVE);
						chiEnsembleModel.putResult(enP, pi.getLabel());

					} else {
						enP = engEnsembleModel.getProbabilityForClass(allFns,
								Coref.POSITIVE);
						engEnsembleModel.putResult(enP, pi.getLabel());
					}
					double avg = (p + xp + enP) / 3;

					if (avg > .5) {
						cl = Coref.POSITIVE;
					} else {
						cl = Coref.NEGATIVE;
					}

					// if(avg>0.5==pi.getLabel() && p>0.5!=pi.getLabel()) {
					if (d.lang.equalsIgnoreCase("chi") && avg>.5) {
						// System.out.println("YES");
						System.out.println(pi.getLabel());
						System.out.println(Boolean.toString(avg > 0.5 == pi
								.getLabel()) + "@@@");
						System.out.println(ant.getText() + "#"
								+ xpi.ant.getText() + "#");
						System.out.println(anaphor.getText() + "#"
								+ xpi.ana.getText() + "#");
						System.out.println(ant.s.toString());
						System.out.println(xpi.ant.s.toString());
						System.out.println(anaphor.s.toString());
						System.out.println(xpi.ana.s.toString());
						System.out.println("==" + (g++) + "==");
					}
				}
				this.ilpLines.add(ant.getPosition() + "-"
						+ anaphor.getPosition() + "-" + enP + "\t" + "E");

				// cl = tune(d, anaphor, pi, p, xp, enP);

				String xFnSVM = Common.getSVMRankFormat(xfns, 1, qid);
				xSvmLines.add(xFnSVM);

				String allFnsSVM = Common.getSVMRankFormat(allFns, 1, qid);
				combSvmLines.add(allFnsSVM);
				xSvmSpans.add(ant.getPosition() + "-" + anaphor.getPosition());
			}
			if (cl == Coref.POSITIVE 
//					&& !find
					) {
				cs.addLink(ant, anaphor);
				find = true;
				sysAnt = ant;
//				break;
			}
		}

		if (anaphor.getSinglePronoun() && sysAnt != null) {

			if (d.lang.equalsIgnoreCase("chi")) {
				CorefCC.chiEvaStat.sys++;
			} else {
				CorefCC.engEvaStat.sys++;
			}

			if (sysAnt != null
					&& d.goldChainMap.containsKey(sysAnt.getReadName())
					&& d.goldChainMap.containsKey(anaphor.getReadName())
					&& d.goldChainMap.get(sysAnt.getReadName()).intValue() == d.goldChainMap
							.get(anaphor.getReadName()).intValue()) {

				if (d.lang.equalsIgnoreCase("chi")) {
					CorefCC.chiEvaStat.hit++;
				} else {
					CorefCC.engEvaStat.hit++;
				}
			}
		}
		return;
	}

	private int tune(Document d, Span anaphor, PairInstance pi, double p,
			double xp, double enP) {
		int cl;
		boolean pB = p - .5 > 0;
		boolean xB = xp - .5 > 0;
		boolean enB = enP - .5 > 0;

		double avg = (p + xp + enP) / 3;
		if (avg > .5) {
			cl = 1;
		} else {
			cl = 0;
		}

		double x1 = pi.ana.alignProb;
		double x2 = pi.ant.alignProb;

		double x = x1 > x2 ? x2 : x1;
		int xI = (int) Math.ceil(x / 0.25);
		// String key = pB + "\t" + oxB + "\t" + xB + "\t" + enB + "\t" + xI +
		// "\t" + anaphor.getText().toLowerCase();
		String key = pB + "\t" + xB + "\t" + enB + "\t"
				+ anaphor.getText().toLowerCase();
		// key = anaphor.getText().toLowerCase();

		// System.out.println(key);

		if ((p - .5) * (xp - .5) < 0) {

			int[] stat = null;
			if (d.lang.equalsIgnoreCase("chi")) {
				stat = Parallel.chiStat.get(key);
				if (stat == null) {
					stat = new int[2];
					Parallel.chiStat.put(key, stat);
				}
			} else {
				stat = Parallel.engStat.get(key);
				if (stat == null) {
					stat = new int[2];
					Parallel.engStat.put(key, stat);
				}
			}
			if (xp - .5 > 0 == pi.getLabel()) {
				stat[0]++;
			} else {
				stat[1]++;
			}

			if (xp - .5 > 0 == pi.getLabel()) {
				// System.out.println(p + " # " + enP);
				// System.out.println(pi.getText());
				// System.out.println(xpi.getText());
				// System.out.println("------------");
			}

			int ok = (int) (p * 10);
			int enk = (int) (enP * 10);

			// String key = ok + "-" + enk;

			if (d.lang.equalsIgnoreCase("chi")
					&& ((pB == true && xB == false && enB == false) || (pB == false
							&& xB == true && enB == true))) {
				// cl = 1 - cl;
			}

			if (d.lang.equalsIgnoreCase("eng")
					&& ((pB == false && xB == true && enB == true))) {
				// cl = 1 - cl;
			}
		}
		return cl;
	}

	// void resolveClosestFirstNoTransitiveEmbedding2(SpanListStruct sls,
	// CorefSolution cs, int anaIndex, Document d) {
	// Set<Integer> skip = new HashSet<Integer>(); // store chain ids of spans
	// // that embed this anaphor
	// // here, so that if we visit
	// // other spans from that
	// // chain later on, we ignore
	// // it.
	// Span anaphor = sls.get(anaIndex);
	// boolean find = false;
	// for (int antIndex = anaIndex - 1, dist = 1; antIndex >= 0
	// && dist < decodeWindow; --antIndex, dist++) {
	// Span ant = sls.get(antIndex);
	// { // This code skips
	// Integer antChainID = cs.getSpanChainID(ant);
	// if (ant.embeds(anaphor)) {
	// // Integer antChainID=cs.span2int.get(ant);
	// if (antChainID != null)
	// skip.add(antChainID);
	// } else {
	// if (!skip.isEmpty()) {
	// // Integer antChainID=cs.span2int.get(ant);
	// if (antChainID != null && skip.contains(antChainID))
	// continue;
	// }
	// }
	// }
	// PairInstance pi = new PairInstance(ant, anaphor,
	// sls.getMentionDist(antIndex, anaIndex), sls.getNesBetween(
	// antIndex, anaIndex));
	//
	// List<String[]> fnsName = new ArrayList<String[]>();
	// List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
	// if (pi.getXInstance() != null) {
	// PairInstance xpi = pi.getXInstance();
	// if (!pi.getName()
	// .equalsIgnoreCase(xpi.getXInstance().getName())) {
	// System.out.println(pi.getName());
	// System.out.println(xpi.getXInstance().getName());
	//
	// System.out.println(xpi.ana.getReadName() + " # "
	// + xpi.ant.getReadName());
	// System.out.println(xpi.ana.getXSpan().getReadName() + " # "
	// + xpi.ant.getXSpan().getReadName());
	// Common.bangErrorPOS("");
	// }
	// // Document xD = xpi.ana.s.d;
	// // List<String[]> xfnsName = new ArrayList<String[]>();
	// // List<FeatureNode> xfns = fs.getFeatureNodes(xpi, xD,
	// // xfnsName);
	// //
	// // System.out.println(fnsName.size() + " # " + xfnsName.size());
	// //
	// // for(int i=0;i<xfns.size()&&i<fns.size();i++) {
	// // String[] fnName = fnsName.get(i);
	// // String[] xfnName = xfnsName.get(i);
	// //
	// // if(fnName[1].equalsIgnoreCase(xfnName[1])) {
	// // System.out.println(fnName[0] + " YYY");
	// // } else {
	// // System.out.println(fnName[0] + " XXXX");
	// // Common.bangErrorPOS(fnName[1] + " # " + xfnName[1]);
	// // System.exit(1);
	// // }
	// // }
	// }
	//
	// int cl = llModel.getMostProbableClass(fns);
	//
	// if (CorefCC.jointTest && CorefCC.jointTestCollect) {
	// double p = llModel.getProbabilityForClass(fns, Coref.POSITIVE);
	// probsMap.put(pi.getName(), p);
	// }
	//
	// if (CorefCC.jointTest && !CorefCC.jointTestCollect) {
	// PairInstance xpi = pi.getXInstance();
	//
	// if (probsMap.containsKey(pi.getName())) {
	// double p = probsMap.get(pi.getName());
	// if (xpi != null
	// && xpi.ana.s.d.docNo
	// .equalsIgnoreCase(xpi.ant.s.d.docNo)) {
	//
	// // TODOO
	// if (probsMap.containsKey(xpi.getName())) {
	//
	// double xp = probsMap.get(xpi.getName());
	// // change cl
	// if ((p - 0.5) * (xp - 0.5) < 0) {
	// // d
	// int newCl = 0;
	// if (pi.getLabel()) {
	// newCl = Coref.POSITIVE;
	// } else {
	// newCl = Coref.NEGATIVE;
	// }
	//
	// String key = d.lang + " " + pi.getLabel() + " "
	// + xpi.getLabel();
	// Integer in = counts.get(key);
	// if (in == null) {
	// counts.put(key, 1);
	// } else {
	// counts.put(key, in.intValue() + 1);
	// }
	//
	// if (cl != newCl) {
	// System.out.println(p + " # " + xp);
	// System.out.println(pi.getText());
	// System.out.println(xpi.getText());
	// System.out.println("------------");
	// }
	//
	// if (pi.getLabel() == xpi.getLabel()) {
	// cl = newCl;
	// }
	//
	// // if (p + xp - 1 > 0) {
	// // cl = Coref.POSITIVE;
	// // } else {
	// // cl = Coref.NEGATIVE;
	// // }
	// // cl = cl*(-1);
	// }
	// }
	// }
	// }
	// }
	//
	// if (cl == Coref.POSITIVE && !find && !CorefCC.jointTestCollect) {
	// cs.addLink(ant, anaphor);
	// find = true;
	// }
	// }
	// return;
	// }

	public static HashMap<String, Integer> counts = new HashMap<String, Integer>();

	void resolveBestLinkFirstOnlyConstraint(SpanListStruct sls,
			CorefSolution cs, int anaIndex, double th, Document d) {
		Set<Integer> skip = new HashSet<Integer>();
		Span anaphor = sls.get(anaIndex);
		Span bestSpan = null;
		double bestScore = -1;
		for (int antIndex = anaIndex - 1, dist = 1; antIndex >= 0
				&& dist < decodeWindow; --antIndex, ++dist) {
			Span ant = sls.get(antIndex);
			// Integer antChainID=cs.span2int.get(ant);
			Integer antChainID = cs.getSpanChainID(ant);
			if (antChainID != null) {
				if (skip.contains(antChainID))
					continue;
				else
					skip.add(antChainID);
			}
			PairInstance pi = new PairInstance(ant, anaphor,
					sls.getMentionDist(antIndex, anaIndex), sls.getNesBetween(
							antIndex, anaIndex));
			List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
			double p = llModel.getProbabilityForClass(fns, Coref.POSITIVE);
			if (p >= th && p > bestScore) {
				bestScore = p;
				bestSpan = ant;
			}
		}
		if (bestSpan != null) {
			cs.addLink(bestSpan, anaphor);
		}
	}

	public static HashMap<String, Double> probsMap = new HashMap<String, Double>();

	void resolveBestLinkNoTransitiveEmbedding(SpanListStruct sls,
			CorefSolution cs, int anaIndex, double th, Document d) {
		Set<Integer> skip = new HashSet<Integer>();
		Span anaphor = sls.get(anaIndex);
		Span bestSpan = null;
		double bestScore = -1;
		for (int antIndex = anaIndex - 1, dist = 1; antIndex >= 0
				&& dist < decodeWindow; --antIndex, ++dist) {
			Span ant = sls.get(antIndex);
			{ // This code skips -- copy and paste from above
				Integer antChainID = cs.getSpanChainID(ant);
				if (ant.embeds(anaphor)) {
					if (antChainID != null)
						skip.add(antChainID);
				} else {
					if (!skip.isEmpty()) {
						if (antChainID != null && skip.contains(antChainID))
							continue;
					}
				}
			}
			PairInstance pi = new PairInstance(ant, anaphor,
					sls.getMentionDist(antIndex, anaIndex), sls.getNesBetween(
							antIndex, anaIndex));
			List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
			double p = llModel.getProbabilityForClass(fns, Coref.POSITIVE);
			if (p >= th && p > bestScore) {
				bestScore = p;
				bestSpan = ant;
			}
		}
		if (bestSpan != null) {
			cs.addLink(bestSpan, anaphor);
		}
	}

	void resolveBestLinkNoConstraint(SpanListStruct sls, CorefSolution cs,
			int anaIndex, double th, Document d) {
		Span anaphor = sls.get(anaIndex);
		Span bestSpan = null;
		double bestScore = -1;
		for (int antIndex = anaIndex - 1, dist = 1; antIndex >= 0
				&& dist < decodeWindow; --antIndex, ++dist) {
			Span ant = sls.get(antIndex);
			PairInstance pi = new PairInstance(ant, anaphor,
					sls.getMentionDist(antIndex, anaIndex), sls.getNesBetween(
							antIndex, anaIndex));
			List<FeatureNode> fns = fs.getFeatureNodes(pi, d);
			double p = llModel.getProbabilityForClass(fns, Coref.POSITIVE);
			if (p >= th && p > bestScore) {
				bestScore = p;
				bestSpan = ant;
			}
		}
		if (bestSpan != null) {
			cs.addLink(bestSpan, anaphor);
		}
	}

	public String toString() {
		return this.getClass().getCanonicalName();
	}

	// abstract CorefSolution doResolve(List<Span> spans,Document d);
	abstract CorefSolution doResolve(SpanListStruct sls, Document d);

	@Override
	public IChainPostProcessor getPostProcessor() {
		return pp;
	}
}
