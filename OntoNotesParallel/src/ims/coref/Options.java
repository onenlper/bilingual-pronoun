package ims.coref;

import ims.coref.resolver.AbstractResolver.SingleLinkConstraint;
import ims.coref.resolver.Decoder;
import ims.coref.training.AbstractTrainingExampleExtractor.CommitStrategy;
import ims.ml.liblinear.LibLinearInMemorySink;
import ims.ml.liblinear.LibLinearInMemorySink.ParameterBiasPair;
import ims.util.Util;

import java.io.File;
import java.util.Date;

public class Options {

	// Global and shared options.
	public static File genderData;
	public static File wordNetDictDir;
	public static boolean PARTITION_BY_PART = false;
	public static String customTokenAnaphoricityTh = null;

	public final Date start;
	public final long startms;
	
	public boolean develop = false;
	
	public String joint = "full";// full|semi

	public String lang = "eng";
	public String sourcelang = "eng";
	public String trainingExampleExtractor = "soon";

	public File chiInput = new File("");
	public File engInput = new File("");
	
	public File chiOut = new File("");
	public File engOut = new File("");

	public File input = new File("input.coref");
	public File model = new File("coref.mdl");
	public File output = new File("out");
	public File t2;

	public Format inputFormat = Format.C12;
	public Format outputFormat = Format.C12;

	public String markableExtractors;
	
	public String chiMarkableExtractors;
	public String engMarkableExtractors;

	public File featureSetFile;

	public File chiFeatureSetFile;
	public File engFeatureSetFile;

	public static File brownCluster;

	public Training training = Training.InMemory;
	public double bias = -1;
	public File onDiskTrainingFile = new File("training.data");
	public CommitStrategy commitStrategy = CommitStrategy.Always;

	public Decoder decode = Decoder.ClosestFirst;

	public enum Format {
		Custom, C12
	}

	public enum Training {
		OnDisk, InMemory
	}

	public String postProcessor = "none";

	public int decodeWindow = Integer.MAX_VALUE - 1;
	public double decodeTH = 0.5d;
	public double avgMaxProbMergeTH = 0.5d;
	public double avgMaxProbNewTH = 0.5d;
	public double avgMaxProbRelaxMergeTHStepSize = 0.0d;
	public boolean reorder = true;

	public boolean stacked = false;

	public static int cores = Math.min(12, Runtime.getRuntime()
			.availableProcessors());
	public File stackModel1 = new File("corefStack1.mdl");
	public int stackfolds = 10;
	public String stackTrainingExampleExtractor = "stacked";

	public boolean multiThreadedStackDecode = false;

	public de.bwaldvogel.liblinear.Parameter libLinearParameter = LibLinearInMemorySink.DEFAULT_S7_PARAMETER;

	public static double anaphoricityThreshold = 1.1d;

	public Options(String[] args) {
		start = new Date();
		System.out.println("Create options at " + start);
		startms = System.currentTimeMillis();
		for (int ai = 0; ai < args.length;)
			ai = parseCmd(args, ai);
		System.out.println("Using threads: " + cores);
	}

	public boolean useGoldMarkableExtractor = false;

	public SingleLinkConstraint singleLinkConstraint = SingleLinkConstraint.NoTransitiveEmbedding;
	public boolean keepNonRefPruner = false;

	private int parseCmd(String[] args, int ai) {
		int in = ai;
		if (args[ai].equals("-joint")) {
			ai++;
			joint = args[ai++];
		} else if (args[ai].equals("-dev")) {
			ai++;
			this.develop = Boolean.valueOf(args[ai++]);
		} else if (args[ai].equals("-chiIn")) {
			ai++;
			chiInput = new File(args[ai++]);
		} else if (args[ai].equals("-engIn")) {
			ai++;
			engInput = new File(args[ai++]);
		} else if (args[ai].equals("-chiOut")) {
			ai++;
			chiOut = new File(args[ai++]);
		} else if (args[ai].equals("-engOut")) {
			ai++;
			engOut = new File(args[ai++]);
		} else if (args[ai].equals("-chiFeature")) {
			ai++;
			this.chiFeatureSetFile = new File(args[ai++]);
		} else if (args[ai].equals("-engFeature")) {
			ai++;
			this.engFeatureSetFile = new File(args[ai++]);
		} else if (args[ai].equals("-chiMES")) {
			ai++;
			this.chiMarkableExtractors = args[ai++];
		} else if (args[ai].equals("-engES")) {
			ai++;
			this.engMarkableExtractors = args[ai++];
		} else if (args[ai].equals("-input") || args[ai].equals("-in")) {
			ai++;
			input = new File(args[ai++]);
		} else if (args[ai].equals("-output") || args[ai].equals("-out")) {
			ai++;
			output = new File(args[ai++]);
		} else if (args[ai].equals("-model") || args[ai].equals("-corefModel")) {
			ai++;
			model = new File(args[ai++]);
		} else if (args[ai].equals("-gender")) {
			ai++;
			genderData = new File(args[ai++]);
		} else if (args[ai].equals("-inputFormat")) {
			ai++;
			inputFormat = Format.valueOf(args[ai++]);
		} else if (args[ai].equals("-outputFormat")) {
			ai++;
			outputFormat = Format.valueOf(args[ai++]);
		} else if (args[ai].equals("-lang")) {
			ai++;
			lang = args[ai++].toLowerCase();
		} else if (args[ai].equals("-srclang")) {
			ai++;
			this.sourcelang = args[ai++].toLowerCase();
		} else if (args[ai].equals("-trainingExCreation")) {
			ai++;
			trainingExampleExtractor = args[ai++];
		} else if (args[ai].startsWith("-markableExtractor")) {
			ai++;
			markableExtractors = args[ai++];
		} else if (args[ai].equals("-features")) {
			ai++;
			featureSetFile = new File(args[ai++]);
		} else if (args[ai].equals("-training")) {
			ai++;
			training = Training.valueOf(args[ai++]);
		} else if (args[ai].equals("-bias")) {
			ai++;
			bias = Double.parseDouble(args[ai++]);
		} else if (args[ai].equals("-trainFile")) {
			ai++;
			onDiskTrainingFile = new File(args[ai++]);
		} else if (args[ai].equals("-commitStrategy")) {
			ai++;
			commitStrategy = CommitStrategy.valueOf(args[ai++]);
		} else if (args[ai].equals("-decode")) {
			ai++;
			decode = Decoder.valueOf(args[ai++]);
		} else if (args[ai].equals("-wordNetDictDir")) {
			ai++;
			wordNetDictDir = new File(args[ai++]);
		} else if (args[ai].equals("-postProcessor")) {
			ai++;
			postProcessor = args[ai++];
		} else if (args[ai].equals("-decodeWindow")) {
			ai++;
			decodeWindow = Integer.parseInt(args[ai++]);
		} else if (args[ai].equals("-decodeTH")) {
			ai++;
			decodeTH = Double.parseDouble(args[ai++]);
		} else if (args[ai].equals("-avgMaxProbNewTH")) {
			ai++;
			avgMaxProbNewTH = Double.parseDouble(args[ai++]);
		} else if (args[ai].equals("-avgMaxProbMergeTH")) {
			ai++;
			avgMaxProbMergeTH = Double.parseDouble(args[ai++]);
		} else if (args[ai].equals("-relaxMergeThStepSize")) {
			ai++;
			avgMaxProbRelaxMergeTHStepSize = Double.parseDouble(args[ai++]);
		} else if (args[ai].equals("-avgMaxProbTH")) {
			ai++;
			avgMaxProbNewTH = Double.parseDouble(args[ai++]);
			avgMaxProbMergeTH = avgMaxProbNewTH;
		} else if (args[ai].equals("-stackModel1")) {
			ai++;
			stackModel1 = new File(args[ai++]);
		} else if (args[ai].equals("-stackFolds")) {
			ai++;
			stackfolds = Integer.parseInt(args[ai++]);
		} else if (args[ai].equals("-cores")) {
			ai++;
			cores = Integer.parseInt(args[ai++]);
		} else if (args[ai].equals("-stacked")) {
			ai++;
			stacked = true;
		} else if (args[ai].equals("-brown")
				|| args[ai].equalsIgnoreCase("-brownCluster")) {
			ai++;
			brownCluster = new File(args[ai++]);
		} else if (args[ai].equals("-partitionByPart")) {
			ai++;
			PARTITION_BY_PART = true;
		} else if (args[ai].equals("-stackTrainingExampleExtractor")) {
			ai++;
			stackTrainingExampleExtractor = args[ai++];
		} else if (args[ai].equals("-dontReorder")) {
			ai++;
			reorder = false;
		} else if (args[ai].equals("-singleLinkConstraint")) {
			ai++;
			singleLinkConstraint = SingleLinkConstraint.valueOf(args[ai++]);
		} else if (args[ai].equals("-multiThreadedStackDecode")) {
			ai++;
			multiThreadedStackDecode = true;
		} else if (args[ai].equals("-s")) {
			ai++;
			if (args[ai].equals("0"))
				libLinearParameter = LibLinearInMemorySink.DEFAULT_S0_PARAMETER;
			else
				throw new Error("Unknown -s value '-s " + args[ai] + "'");
			ai++;
		} else if (args[ai].equals("-llParams")) {
			ai++;
			String[] a = args[ai].split("\\s");
			ParameterBiasPair p = LibLinearInMemorySink.parseCmdLineArgs(a);
			libLinearParameter = p.parameter;
			bias = p.bias;
			ai++;
		} else if (args[ai].equals("-t2")) {
			ai++;
			t2 = new File(args[ai++]);
		} else if (args[ai].equals("-anaphoricityTh")) {
			ai++;
			anaphoricityThreshold = Double.parseDouble(args[ai++]);
		} else if (args[ai].equals("-customTokenAnaphoricityTh")) {
			ai++;
			customTokenAnaphoricityTh = args[ai++];
		} else if (args[ai].equals("-C")) {
			ai++;
			double C = Double.parseDouble(args[ai++]);
			libLinearParameter.setC(C);
		} else if (args[ai].equals("-useGoldMarkableExtrator")) {
			ai++;
			useGoldMarkableExtractor = true;
		} else if (args[ai].equals("-keepNonRefPruner")) {
			ai++;
			keepNonRefPruner = true;
		} else if (args[ai].equals("")) {
		} else if (args[ai].equals("")) {
		} else if (args[ai].equals("")) {
		}
		if (in == ai)
			throw new RuntimeException("Failed to parse " + args[ai]);
		return ai;
	}

	public void done() {
		System.out.println("Done.");
		System.out.println(new Date());
		System.out.println();
		System.out.println("Time: "
				+ Util.insertCommas(System.currentTimeMillis() - startms));
	}

}
