package ims.coref;

import ims.coref.data.Chain;
import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;
import ims.coref.io.DocumentReader;
import ims.coref.io.ReaderWriterFactory;
import ims.coref.lang.Language;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.markables.MarkableExtractorFactory;
import ims.coref.training.GoldStandardChainExtractor;
import ims.coref.training.ITrainingExampleExtractor;
import ims.coref.training.TrainingExampleExtractorFactory;
import ims.coref.util.ModelReaderWriter;
import ims.ml.liblinear.InstanceSink;
import ims.ml.liblinear.LibLinearInMemorySink;
import ims.ml.liblinear.LibLinearModel;
import ims.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import util.Common;
import align.DocumentMap;
import de.bwaldvogel.liblinear.FeatureNode;

public class TrainCCMTZeroAll {

	private static int docCount = 0;
	private static int senCount = 0;
	private static int trainingInstanceCount = 0;
	private static int positiveCount = 0;

	private static InstanceSink chiEnsembleIS;
	private static InstanceSink engEnsembleIS;

	private static InstanceSink xChiIS;
	private static InstanceSink xEngIS;

	private static double ensemblePos = 0;
	private static double ensembleNeg = 0;

	private static ArrayList<String> chiSVMs = new ArrayList<String>();
	private static ArrayList<String> engSVMs = new ArrayList<String>();

	private static ArrayList<String> chiEnsSVMs = new ArrayList<String>();
	private static ArrayList<String> engEnsSVMs = new ArrayList<String>();

	static String engAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/eng_MT/align/";
	static String chiAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/chi_MT/align/";
	static String engDoc = "engCoNLL.train.1";
	static String mtEngDoc = "MT.engCoNLL.train.1";
	static String chiDoc = "chiCoNLL.train.1";
	static String mtChiDoc = "MT.chiCoNLL.train.1";

	static void v11() {
		engAlign = "/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/";
		chiAlign = "/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/";
		engDoc = "engCoNLL.train.1";
		mtEngDoc = "chiCoNLL.train.1";
		chiDoc = "chiCoNLL.train.1";
		mtChiDoc = "engCoNLL.train.1";
	}

	static void v13() {
		engAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/eng_MT/align/";
		chiAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/chi_MT/align/";
		engDoc = "engCoNLL.train.1";
		mtEngDoc = "MT.engCoNLL.train.1";
		chiDoc = "chiCoNLL.train.1";
		mtChiDoc = "MT.chiCoNLL.train.1";
	}

	static void v14() {
		engAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTALL/eng_MT/align/";
		chiAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/";
		engDoc = "engCoNLL.train.0";
		mtEngDoc = "MT.engCoNLL.train.0";
		chiDoc = "chiCoNLL.train.0";
		mtChiDoc = "MT.chiCoNLL.train.0";

		Parallel.filter = true;
		ArrayList<String> files = Common.getLines("parallelMap.train.1");
		for (String file : files) {
			String tks[] = file.split("#");
			Parallel.accessFiles.add(tks[0].trim() + "_chi");
			Parallel.accessFiles.add(tks[1].trim() + "_eng");
		}
	}

	static void v15() {
		engAlign = "/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/";
		chiAlign = "/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/";
		engDoc = "engCoNLL.train.0";
		mtEngDoc = "chiCoNLL.train.1";
		chiDoc = "chiCoNLL.train.0";
		mtChiDoc = "engCoNLL.train.1";
	}

	static void loadAllOrig_PartTrainMT() {
		engAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/eng_MT/align/";
		chiAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/chi_MT/align/";
		engDoc = "engCoNLL.train.0";
		mtEngDoc = "MT.engCoNLL.train.1";
		chiDoc = "chiCoNLL.train.0";
		mtChiDoc = "MT.chiCoNLL.train.1";
	}

	static void v20() {
		engAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTALL/eng_MT/align/";
		chiAlign = "/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/";
		engDoc = "engCoNLL.train.0";
		mtEngDoc = "MT.engCoNLL.train.0";
		chiDoc = "chiCoNLL.train.0";
		mtChiDoc = "MT.chiCoNLL.train.0";

	}

	public static void main(String[] args) throws IOException {
		Options options = new Options(args);
		// load giza output
		Span.CC = true;
		Parallel.zero = true;
		v15();
//		loadAllOrig_AllTrainMT();
//		loadAllOrig_PartTrainMT();
//		loadAllOrig_PartTrainParallel();
//		loadPartOrig_PartTrainMT();
//		loadPartOrig_PartTrainParallel();
//		loadAllOrig_PartTrainParallel();
		// loadPartOrig_AllTrainMT();
		// DocumentMap
		// .loadRealBAAlignResult("/users/yzcchen/chen3/ijcnlp2013/googleMTRED/"
		// + srclang + "_MT/align/");

		if (Parallel.ensemble) {
			chiEnsembleIS = new LibLinearInMemorySink(
					LibLinearInMemorySink.DEFAULT_S7_PARAMETER, options.bias);
			engEnsembleIS = new LibLinearInMemorySink(
					LibLinearInMemorySink.DEFAULT_S7_PARAMETER, options.bias);

			xChiIS = new LibLinearInMemorySink(
					LibLinearInMemorySink.DEFAULT_S7_PARAMETER, options.bias);
			;
			xEngIS = new LibLinearInMemorySink(
					LibLinearInMemorySink.DEFAULT_S7_PARAMETER, options.bias);
			;
		}
		GoldStandardChainExtractor gsce = new GoldStandardChainExtractor();
		final FeatureSet engFs = getFeatureSet(options,
				Language.initLanguage("eng"));
		final FeatureSet chiFs = getFeatureSet(options,
				Language.initLanguage("chi"));
		final FeatureSet FS = FeatureSet.concat(engFs, chiFs);
		// Initialize English
		Language.initLanguage("eng");
		final IMarkableExtractor engME = MarkableExtractorFactory
				.getExtractorS(options.engMarkableExtractors == null ? Language
						.getLanguage().getDefaultMarkableExtractors()
						: options.engMarkableExtractors);

		final ITrainingExampleExtractor engTeex = TrainingExampleExtractorFactory
				.getExtractor(options.trainingExampleExtractor, engME,
						options.commitStrategy);
		System.out.println("Using training example extractor: "
				+ engTeex.getClass().getCanonicalName());
		// Initialize Chinese
		Language.initLanguage("chi");
		final IMarkableExtractor chiME = MarkableExtractorFactory
				.getExtractorS(options.markableExtractors == null ? Language
						.getLanguage().getDefaultMarkableExtractors()
						: options.markableExtractors);

		final ITrainingExampleExtractor chiTeex = TrainingExampleExtractorFactory
				.getExtractor(options.trainingExampleExtractor, chiME,
						options.commitStrategy);
		System.out.println("Using training example extractor: "
				+ chiTeex.getClass().getCanonicalName());

		// load English
		DocumentMap.loadRealBAAlignResult(engAlign);
		Language.initLanguage("eng");
		DocumentReader engReader = getDocReader(options, engME,
				new File(engDoc), false);
		HashSet<Span> engSpans = loadSpans(gsce, engME, engReader);
		// load translated English
		Language.initLanguage("chi");
		Document.MTDoc = true;
		DocumentReader MTEngReader = getDocReader(options, chiME, new File(
				mtEngDoc), true);
		HashSet<Span> MTEngSpans = loadSpans(gsce, chiME, MTEngReader);
		Document.MTDoc = false;
		for (int i = 1; i <= 4; i++) {
			Span.assignMode = i;
			for (Span s : engSpans) {
				s.getXSpan();
			}
			for (Span s : MTEngSpans) {
				s.getXSpan();
			}
		}
		DocumentMap.clear();

		// load Chinese
		DocumentMap.loadRealBAAlignResult(chiAlign);
		Language.initLanguage("chi");
		DocumentReader chiReader = getDocReader(options, chiME,
				new File(chiDoc), false);
		HashSet<Span> chiSpans = loadSpans(gsce, chiME, chiReader);
		Language.initLanguage("eng");
		Document.MTDoc = true;
		DocumentReader MTChiReader = getDocReader(options, engME, new File(
				mtChiDoc), true);
		HashSet<Span> MTChiSpans = loadSpans(gsce, engME, MTChiReader);
		Document.MTDoc = false;
		// read once
		for (int i = 1; i <= 4; i++) {
			Span.assignMode = i;
			for (Span s : chiSpans) {
				s.getXSpan();
			}
			for (Span s : MTChiSpans) {
				s.getXSpan();
			}
		}
		Span.assignMode = 5;
		DocumentMap.clear();
		// read once
		Language.initLanguage("chi");
		// for (int i = 1; i <= 4; i++) {
		// Span.assignMode = i;
		// for (Span s : engSpans) {
		// s.getXSpan();
		// }
		// for (Span s : chiSpans) {
		// s.getXSpan();
		// }
		// }
		// Span.assignMode = 5;

		Parallel.turn = true;
		System.out.println("Registering features");
		FS.bilingual = true;
		FS.jointRegisterAll(engReader, chiReader, engTeex, chiTeex);
		FS.bilingual = false;
		Parallel.turn = false;
		System.out.println("Done. Using features:");
		for (IFeature f : FS.getFeatures())
			System.out.println(f.toString());
		System.out.println();
		System.out.println("Size of feature space: "
				+ Util.insertCommas(FS.getSizeOfFeatureSpace()));

		System.out.println("Creating training instances");
		// EvaluateMarkables evalMark=new EvaluateMarkables();

		Collections.sort(Parallel.chiSortedDocs);
		Collections.sort(Parallel.engSortedDocs);

		System.out.println("ENG: " + Parallel.engMatch / Parallel.engOverall);
		System.out.println("CHI: " + Parallel.chiMatch / Parallel.chiOverall);
		System.out.println("Match: ");
		System.out.println("ENG: " + Span.chiSpanMaps.size());
		System.out.println("CHI: " + Span.engSpanMaps.size());

		System.out.println("ENG: " + engSpans.size());
		System.out.println("CHI: " + chiSpans.size());

		System.out.println("ENG: " + Parallel.engSpanMount);
		System.out.println("CHI: " + Parallel.chiSpanMount);

		if (options.joint.equalsIgnoreCase("oneM")) {
			// train english
			final InstanceSink jointIS = new LibLinearInMemorySink(
					options.libLinearParameter, options.bias);
			langTrain(FS, engTeex, engReader, jointIS, "eng");
			langTrain(FS, chiTeex, chiReader, jointIS, "chi");
			jointIS.close();

			printStat();
			// System.out.println("Markable extraction: ");
			// System.out.println(evalMark.toString());
			LibLinearModel llModel = jointIS.train();
			saveModel(options, engFs, engME, llModel, chiFs, chiME, llModel);
		} else if (options.joint.equalsIgnoreCase("twoM")) {
			// train english
			final InstanceSink engIS = new LibLinearInMemorySink(
					options.libLinearParameter, options.bias);
			final InstanceSink chiIS = new LibLinearInMemorySink(
					options.libLinearParameter, options.bias);
			langTrain(FS, engTeex, engReader, engIS, "eng");
			langTrain(FS, chiTeex, chiReader, chiIS, "chi");
			engIS.close();
			chiIS.close();

			printStat();
			// System.out.println("Markable extraction: ");
			// System.out.println(evalMark.toString());
			LibLinearModel engllModel = engIS.train();
			LibLinearModel chillModel = chiIS.train();
			saveModel(options, engFs, engME, engllModel, chiFs, chiME,
					chillModel);

			System.out.println("chi Positive Percent:\t"
					+ chiIS.getPosInstance() + " / " + chiIS.getAllInstance()
					+ " = " + chiIS.getPosInstance() / chiIS.getAllInstance());
			System.out.println("eng Positive Percent:\t"
					+ engIS.getPosInstance() + " / " + engIS.getAllInstance()
					+ " = " + engIS.getPosInstance() / engIS.getAllInstance());

		} else {
			Common.bangErrorPOS("Not implemented joint algorithm: oneM or twoM");
		}

		System.out.println("Train combine model");
		if (Parallel.ensemble) {
			LibLinearModel chiEnsembleModel = chiEnsembleIS.train();
			LibLinearModel engEnsembleModel = engEnsembleIS.train();

			chiEnsembleIS.close();
			engEnsembleIS.close();

			System.out.println("Pos:\t" + ensemblePos);
			System.out.println("Neg:\t" + ensembleNeg);

			System.out.println("ensCHI Positive Percent:\t"
					+ chiEnsembleIS.getPosInstance() + " / "
					+ chiEnsembleIS.getAllInstance() + " = "
					+ chiEnsembleIS.getPosInstance()
					/ chiEnsembleIS.getAllInstance());
			System.out.println("ensENG Positive Percent:\t"
					+ engEnsembleIS.getPosInstance() + " / "
					+ engEnsembleIS.getAllInstance() + " = "
					+ engEnsembleIS.getPosInstance()
					/ engEnsembleIS.getAllInstance());

			LibLinearModel xChiModel = xChiIS.train();
			LibLinearModel xEngModel = xEngIS.train();
			xChiIS.close();
			xEngIS.close();

			System.out.println("XCHI Positive Percent:\t"
					+ xChiIS.getPosInstance() / xChiIS.getAllInstance());
			System.out.println("XENG Positive Percent:\t"
					+ xEngIS.getPosInstance() / xEngIS.getAllInstance());

			ModelReaderWriter.saveEnsemble(
					new File("ensemble." + options.model.getName()),
					chiEnsembleModel, engEnsembleModel, xChiModel, xEngModel,
					FS);

		}

		Common.outputLines(chiSVMs, "svm/chi." + options.model.getName());
		Common.outputLines(engSVMs, "svm/eng." + options.model.getName());
		Common.outputLines(chiEnsSVMs, "svm/chi.ens." + options.model.getName());
		Common.outputLines(engEnsSVMs, "svm/eng.ens." + options.model.getName());
	}

	private static HashSet<Span> loadSpans(GoldStandardChainExtractor gsce,
			final IMarkableExtractor engME, DocumentReader engReader) {
		HashSet<Span> engSpans = new HashSet<Span>();
		for (Document d : engReader) {
			// need to extract spans
			engSpans.addAll(engME.extractMarkables(d));
			for (Chain c : gsce.getGoldChains(d)) {
				engSpans.addAll(c.spans);
			}
		}
		return engSpans;
	}

	private static DocumentReader getDocReader(Options options,
			final IMarkableExtractor engME, File input, boolean MT)
			throws IOException {
		DocumentReader engReader = ReaderWriterFactory.getReader(
				options.inputFormat, input);
		if (engME.needsTraining() && !MT) {
			System.out.println("Training anaphoricy classifier: "
					+ input.getName());
			engME.train(engReader);
		}
		return engReader;
	}

	private static void printStat() {
		System.out.println("Documents: " + docCount);
		System.out.println("Sentences: " + senCount);
		System.out.println("Training Instances (% positive): "
				+ Util.insertCommas(trainingInstanceCount)
				+ " ("
				+ String.format("%.3f", 100.0 * positiveCount
						/ trainingInstanceCount) + "%)");
		System.out.println("Training model.");
		System.out.println();
	}

	private static void saveModel(Options options, final FeatureSet engFs,
			final IMarkableExtractor engME, final LibLinearModel engModel,
			final FeatureSet chiFs, final IMarkableExtractor chiME,
			final LibLinearModel chiModel) throws IOException {
		System.out.println("Saving model to file.");
		ModelReaderWriter.saveFullJoint(options.model, engME, engFs, engModel,
				chiME, chiFs, chiModel);
		System.out.println("Done.");
		System.out.println(new Date());
		System.out.println();
		System.out.println("Time: "
				+ Util.insertCommas(System.currentTimeMillis()
						- options.startms));
	}

	private static void langTrain(final FeatureSet FS,
			final ITrainingExampleExtractor engTeex, DocumentReader engReader,
			final InstanceSink jointIS, String lang) {
		Language.initLanguage(lang);

		int qid = 0;
		HashSet<Span> anaSet = new HashSet<Span>();

		for (Document d : engReader) {
			docCount++;
			senCount += d.sen.size();
			// List<PairInstance>
			// pis=trainingExampleExtractor.getInstances(goldChains,predSpans);
			List<PairInstance> pis = engTeex.getInstances(d);
			trainingInstanceCount += pis.size();
			for (PairInstance pi : pis) {
				if (!pi.ana.getSinglePronoun()) {
					continue;
				}

				final int label;
				if (pi.corefers) {
					positiveCount++;
					label = Coref.POSITIVE;
				} else {
					label = Coref.NEGATIVE;
				}
				FS.thisLang = true;
				List<FeatureNode> fns = FS.getFeatureNodes(pi, d);
				FS.thisLang = false;
				if (!pi.rankExtra) {
					if (!Parallel.filter
							|| Parallel.accessFiles.contains(d.docName + "_"
									+ d.lang)) {
						jointIS.sink(label, fns);
					}
				}

				int rank = 1;
				if (pi.corefers) {
					rank = 2;
				}

				if (pi.ant.empty && !pi.ana.isAnaphor) {
					rank = 2;
				}

				if (!anaSet.contains(pi.ana)) {
					anaSet.add(pi.ana);
					qid++;
				}

				String fnsSVM = Common.getSVMRankFormat(fns, rank, qid);

				if (lang.equalsIgnoreCase("chi")) {
					chiSVMs.add(fnsSVM);
				} else if (lang.equalsIgnoreCase("eng")) {
					engSVMs.add(fnsSVM);
				}

				if (Parallel.ensemble) {
					PairInstance xpi = pi.getXInstance();
					if (xpi != null
							&& (xpi.ant.empty || xpi.ana.s.d.docNo
									.equalsIgnoreCase(xpi.ant.s.d.docNo))) {
						if (pi.corefers) {
							ensemblePos++;
						} else {
							ensembleNeg++;
						}

						FS.bilingual = true;
						List<FeatureNode> allFns = FS.getFeatureNodes(pi, d);
						FS.bilingual = false;

						String allFnsSVM = Common.getSVMRankFormat(allFns,
								rank, qid);
						if (d.lang.equalsIgnoreCase("chi")) {
							if (!pi.rankExtra) {
								chiEnsembleIS.sink(label, allFns);
							}
							chiEnsSVMs.add(allFnsSVM);
						} else {
							if (!pi.rankExtra) {
								engEnsembleIS.sink(label, allFns);
							}
							engEnsSVMs.add(allFnsSVM);
						}

						FS.xLang = true;
						FS.x2 = true;
						List<FeatureNode> xFns = FS.getFeatureNodes(pi, d);
						FS.x2 = false;
						FS.xLang = false;
						if (d.lang.equalsIgnoreCase("chi")) {
							if (!pi.rankExtra) {
								xChiIS.sink(label, xFns);
							}
						} else {
							if (!pi.rankExtra) {
								xEngIS.sink(label, xFns);
							}
						}
					}
				}
			}
		}
	}

	public static FeatureSet getFeatureSet(Options options, final Language lang)
			throws IOException {
		FeatureSet fs = null;
		if (lang.getLang().equalsIgnoreCase("chi")) {
			fs = FeatureSet.getFromFile(options.chiFeatureSetFile);
		} else if (lang.getLang().equalsIgnoreCase("eng")) {
			fs = FeatureSet.getFromFile(options.engFeatureSetFile);
		}
		fs.appendFS(FeatureSet.getLoneFS(lang.getLang()));

		return fs;
	}
}
