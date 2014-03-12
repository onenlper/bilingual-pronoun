package ims.coref.resolver;

import ims.coref.Parallel;
import ims.coref.data.Chain;
import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.Span;
import ims.coref.data.SpanListStruct;
import ims.coref.features.FeatureSet;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.postprocessor.IChainPostProcessor;
import ims.ml.liblinear.LibLinearModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import util.Common;

public class ClosestFirstResolver extends AbstractResolver {

	public ClosestFirstResolver(IMarkableExtractor me, LibLinearModel llModel,
			FeatureSet fs, IChainPostProcessor pp, int decodeWindow,
			SingleLinkConstraint slc) {
		super(me, llModel, fs, pp, decodeWindow, slc);
	}

	public CorefSolution doResolve(SpanListStruct sls, Document d) {

		this.ilpLines = new ArrayList<String>();

		this.svmLines = new ArrayList<String>();
		this.combSvmLines = new ArrayList<String>();
		this.svmSpans = new ArrayList<String>();
		
		this.xSvmLines = new ArrayList<String>();
		this.xSvmSpans = new ArrayList<String>();
		
		AbstractResolver.qid = 0;
		
		for (int anaIndex = 0; anaIndex < sls.size(); anaIndex++) {
			Span span = sls.get(anaIndex);
			StringBuilder sb = new StringBuilder();
			sb.append(span.getPosition()).append(" ");
			double xConf = 1;
			if (span.getXSpan() != null) {
				xConf = span.alignProb;
			}
			if (span.getSinglePronoun()) {
				sb.append("P");
			} else {
				sb.append("N");
			}
			sb.append("@").append(xConf);
			this.ilpLines.add(sb.toString().trim());
		}
		this.ilpLines.add("####");

		CorefSolution solution = new CorefSolution();
		for (int anaIndex = 1; anaIndex < sls.size(); ++anaIndex)
			resolveClosestFirst(sls, solution, anaIndex, d);

		// System.out.println("OUT???");

		if (Span.CC) {
			String path = "/users/yzcchen/chen3/ijcnlp2013/ilp/" + d.lang
					+ Parallel.part + "/";
			if (!(new File(path)).exists()) {
				(new File(path)).mkdir();
			}
			Common.outputLines(this.ilpLines,
					path + "/" + d.docName.replace("/", "-") + "." + d.docNo
							+ ".ilp");

			//TODO skip print gold
			outputGoldCR(sls, d, path);
			
			
			String rankPath =  "/users/yzcchen/chen3/ijcnlp2013/ranker/" + d.lang
					+ Parallel.part + "/";
			if (!(new File(rankPath)).exists()) {
				(new File(rankPath)).mkdir();
			}
			
			Common.outputLines(this.svmLines, rankPath + d.docName.replace("/", "-") + "." + d.docNo
							+ ".testO");
			
			Common.outputLines(this.xSvmLines, rankPath + d.docName.replace("/", "-") + "." + d.docNo
					+ ".testX");
			
			Common.outputLines(this.combSvmLines, rankPath + d.docName.replace("/", "-") + "." + d.docNo
					+ ".testE");
			
			Common.outputLines(this.svmSpans, rankPath + d.docName.replace("/", "-") + "." + d.docNo
					+ ".spansO");
			
			Common.outputLines(this.xSvmSpans, rankPath + d.docName.replace("/", "-") + "." + d.docNo
					+ ".spansX");
		}

		return solution;
	}

	private void outputGoldCR(SpanListStruct sls, Document d, String path) {
		// output goldchain
		Chain[] goldChains = d.goldChains;
		// output spans, indicate if pronoun
		ArrayList<String> goldInfo = new ArrayList<String>();

		for (Chain chain : goldChains) {
			for (Span s : chain.spans) {
				StringBuilder sb = new StringBuilder();
				sb.append(s.s.sentenceIndex).append(":").append(s.start)
						.append(",").append(s.end).append(" ");
				if ((s.isPronoun && !Parallel.testzero) || (Parallel.testzero && s.getSinglePronoun()) ) {
					sb.append("P");
				} else {
					sb.append("N");
				}
				goldInfo.add(sb.toString());
			}
		}

		for (Span s : d.deletedSpans) {
			if (!d.goldChainMap.containsKey(s)) {
				if(!Parallel.testzero) {
					goldInfo.add(s.getPosition() + " P");
				}
			}
		}

		for (int anaIndex = 0; anaIndex < sls.size(); anaIndex++) {
			Span span = sls.get(anaIndex);
			if (span.getSinglePronoun()
					&& !d.goldChainMap.containsKey(span.getReadName())) {
				goldInfo.add(span.getPosition() + " P");
			}
		}

		goldInfo.add("####");
		for (Chain chain : goldChains) {
			StringBuilder sb = new StringBuilder();
			Collections.sort(chain.spans);
			for (Span s : chain.spans) {
				sb.append(s.getPosition()).append(" ");
				// sb.append(s.getText()).append(" # ");
			}
			goldInfo.add(sb.toString().trim());
		}

		Common.outputLines(goldInfo, path + "/" + d.docName.replace("/", "-")
				+ "." + d.docNo + ".gold");
	}

	public String toString() {
		return this.getClass().getCanonicalName() + "  (SLC: " + slc + ")";
	}
}
