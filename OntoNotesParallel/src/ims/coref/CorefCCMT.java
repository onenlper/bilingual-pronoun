package ims.coref;

import ims.coref.data.Chain;
import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.Span;
import ims.coref.io.DocumentReader;
import ims.coref.io.DocumentWriter;
import ims.coref.io.ReaderWriterFactory;
import ims.coref.lang.Language;
import ims.coref.resolver.AbstractResolver;
import ims.coref.util.ModelReaderWriter;
import ims.ml.liblinear.InstanceSink;
import ims.ml.liblinear.LibLinearInMemorySink;
import ims.ml.liblinear.LibLinearModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import util.Common;

import align.DocumentMap;

public class CorefCCMT {

	private static final int POSITIVE = 1;
	private static final int NEGATIVE = 0;

	private static int docCount = 0;
	private static int senCount = 0;
	private static int chainCount = 0;
	private static int mentionCount = 0;


	private static InstanceSink chiEnsembleIS;
	private static InstanceSink engEnsembleIS;


	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		Span.CC = true;
		Options options = new Options(args);
		Parallel.part = options.model.getAbsolutePath().charAt(
				options.model.getAbsolutePath().length() - 1);
		// load giza output
		// DocumentMap.loadRealGizaAlignResult(util.Util.tokenBAAlignBaseSys
		// + "/align/");
//		DocumentMap.loadRealBAAlignResult(util.Util.tokenBAAlignBaseSys
//				+ "/align_/");

//		DocumentMap.loadRealBAAlignResult("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/");
		
		String srclang = options.sourcelang;
//		DocumentMap.loadRealBAAlignResult("/users/yzcchen/chen3/ijcnlp2013/googleMTRED/" + srclang +"_MT/align/");
		DocumentMap.loadRealBAAlignResult("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/" + srclang +"_MT/align/");
		if (options.develop) {
			Parallel.dev = true;
			chiEnsembleIS = new LibLinearInMemorySink(
					LibLinearInMemorySink.DEFAULT_Ensem_PARAMETER, options.bias);
			engEnsembleIS = new LibLinearInMemorySink(
					LibLinearInMemorySink.DEFAULT_Ensem_PARAMETER, options.bias);

			// new File("ensemble."
			// + options.model.getName().substring(0,
			// options.model.getName().length() - 1));
			//
			// chiEnsembleIS = new
			// LibLinearOnDiskSink(Util.getWriter(options.onDiskTrainingFile));
			// engEnsembleIS = new LibLinearOnDiskSink(Util.getWriter(
			// options.onDiskTrainingFile));
		}

		final AbstractResolver resolver;
		// if (options.stacked)
		// resolver = ModelReaderWriter.loadStackedSolver(options);
		// else
		resolver = ModelReaderWriter.loadFullJointResolver(options);
		System.out.println("Using resolver: " + resolver.toString());
		System.out.println("Using postprocessor: "
				+ resolver.getPostProcessor().getClass().getCanonicalName());

		DocumentReader engReader = ReaderWriterFactory.getReader(
				options.inputFormat, options.engInput);
		DocumentWriter engWriter = ReaderWriterFactory.getWriter(
				options.outputFormat, options.engOut);

		DocumentReader chiReader = ReaderWriterFactory.getReader(
				options.inputFormat, options.chiInput);
		DocumentWriter chiWriter = ReaderWriterFactory.getWriter(
				options.outputFormat, options.chiOut);

		Language.initLanguage("eng");
		HashSet<Span> engSpans = new HashSet<Span>();
		HashSet<Span> chiSpans = new HashSet<Span>();
		for (Document d : engReader) {
			// need to extract spans
			engSpans.addAll(resolver.engME.extractMarkables(d));
		}
		Language.initLanguage("chi");
		for (Document d : chiReader) {
			// need to extract spans
			chiSpans.addAll(resolver.chiME.extractMarkables(d));
		}

		// read once
		for (int i = 1; i <= 4; i++) {
			Span.assignMode = i;
			Language.initLanguage("eng");
			for (Span s : engSpans) {
				s.getXSpan();
			}
			Language.initLanguage("chi");
			for (Span s : chiSpans) {
				s.getXSpan();
			}
		}
		Span.assignMode = 5;

		Parallel.turn = true;

		Eva engEva = new Eva();
		Eva chiEva = new Eva();

		if(srclang.equalsIgnoreCase("eng")) {
			resolve(options, resolver, engReader, engWriter, "eng", engEva);
		}
		if(srclang.equalsIgnoreCase("chi")) {
			resolve(options, resolver, chiReader, chiWriter, "chi", chiEva);
		}
		Parallel.turn = false;
		options.done();
		System.out.println("Documents: " + docCount);
		System.out.println("Sentences: " + senCount);
		System.out.println();
		System.out.println("Chains:   " + chainCount);
		System.out.println("Mentions: " + mentionCount);
		System.out.println();
		System.out.println(resolver.getPostProcessor().toString());
		System.out.println();

		System.out.println("ENG: " + AbstractResolver.engMatch
				/ AbstractResolver.engOverall);
		System.out.println("CHI: " + AbstractResolver.chiMatch
				/ AbstractResolver.chiOverall);

		for (String key : AbstractResolver.counts.keySet()) {
			System.out.println(key + " # " + AbstractResolver.counts.get(key));
		}

		// train ensemble
		if (Parallel.dev) {
			LibLinearModel chiEnsembleModel = chiEnsembleIS.train();
			LibLinearModel engEnsembleModel = engEnsembleIS.train();
			// ModelReaderWriter.saveEnsemble(
			// new File("ensemble."
			// + options.model.getName().substring(0,
			// options.model.getName().length() - 1)),
			// chiEnsembleModel, engEnsembleModel, resolver.fs);
			System.out.println("Pos:\t" + ensemblePos);
			System.out.println("Neg:\t" + ensembleNeg);

		} else {
			resolver.chillModel.printStat("CHI MODEL");
			resolver.chillModel.xmodel.printStat("xCHI MODEL");
			resolver.chiEnsembleModel.printStat("enCHI MODEL");

			resolver.engllModel.printStat("ENG MODEL");
			resolver.engllModel.xmodel.printStat("xENG MODEL");
			resolver.engEnsembleModel.printStat("enENG MODEL");

			// System.out.println("ALL:\t" + ensembleAll);
			// System.out.println("Cor:\t" + ensembleCorrect);
			// System.out.println("Pre:\t" + ensembleCorrect/ensembleAll);
			//
			// System.out.println("Base:\t" + ensembleGoldPos/ensembleAll);
			//
			// System.out.println("========");
			// System.out.println("Positive:");
			// System.out.println("GoldPos:\t" + ensembleGoldPos);
			// System.out.println("SysPos: \t" + ensembleSysPos);
			// System.out.println("HitPos: \t" + ensembleHitPos);
			// System.out.println("---");
			// System.out.println("Rec: \t" + ensembleHitPos/ensembleGoldPos);
			// System.out.println("Prec: \t" + ensembleHitPos/ensembleSysPos);
			//
			//
			// System.out.println("========");
			// System.out.println("Negative:");
			// System.out.println("GoldNeg:\t" + ensembleGoldNeg);
			// System.out.println("SysNeg: \t" + ensembleSysNeg);
			// System.out.println("HitNeg: \t" + ensembleHitNeg);
			// System.out.println("---");
			// System.out.println("Rec: \t" + ensembleHitNeg/ensembleGoldNeg);
			// System.out.println("Prec: \t" + ensembleHitNeg/ensembleSysNeg);
		}
		engEva.printStat("ENG PRONOUN:");
		chiEva.printStat("CHI PRONOUN:");

		System.out.println("=================================");

		engEvaStat.printStat("ENG LINK:");
		chiEvaStat.printStat("CHI LINK:");

		System.out.println(CorefCCMT.right);
		System.out.println(CorefCCMT.wrong);

		System.out.println(Parallel.chiPros);
		System.out.println(Parallel.engPros);

		System.out.println("English: ");
		StringBuilder engSbX = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				String key = i + "-" + j;
				int[] stat = Parallel.engStat.get(key);
				if (stat != null) {
					System.out.println(key + "\t" + stat[0] + "\t" + stat[1]
							+ "\t" + ((double) stat[0]) / ((double) stat[1]));
					double pre = ((double) stat[0]) / ((double) stat[1]);
					if (pre > 0.5) {
						engSbX.append(key).append(",");
					}
				}
				Parallel.engStat.remove(key);
			}
		}
		
		ArrayList<String> engKeys = new ArrayList<String>(Parallel.engStat.keySet());
		Collections.sort(engKeys);
		
		for (String key : engKeys) {
			int[] stat = Parallel.engStat.get(key);
			System.out.println(key + "\t" + stat[0] + "\t" + stat[1] + "\t"
					+ ((double) stat[0]) / ((double) stat[1]));
		}
		System.out.println(engSbX.toString().trim());
		System.out.println("Chi: ");
		StringBuilder chiSbX = new StringBuilder();
		StringBuilder chiSbO = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				String key = i + "-" + j;
				int[] stat = Parallel.chiStat.get(key);
				if (stat != null) {
					System.out.println(key + "\t" + stat[0] + "\t" + stat[1]
							+ "\t" + ((double) stat[0]) / ((double) stat[1]));
					double pre = ((double) stat[0]) / ((double) stat[1]);
					if (pre > 0.5) {
						chiSbX.append(key).append(",");
					}
				}
				Parallel.chiStat.remove(key);
			}
		}

		ArrayList<String> chiKeys = new ArrayList<String>(Parallel.chiStat.keySet());
		Collections.sort(chiKeys);
		
		for (String key : chiKeys) {
			int[] stat = Parallel.chiStat.get(key);
			System.out.println(key + "\t" + stat[0] + "\t" + stat[1] + "\t"
					+ ((double) stat[0]) / ((double) stat[1]));
		}

		System.out.println(chiSbX.toString().trim());
		
//		Common.outputHashSet(Span.mappedChiMs, "MTTestMapChiMention");
	}

	private static double ensemblePos = 0;
	private static double ensembleNeg = 0;

	private static double ensembleAll = 0;
	private static double ensembleCorrect = 0;

	private static double ensembleGoldPos = 0;
	private static double ensembleSysPos = 0;
	private static double ensembleHitPos = 0;

	private static double ensembleGoldNeg = 0;
	private static double ensembleSysNeg = 0;
	private static double ensembleHitNeg = 0;

	private static void resolve(Options options,
			final AbstractResolver resolver, DocumentReader reader,
			DocumentWriter writer, String lang, Eva eva) throws IOException {
		Language.initLanguage(lang);
		if (lang.equalsIgnoreCase("eng")) {
			resolver.llModel = resolver.engllModel;
		} else {
			resolver.llModel = resolver.chillModel;
		}
		Iterator<Document> engDocIt;
		engDocIt = reader.iterator();
		// for(Document d:reader){
		while (engDocIt.hasNext()) {
			Document d = engDocIt.next();
			if (docCount % 80 == 0) {
				System.out.println("ENG: " + d.docName + " # " + docCount);
			}
			if (!options.useGoldMarkableExtractor)
				d.clearCorefCols();
			docCount++;
			senCount += d.sen.size();
			CorefSolution cs = resolver.resolve(d, lang);
			chainCount += cs.getChainCount();
			mentionCount += cs.getMentionCount();
			if (options.useGoldMarkableExtractor)
				d.clearCorefCols();
			d.setCorefCols(cs.getKey());
			writer.write(d);

			evaPronounCoref(d, cs, eva);
		}
		writer.close();
	}

	public static Eva chiEvaStat = new Eva();
	public static Eva engEvaStat = new Eva();

	public static int right = 0;
	public static int wrong = 0;

	private static void evaPronounCoref(Document d, CorefSolution cs, Eva eva) {
		// TODO
		HashMap<Span, HashSet<Span>> pronounMap = new HashMap<Span, HashSet<Span>>();

		for (Chain c : d.goldChains) {
			Collections.sort(c.spans);
			for (int i = 1; i < c.spans.size(); i++) {
				Span s = c.spans.get(i);
				if (s.getSinglePronoun()) {
					HashSet<Span> spans = new HashSet<Span>(c.spans.subList(0,
							i));
					pronounMap.put(s, spans);
					eva.gold++;
				}
			}
		}

		for (Chain c : cs.getKey()) {
			Collections.sort(c.spans);
			for (int i = 1; i < c.spans.size(); i++) {
				Span s = c.spans.get(i);
				if (s.getSinglePronoun()) {
					eva.sys++;
					Span ana = c.spans.get(i - 1);

					HashSet<Span> anas = pronounMap.get(s);

					if (anas != null && anas.contains(ana)) {
						eva.hit++;
					}
				}
			}
		}

	}

	// private static void chiResolve(Options options,
	// final AbstractResolver resolver, DocumentReader chiReader,
	// DocumentWriter chiWriter) throws IOException {
	// Language.initLanguage("chi");
	// resolver.llModel = resolver.chillModel;
	// Iterator<Document> chiDocIt;
	// chiDocIt = chiReader.iterator();
	// // for(Document d:reader){
	// while (chiDocIt.hasNext()) {
	// Document d = chiDocIt.next();
	// if (docCount % 80 == 0) {
	// System.out.println("CHI: " + d.docName + " # " + docCount);
	// }
	// if (!options.useGoldMarkableExtractor)
	// d.clearCorefCols();
	// docCount++;
	// senCount += d.sen.size();
	// CorefSolution cs = resolver.resolveChi(d);
	// chainCount += cs.getChainCount();
	// mentionCount += cs.getMentionCount();
	// if (options.useGoldMarkableExtractor)
	// d.clearCorefCols();
	// d.setCorefCols(cs.getKey());
	// chiWriter.write(d);
	// }
	// chiWriter.close();
	// }
}
