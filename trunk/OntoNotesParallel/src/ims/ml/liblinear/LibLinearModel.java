package ims.ml.liblinear;

import ims.coref.Coref;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;

public class LibLinearModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Model model;
	private final int biasIndex;
	private final double bias;

	public LibLinearModel xmodel;

	public double correct = 0;

	public double tp = 0;
	public double tn = 0;
	public double fp = 0;
	public double fn = 0;

	public void putResult(double p, boolean coref) {
		if (coref && p > 0.5) {
			tp++;
		} else if (coref && p <= 0.5) {
			tn++;
		} else if (!coref && p > 0.5) {
			fp++;
		} else if (!coref && p <= 0.5) {
			fn++;
		}
	}

	public void printStat(String name) {
		double rec = tp / (tp + tn);
		double pre = tp / (tp + fp);
		double accu = (tp + fn) / (tp + tn + fp + fn);
		double f = 2 * rec * pre / (rec + pre);
		System.out.println("Model " + name);
		System.out.format("Rec: %f \n", rec);
		System.out.format("Pre: %f \n", pre);
		System.out.format("Acc: %f \n", accu);
		System.out.format("F-1: %f \n", f);
		System.out.format("All: %f \n", tp + tn + fp + fn);
		System.out.println("======");
	}

	public LibLinearModel(Model model) {
		this.model = model;
		this.bias = model.getBias();
		if (bias >= 0) {
			biasIndex = model.getNrFeature() + 1;
		} else {
			biasIndex = -1;
		}
	}

	public ClassLabel getMostProbableLabel(List<FeatureNode> fns) {
		ClassLabel[] cl = getAllLabels(fns);
		int best = -1;
		double bestPr = -1;
		for (int i = 0; i < cl.length; ++i) {
			if (cl[i].prob > bestPr) {
				bestPr = cl[i].prob;
				best = i;
			}
		}
		return cl[best];
	}

	public ClassLabel[] getAllLabels(List<FeatureNode> fns) {
		FeatureNode[] f = toArray(fns);
		sort(f);
		double[] pr = new double[model.getLabels().length];
		Linear.predictProbability(model, f, pr);
		ClassLabel[] cl = new ClassLabel[pr.length];
		int[] labels = model.getLabels();
		for (int i = 0; i < cl.length; ++i)
			cl[i] = new ClassLabel(labels[i], pr[i]);
		return cl;
	}

	public int getMostProbableClass(List<FeatureNode> fns) {
		FeatureNode[] f = toArray(fns);
		sort(f);
		int cl = Linear.predict(model, f);
		return cl;
	}

	private FeatureNode[] toArray(List<FeatureNode> fns) {
		FeatureNode[] f = new FeatureNode[fns.size() + (biasIndex >= 0 ? 1 : 0)];
		fns.toArray(f);
		if (biasIndex >= 0)
			f[f.length - 1] = new FeatureNode(biasIndex, bias);
		return f;
	}

	public static void sort(FeatureNode[] fns) {
		Arrays.sort(fns, FEATURENODE_COMAPARATOR);
	}

	public static Comparator<FeatureNode> FEATURENODE_COMAPARATOR = new Comparator<FeatureNode>() {
		@Override
		public int compare(FeatureNode arg0, FeatureNode arg1) {
			return arg0.index - arg1.index;
		}
	};

	public double getProbabilityForClass(List<FeatureNode> fns, int label) {
		ClassLabel[] cls = getAllLabels(fns);
		for (ClassLabel cl : cls) {
			if (cl.label == label)
				return cl.prob;
		}
		throw new RuntimeException("Model cannot predict label '" + label + "'");
	}

	public Model getModel() {
		return model;
	}
}
