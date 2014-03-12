package ims.ml.liblinear;

import ims.coref.Coref;

import java.util.ArrayList;
import java.util.List;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LibLinearInMemorySink implements InstanceSink {

	public static final Parameter DEFAULT_S0_PARAMETER = new Parameter(
			SolverType.L2R_LR, 1, 0.01);
	// default c is 1.
	public static final Parameter DEFAULT_S7_PARAMETER = new Parameter(
			SolverType.L2R_LR_DUAL, 0.25, 0.1);

	public static final Parameter DEFAULT_Ensem_PARAMETER = new Parameter(
			SolverType.L2R_LR_DUAL, 1, 0.01);

	final Parameter parameter;
	final double bias;
	private final List<IP> inst;

	private boolean closed = false;
	private int maxIndex = 0;

	public LibLinearInMemorySink(Parameter parameter, double bias) {
		this.parameter = parameter;
		this.bias = bias;
		inst = new ArrayList<IP>();
	}

	public double all = 0;
	public double positive = 0;

	@Override
	public void sink(int label, List<FeatureNode> nodes) {
		all++;
		if (label == Coref.POSITIVE) {
			positive++;
		}
		if (closed)
			throw new RuntimeException(
					"Can't add new instances when it has been closed.");
		FeatureNode[] x = new FeatureNode[nodes.size() + (bias >= 0 ? 1 : 0)];
		int lastIndex = -1;
		for (int i = 0; i < nodes.size(); ++i) {
			FeatureNode fn = nodes.get(i);
			if (fn.index <= lastIndex)
				throw new RuntimeException("Feature nodes not sorted");
			lastIndex = fn.index;
			x[i] = fn;
		}
		maxIndex = Math.max(lastIndex, maxIndex);
		inst.add(new IP(label, x));
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public LibLinearModel train() {
		Problem p = constructProblem();
		Model m = Linear.train(p, parameter);
		return new LibLinearModel(m);
	}

	private Problem constructProblem() {
		Problem problem = new Problem();
		problem.bias = bias;
		problem.l = inst.size();
		problem.n = maxIndex;
		boolean addBias = bias >= 0;
		if (addBias)
			problem.n++;
		problem.x = new FeatureNode[problem.l][];
		problem.y = new int[problem.l];
		int i = 0;
		for (IP ip : inst) {
			if (addBias) {
				if (ip.x[ip.x.length - 1] != null)
					throw new RuntimeException("Error here");
				ip.x[ip.x.length - 1] = new FeatureNode(maxIndex + 1, bias);
			}
			problem.x[i] = ip.x;
			problem.y[i] = ip.label;
			++i;
		}
		return problem;
	}

	private static final class IP {
		final int label;
		final FeatureNode[] x;

		IP(int label, FeatureNode[] x) {
			this.label = label;
			this.x = x;
		}
	}

	public static class ParameterBiasPair {
		public final Parameter parameter;
		public final double bias;

		private ParameterBiasPair(Parameter param, double bias) {
			this.parameter = param;
			this.bias = bias;
		}
	}

	public static ParameterBiasPair parseCmdLineArgs(String[] argv) {
		double bias = -1d;
		Parameter param = new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, 1,
				Double.POSITIVE_INFINITY);
		for (int i = 0; i < argv.length; i++) {
			if (argv[i].charAt(0) != '-')
				break;
			if (++i >= argv.length)
				throw new IllegalArgumentException("Failed to parse arguments.");
			switch (argv[i - 1].charAt(1)) {
			case 's':
				param.setSolverType(SolverType.values()[atoi(argv[i])]);
				break;
			case 'c':
				param.setC(atof(argv[i]));
				break;
			case 'e':
				param.setEps(atof(argv[i]));
				break;
			case 'B':
				bias = atof(argv[i]);
				break;
			case 'w':
				throw new IllegalArgumentException("-w parameter not supported");
			case 'v':
				throw new IllegalArgumentException("-v parameter not supported");
			case 'q':
				Linear.disableDebugOutput();
				break;
			default:
				System.err.println("unknown option");
				throw new IllegalArgumentException(
						"Failed to parse liblinear parameters");
			}
		}

		if (param.getEps() == Double.POSITIVE_INFINITY) {
			if (param.getSolverType() == SolverType.L2R_LR
					|| param.getSolverType() == SolverType.L2R_L2LOSS_SVC) {
				param.setEps(0.01);
			} else if (param.getSolverType() == SolverType.L2R_L2LOSS_SVC_DUAL
					|| param.getSolverType() == SolverType.L2R_L1LOSS_SVC_DUAL
					|| param.getSolverType() == SolverType.MCSVM_CS
					|| param.getSolverType() == SolverType.L2R_LR_DUAL) {
				param.setEps(0.1);
			} else if (param.getSolverType() == SolverType.L1R_L2LOSS_SVC
					|| param.getSolverType() == SolverType.L1R_LR) {
				param.setEps(0.01);
			}
		}
		return new ParameterBiasPair(param, bias);
	}

	static double atof(String s) {
		if (s == null || s.length() < 1)
			throw new IllegalArgumentException(
					"Can't convert empty string to integer");
		double d = Double.parseDouble(s);
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			throw new IllegalArgumentException("NaN or Infinity in input: " + s);
		}
		return (d);
	}

	static int atoi(String s) throws NumberFormatException {
		if (s == null || s.length() < 1)
			throw new IllegalArgumentException(
					"Can't convert empty string to integer");
		// Integer.parseInt doesn't accept '+' prefixed strings
		if (s.charAt(0) == '+')
			s = s.substring(1);
		return Integer.parseInt(s);
	}

	@Override
	public double getAllInstance() {
		return this.all;
	}

	@Override
	public double getPosInstance() {
		return this.positive;
	}
}
