package ims.coref.util;

import ims.coref.CorefCC;
import ims.coref.Options;
import ims.coref.Parallel;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;
import ims.coref.lang.Language;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.markables.MarkableExtractorFactory;
import ims.coref.postprocessor.IChainPostProcessor;
import ims.coref.postprocessor.NoPostProcessor;
import ims.coref.resolver.AbstractResolver;
import ims.coref.resolver.AbstractResolver.SingleLinkConstraint;
import ims.coref.resolver.AvgMaxProbResolver;
import ims.coref.resolver.BestLinkResolver;
import ims.coref.resolver.ClosestFirstResolver;
import ims.coref.resolver.Decoder;
import ims.coref.resolver.PrBLResolver;
import ims.coref.resolver.PrCFResolver;
import ims.coref.resolver.StackedResolver;
import ims.coref.training.ITrainingExampleExtractor;
import ims.ml.liblinear.LibLinearModel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ModelReaderWriter {

	static final String ENSEMBLE_MODEL_ENTRY = "ensemble.model";
	static final String ENSEMBLE_FS_ENTRY = "ensemble.fs";

	static final String LL_MODEL_ENTRY = "llmodel";
	static final String ME_ENTRY = "markableExt";
	static final String TEEX_ENTRY = "trainingExt";
	static final String FS_ENTRY = "fs";
	static final String FS_TXT = "features.txt";
	static final String LANG_ENTRY = "lang";
	static final String LANG_TXT = "lang.txt";

	static final String STACKED_STAGE1_ENTRY = "stack1";

	static final String STACKFS_ENTRY = "stackfs";
	static final String STACKFS_TXT = "stackfs.txt";
	static final String STACK_MODEL1_ENTRY = "llmodel1stack";
	static final String STACK_MODEL2_ENTRY = "llmodel2stack";

	static final String STACK_DECODE_DATA = "stack_decode_data";

	private static IMarkableExtractor getMarkableExtractor(Options options,
			ZipFile zf) throws IOException, ClassNotFoundException {
		IMarkableExtractor me = (IMarkableExtractor) loadObjectFromEntry(
				ME_ENTRY, zf);
		if (options.useGoldMarkableExtractor)
			me = MarkableExtractorFactory.getGoldExtractor(me,
					options.keepNonRefPruner);
		return me;
	}

	public static AbstractResolver loadResolver(Options options)
			throws ZipException, IOException, ClassNotFoundException {
		ZipFile zf = new ZipFile(options.model);
		LibLinearModel llModel = (LibLinearModel) loadObjectFromEntry(
				LL_MODEL_ENTRY, zf);
		IMarkableExtractor me = getMarkableExtractor(options, zf);
		FeatureSet fs = (FeatureSet) loadObjectFromEntry(FS_ENTRY, zf);
		Language lang = (Language) loadObjectFromEntry(LANG_ENTRY, zf);
		Language.setLanguage(lang);
		IChainPostProcessor pp = getPostprocessor(options.postProcessor);
		return createResolver(options.decode, llModel, me, fs, pp,
				options.decodeWindow, options.decodeTH,
				options.avgMaxProbNewTH, options.avgMaxProbMergeTH,
				options.reorder, options.singleLinkConstraint,
				options.avgMaxProbRelaxMergeTHStepSize);
	}

	private static AbstractResolver createResolver(Decoder decode,
			LibLinearModel llModel, IMarkableExtractor me, FeatureSet fs,
			IChainPostProcessor pp, int decodeWindow, double decodeTH,
			double avgMaxProbNewTH, double avgMaxProbMergeTH, boolean reorder,
			SingleLinkConstraint slc, double avgMaxProbRelaxMergeThStepSize) {
		switch (decode) {
		case ClosestFirst:
			return new ClosestFirstResolver(me, llModel, fs, pp, decodeWindow,
					slc);
		case BestLink:
			return new BestLinkResolver(me, llModel, fs, pp, decodeWindow,
					decodeTH, slc);
		case AvgMaxProb:
			return new AvgMaxProbResolver(me, llModel, fs, pp, decodeWindow,
					avgMaxProbNewTH, avgMaxProbMergeTH, reorder,
					avgMaxProbRelaxMergeThStepSize);
		case PronounsBL:
			return new PrBLResolver(me, llModel, fs, pp, decodeWindow,
					decodeTH, slc);
		case PronounsCF:
			return new PrCFResolver(me, llModel, fs, pp, decodeWindow,
					decodeTH, slc);
		default:
			throw new Error("!");
		}
	}

	public static IChainPostProcessor getPostprocessor(String ppName) {
		return new NoPostProcessor();
	}

	public static class StackModel1 {
		public AbstractResolver[] resolvers;
		public FeatureSet fs;
		public IMarkableExtractor me;
		public ITrainingExampleExtractor teex;
	}

	public static StackModel1 loadStacked1(Options options) throws IOException,
			ClassNotFoundException {
		ZipFile zf = new ZipFile(options.stackModel1);
		StackModel1 sm1 = new StackModel1();
		LibLinearModel[] llModel = (LibLinearModel[]) loadObjectFromEntry(
				STACKED_STAGE1_ENTRY, zf);
		sm1.me = (IMarkableExtractor) loadObjectFromEntry(ME_ENTRY, zf);
		sm1.fs = (FeatureSet) loadObjectFromEntry(FS_ENTRY, zf);
		Language lang = (Language) loadObjectFromEntry(LANG_ENTRY, zf);
		Language.setLanguage(lang);
		sm1.teex = (ITrainingExampleExtractor) loadObjectFromEntry(TEEX_ENTRY,
				zf);
		IChainPostProcessor pp = getPostprocessor("none");
		sm1.resolvers = new AbstractResolver[llModel.length];
		for (int i = 0; i < llModel.length; ++i) {
			sm1.resolvers[i] = createResolver(options.decode, llModel[i],
					sm1.me, sm1.fs, pp, options.decodeWindow, options.decodeTH,
					options.avgMaxProbNewTH, options.avgMaxProbMergeTH,
					options.reorder, options.singleLinkConstraint,
					options.avgMaxProbRelaxMergeTHStepSize);
		}
		return sm1;
	}

	public static StackedResolver loadStackedSolver(Options options)
			throws IOException, ClassNotFoundException {
		ZipFile zf = new ZipFile(options.model);

		// IMarkableExtractor me=(IMarkableExtractor)
		// loadObjectFromEntry(ME_ENTRY,zf);
		IMarkableExtractor me = getMarkableExtractor(options, zf);
		Language lang = (Language) loadObjectFromEntry(LANG_ENTRY, zf);
		Language.setLanguage(lang);

		FeatureSet fs1 = (FeatureSet) loadObjectFromEntry(FS_ENTRY, zf);
		FeatureSet fs2 = (FeatureSet) loadObjectFromEntry(STACKFS_ENTRY, zf);

		LibLinearModel m1 = (LibLinearModel) loadObjectFromEntry(
				STACK_MODEL1_ENTRY, zf);
		LibLinearModel m2 = (LibLinearModel) loadObjectFromEntry(
				STACK_MODEL2_ENTRY, zf);
		String[] m1DecoderData = readStringTXT(zf, STACK_DECODE_DATA);
		AbstractResolver r1 = createResolver(Decoder.valueOf(m1DecoderData[0]),
				m1, me, fs1, getPostprocessor("none"),
				Integer.parseInt(m1DecoderData[1]),
				Double.parseDouble(m1DecoderData[2]),
				Double.parseDouble(m1DecoderData[3]),
				Double.parseDouble(m1DecoderData[4]),
				Boolean.parseBoolean(m1DecoderData[5]),
				SingleLinkConstraint.valueOf((m1DecoderData[6])),
				Double.parseDouble(m1DecoderData[7]));
		AbstractResolver r2 = createResolver(options.decode, m2, me,
				FeatureSet.concat(fs1, fs2), getPostprocessor("none"),
				options.decodeWindow, options.decodeTH,
				options.avgMaxProbNewTH, options.avgMaxProbMergeTH,
				options.reorder, options.singleLinkConstraint,
				options.avgMaxProbRelaxMergeTHStepSize);
		return new StackedResolver(r1, r2,
				getPostprocessor(options.postProcessor));
	}

	public static void saveStacked2(File model, StackModel1 sm1,
			FeatureSet stackFS, LibLinearModel model1, LibLinearModel model2,
			Options options) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(model)));
		saveObjectAsZipEntry(ME_ENTRY, sm1.me, zos);
		saveObjectAsZipEntry(LANG_ENTRY, Language.getLanguage(), zos);
		writeLangTXT(zos, Language.getLanguage());

		saveObjectAsZipEntry(FS_ENTRY, sm1.fs, zos);
		writeFeatureTXT(zos, sm1.fs, FS_TXT);
		saveObjectAsZipEntry(STACKFS_ENTRY, stackFS, zos);
		writeFeatureTXT(zos, stackFS, STACKFS_TXT);

		saveObjectAsZipEntry(STACK_MODEL1_ENTRY, model1, zos);
		saveObjectAsZipEntry(STACK_MODEL2_ENTRY, model2, zos);
		String[] decodeStackData = { options.decode.toString(),
				Integer.toString(options.decodeWindow),
				Double.toString(options.decodeTH),
				Double.toString(options.avgMaxProbNewTH),
				Double.toString(options.avgMaxProbMergeTH),
				Boolean.toString(options.reorder),
				options.singleLinkConstraint.toString(),
				Double.toString(options.avgMaxProbRelaxMergeTHStepSize) };
		writeStringTXT(zos, STACK_DECODE_DATA, decodeStackData);
		zos.close();
	}

	public static void saveStacked1(File model,
			IMarkableExtractor markableExtractor, FeatureSet fs,
			LibLinearModel[] models, Language lang,
			ITrainingExampleExtractor trainingExExtractor) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(model)));
		saveObjectAsZipEntry(STACKED_STAGE1_ENTRY, models, zos);
		saveObjectAsZipEntry(ME_ENTRY, markableExtractor, zos);
		saveObjectAsZipEntry(FS_ENTRY, fs, zos);
		saveObjectAsZipEntry(TEEX_ENTRY, trainingExExtractor, zos);
		saveObjectAsZipEntry(LANG_ENTRY, lang, zos);
		writeFeatureTXT(zos, fs, FS_TXT);
		writeLangTXT(zos, lang);
		zos.close();
	}

	public static void save(File model, IMarkableExtractor markableExtractor,
			FeatureSet fs, LibLinearModel llModel, Language lang)
			throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(model)));
		if (llModel != null)
			saveObjectAsZipEntry(LL_MODEL_ENTRY, llModel, zos);
		saveObjectAsZipEntry(ME_ENTRY, markableExtractor, zos);
		saveObjectAsZipEntry(FS_ENTRY, fs, zos);
		saveObjectAsZipEntry(LANG_ENTRY, lang, zos);
		writeFeatureTXT(zos, fs, FS_TXT);
		writeLangTXT(zos, lang);
		zos.close();
	}

	public static void saveEnsemble(File model,
			LibLinearModel chiEnsembleModel, LibLinearModel engEnsembleModel, 
			LibLinearModel xChiModel, LibLinearModel xEngModel, 
			FeatureSet ensembleFS) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(model)));
		saveObjectAsZipEntry("eng" + ENSEMBLE_MODEL_ENTRY, engEnsembleModel,
				zos);
		saveObjectAsZipEntry("chi" + ENSEMBLE_MODEL_ENTRY, chiEnsembleModel,
				zos);
		
		
		saveObjectAsZipEntry("xeng" + ENSEMBLE_MODEL_ENTRY, xEngModel,
				zos);
		saveObjectAsZipEntry("xchi" + ENSEMBLE_MODEL_ENTRY, xChiModel,
				zos);
		

		saveObjectAsZipEntry(ENSEMBLE_FS_ENTRY, ensembleFS, zos);
		zos.close();
	}

	public static void saveFullJoint(File model, IMarkableExtractor engME,
			FeatureSet engFS, LibLinearModel engllModel,
			IMarkableExtractor chiME, FeatureSet chiFS,
			LibLinearModel chillModel) throws IOException {

		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(model)));

		saveObjectAsZipEntry("eng" + LL_MODEL_ENTRY, engllModel, zos);
		saveObjectAsZipEntry("eng" + ME_ENTRY, engME, zos);
		saveObjectAsZipEntry("eng" + FS_ENTRY, engFS, zos);
		writeFeatureTXT(zos, engFS, "eng" + FS_TXT);

		saveObjectAsZipEntry("chi" + LL_MODEL_ENTRY, chillModel, zos);
		saveObjectAsZipEntry("chi" + ME_ENTRY, chiME, zos);
		saveObjectAsZipEntry("chi" + FS_ENTRY, chiFS, zos);
		writeFeatureTXT(zos, chiFS, "chi" + FS_TXT);

		zos.close();
	}

	public static AbstractResolver loadFullJointResolver(Options options)
			throws ZipException, IOException, ClassNotFoundException {
		ZipFile zf = new ZipFile(options.model);
		LibLinearModel engllModel = (LibLinearModel) loadObjectFromEntry("eng"
				+ LL_MODEL_ENTRY, zf);
		IMarkableExtractor engME = (IMarkableExtractor) loadObjectFromEntry(
				"eng" + ME_ENTRY, zf);
		FeatureSet engFs = (FeatureSet) loadObjectFromEntry("eng" + FS_ENTRY,
				zf);
		for (IFeature f : engFs.getFeatures()) {
			// f.setLang("eng");
		}

		LibLinearModel chillModel = (LibLinearModel) loadObjectFromEntry("chi"
				+ LL_MODEL_ENTRY, zf);
		IMarkableExtractor chiME = (IMarkableExtractor) loadObjectFromEntry(
				"chi" + ME_ENTRY, zf);
		FeatureSet chiFs = (FeatureSet) loadObjectFromEntry("chi" + FS_ENTRY,
				zf);
		for (IFeature f : chiFs.getFeatures()) {
			// f.setLang("chi");
		}

		IChainPostProcessor pp = getPostprocessor(options.postProcessor);
		AbstractResolver resolver = createResolver(options.decode, null, null,
				FeatureSet.concat(engFs, chiFs), pp, options.decodeWindow,
				options.decodeTH, options.avgMaxProbNewTH,
				options.avgMaxProbMergeTH, options.reorder,
				options.singleLinkConstraint,
				options.avgMaxProbRelaxMergeTHStepSize);

		resolver.engllModel = engllModel;
		resolver.engME = engME;

		resolver.chillModel = chillModel;
		resolver.chiME = chiME;

		resolver.ensembleFS = resolver.fs;
		if (Parallel.jointTest && !Parallel.dev) {
			ZipFile ensembleZF = new ZipFile(new File("ensemble."
					+ options.model.getName()));
			resolver.chiEnsembleModel = (LibLinearModel) loadObjectFromEntry(
					"chi" + ENSEMBLE_MODEL_ENTRY, ensembleZF);
			resolver.engEnsembleModel = (LibLinearModel) loadObjectFromEntry(
					"eng" + ENSEMBLE_MODEL_ENTRY, ensembleZF);
			
			resolver.xChiModel = (LibLinearModel) loadObjectFromEntry(
					"xchi" + ENSEMBLE_MODEL_ENTRY, ensembleZF);
			resolver.xEngModel = (LibLinearModel) loadObjectFromEntry(
					"xeng" + ENSEMBLE_MODEL_ENTRY, ensembleZF);
			
			resolver.engllModel.xmodel = resolver.xEngModel;
			resolver.chillModel.xmodel = resolver.xChiModel;
			
			resolver.engllModel.xmodel = (LibLinearModel) loadObjectFromEntry("chi"
					+ LL_MODEL_ENTRY, zf);
			resolver.chillModel.xmodel = (LibLinearModel) loadObjectFromEntry("eng"
					+ LL_MODEL_ENTRY, zf);
			
			resolver.ensembleFS = (FeatureSet) loadObjectFromEntry(
					ENSEMBLE_FS_ENTRY, ensembleZF);
		}

		return resolver;
	}

	private static void writeLangTXT(ZipOutputStream zos, Language lang)
			throws IOException {
		writeStringTXT(zos, LANG_TXT, lang.getClass().getCanonicalName());
	}

	private static void writeFeatureTXT(ZipOutputStream zos, FeatureSet fs,
			String entry) throws IOException {
		String[] strings = new String[fs.getFeatures().size()];
		int i = 0;
		for (IFeature f : fs.getFeatures())
			strings[i++] = f.toString();

		writeStringTXT(zos, entry, strings);
	}

	private static void writeStringTXT(ZipOutputStream zos, String entry,
			String... strings) throws IOException {
		zos.putNextEntry(new ZipEntry(entry));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zos));
		for (String s : strings) {
			writer.write(s);
			writer.newLine();
		}
		writer.flush();
	}

	private static String[] readStringTXT(ZipFile zf, String entry)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				zf.getInputStream(zf.getEntry(entry))));
		List<String> a = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null)
			a.add(line);
		return a.toArray(new String[a.size()]);
	}

	public static void saveObjectAsZipEntry(String entryName, Object obj,
			ZipOutputStream zos) throws IOException {
		zos.putNextEntry(new ZipEntry(entryName));
		ObjectOutputStream oos = new ObjectOutputStream(zos);
		oos.writeObject(obj);
		oos.flush();
	}

	public static Object loadObjectFromEntry(String entryName, ZipFile zf)
			throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
				zf.getInputStream(zf.getEntry(entryName))));
		Object o = ois.readObject();
		return o;
	}

}
