package ilp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import util.Common;

public class ILPPronoun extends Thread {

	static Pattern mentionP = Pattern.compile("([^\\s]*)\\s(.*)");

	static Pattern zP = Pattern.compile("z\\((\\d*),(\\d*)\\)");

	static Pattern probP = Pattern
			.compile("([^\\-]*)-([^\\-]*)-([^\\s]*)\\s(.)");
	public ArrayList<String> spans;
	public HashMap<String, Double> OProb;
	public HashMap<String, Double> XProb;
	public HashMap<String, Double> EProb;
	public HashMap<String, Integer> nameMap;
	public HashSet<String> pronouns;

	int threadID;
	String path;

	public ILPPronoun(int threadID) {
		this.threadID = threadID;
	}

	public void readin(String path) {
		this.path = path;
		spans = new ArrayList<String>();
		OProb = new HashMap<String, Double>();
		XProb = new HashMap<String, Double>();
		EProb = new HashMap<String, Double>();
		nameMap = new HashMap<String, Integer>();
		pronouns = new HashSet<String>();

		ArrayList<String> lines = Common.getLines(path);
		boolean parseMention = true;
		for (String line : lines) {
			if (line.startsWith("###")) {
				parseMention = false;
				continue;
			}
			Matcher m;
			if (parseMention) {
				m = mentionP.matcher(line);
				if (m.find()) {
					String mid = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.add(mid);
					}
					spans.add(mid);
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				m = probP.matcher(line);
				if (m.find()) {
					String ante = m.group(1);
					String anaphor = m.group(2);
					Double prob = Double.valueOf(m.group(3));
					String type = m.group(4);

					String key = ante + "-" + anaphor;
					// only store pronouns
					if (type.equalsIgnoreCase("O")) {
						OProb.put(key, prob);
						// if (!XProb.containsKey(key)) {
						// XProb.put(key, prob);
						// }
						// if (!EProb.containsKey(key)) {
						// EProb.put(key, prob);
						// }
					} else if (type.equalsIgnoreCase("X")) {
						XProb.put(key, prob);
					} else if (type.equalsIgnoreCase("E")) {
						EProb.put(key, prob);
					}
				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
	}

	public int execute() throws LpSolveException {
		LpSolve lp;
		int Ncol, m, ret = 0;

		/*
		 * We will build the model row by row So we start with creating a model
		 * with 0 rows and 2 columns
		 */
		Ncol = spans.size() * (spans.size() - 1); /*
												 * there are two variables in
												 * the model
												 */
		if (Ncol == 0) {
			return 0;
		}
		/* create space large enough for one row */
		int[] colno = new int[Ncol * 3];
		double[] row = new double[Ncol * 3];

		lp = LpSolve.makeLp(0, Ncol);
		if (lp.getLp() == 0)
			ret = 1; /* couldn't construct a new model... */

		// set binary
		for (int i = 1; i < Ncol; i++) {
			lp.setBinary(i, true);
		}
		if (ret == 0) {
			/*
			 * let us name our variables. Not required, but can be usefull for
			 * debugging
			 */
			int vNo = 1;

			for (int i = 0; i < spans.size(); i++) {
				for (int j = i + 1; j < spans.size(); j++) {
					if (pronouns.contains(spans.get(j))) {
						String name = "o(" + i + "," + j + ")";
						lp.setColName(vNo, name);
						nameMap.put(name, vNo);
						vNo++;

						if (XProb
								.containsKey(spans.get(i) + "-" + spans.get(j))) {
							name = "x(" + i + "," + j + ")";
							lp.setColName(vNo, name);
							nameMap.put(name, vNo);
							vNo++;

							name = "e(" + i + "," + j + ")";
							lp.setColName(vNo, name);
							nameMap.put(name, vNo);
							vNo++;
						}
					}
				}
			}
			lp.setAddRowmode(true);
		}
		System.out.format("Span: %d \t Pronoun: %d\n", spans.size(),
				pronouns.size());
		// System.out.println("Register Variables");
		// // constraint 6: if transitive constraint
		int constraint = 0;
		if (ret == 0) {
			for (int i = 0; i < spans.size(); i++) {
				for (int j = i + 1; j < spans.size() && j < i + 0; j++) {
					if (!pronouns.contains(spans.get(j))) {
						continue;
					}
					for (int k = j + 1; k < spans.size() && k < i + 0; k++) {
						if (pronouns.contains(spans.get(k))) {
							int zij = nameMap.get("o(" + i + "," + j + ")");
							int zjk = nameMap.get("o(" + j + "," + k + ")");
							int zik = nameMap.get("o(" + i + "," + k + ")");

							/* construct z(i,j)+z(j,k)-z(i,k)<=1 */
							m = 0;
							colno[m] = zij;
							row[m++] = 1;

							colno[m] = zjk;
							row[m++] = 1;

							colno[m] = zik;
							row[m++] = -1;

							/* add the row to lp_solve */
							lp.addConstraintex(m, row, colno, LpSolve.LE, 1);
							constraint++;

							/* CONSTRUCT Z(I,J)+Z(I,K)-Z(J,K)<=1 */
							m = 0;
							colno[m] = zij;
							row[m++] = 1;

							colno[m] = zik;
							row[m++] = 1;

							colno[m] = zjk;
							row[m++] = -1;

							/* add the row to lp_solve */
							lp.addConstraintex(m, row, colno, LpSolve.LE, 1);

							/* construct z(i,k)+z(j,k)-z(i,j)<=1 */
							m = 0;
							colno[m] = zik;
							row[m++] = 1;

							colno[m] = zjk;
							row[m++] = 1;

							colno[m] = zij;
							row[m++] = -1;
							/* add the row to lp_solve */
							lp.addConstraintex(m, row, colno, LpSolve.LE, 1);
						}
					}
				}
			}
		}
		System.out.println("Add constraint 6:\t" + constraint);
		// constraint: np-np default coref
		// System.out.println("Deter: " + deter);
		// System.out.println("noDeter: " + noDeter);
		// constraint , keep O X E consistent
		for (int i = 0; i < spans.size(); i++) {
			for (int j = i + 1; j < spans.size(); j++) {
				if (pronouns.contains(spans.get(j))) {
					int oij = nameMap.get("o(" + i + "," + j + ")");
					if (XProb.containsKey(spans.get(i) + "-" + spans.get(j))) {
						int xij = nameMap.get("x(" + i + "," + j + ")");
						int eij = nameMap.get("e(" + i + "," + j + ")");

						m = 0;
						colno[m] = oij;
						row[m++] = 1;
						colno[m] = xij;
						row[m++] = -1;
						/* add the row to lp_solve */
						lp.addConstraintex(m, row, colno, LpSolve.EQ, 0);

						m = 0;
						colno[m] = oij;
						row[m++] = 1;
						colno[m] = eij;
						row[m++] = -1;

						/* add the row to lp_solve */
						lp.addConstraintex(m, row, colno, LpSolve.EQ, 0);
					}
				}
			}
		}

		if (ret == 0) {
			/* set the objective function */
			m = 0;
			for (int i = 0; i < spans.size(); i++) {
				for (int j = i + 1; j < spans.size(); j++) {
					if (!pronouns.contains(spans.get(j))) {
						continue;
					}
					// if (spans.get(i).isPronoun || spans.get(j).isPronoun) {
					int oij = nameMap.get("o(" + i + "," + j + ")");
					Double poij = OProb.get(spans.get(i) + "-" + spans.get(j));
					colno[m] = oij;
					row[m++] = 2 * poij.doubleValue() - 1;

					if (XProb.containsKey(spans.get(i) + "-" + spans.get(j))) {
						int xij = nameMap.get("x(" + i + "," + j + ")");
						Double pxij = XProb.get(spans.get(i) + "-"
								+ spans.get(j));
						colno[m] = xij;
						row[m++] = 2 * pxij.doubleValue() - 1;

						int eij = nameMap.get("e(" + i + "," + j + ")");
						Double peij = EProb.get(spans.get(i) + "-"
								+ spans.get(j));
						colno[m] = eij;
						row[m++] = 2 * peij.doubleValue() - 1;
					}
				}
			}
			/* set the objective in lp_solve */
			lp.setObjFnex(m, row, colno);
		}

		if (ret == 0) {
			lp.setAddRowmode(false); /*
									 * rowmode should be turned off again when
									 * done building the model
									 */
			/* set the object direction to maximize */
			lp.setMaxim();
			// lp.setMinim();
			/*
			 * just out of curioucity, now generate the model in lp format in
			 * file model.lp
			 */
			lp.writeLp("model.lp");
			// lp.writeMps("model.mps");

			/* I only want to see importand messages on screen while solving */
			lp.setVerbose(LpSolve.IMPORTANT);

			/* Now let lp_solve calculate a solution */
			ret = lp.solve();
			if (ret == LpSolve.OPTIMAL)
				ret = 0;
			else
				ret = 5;
		}
		System.err.println("Thread: " + this.threadID + " Return: " + ret);

		if (ret == 0) {
			System.out.println(lp.getObjective());

			/* a solution is calculated, now lets get some results */
			/* objective value */
			System.err.println("Objective value: " + lp.getObjective());

			/* variable values */
			lp.getVariables(row);

			ArrayList<String> lines = new ArrayList<String>();

			for (int i = 0; i < spans.size(); i++) {
				String span = spans.get(i);
				StringBuilder sb = new StringBuilder();
				sb.append(span).append(" ");
				if (pronouns.contains(span)) {
					sb.append("P");
				} else {
					sb.append("N");
				}
				lines.add(sb.toString());
			}

			lines.add("###");

			for (int j = 0; j < spans.size(); j++) {
				String anaphor = spans.get(j);
				if (!pronouns.contains(anaphor)) {
					continue;
				}
				for (int i = j - 1; i >= 0; i--) {
					String ant = spans.get(i);
					// String oij = "o(" + j + "," + i + ")";
					// String eij = "e(" + j + "," + i + ")";
					// String xij = "x(" + j + "," + i + ")";
					// TODO
					int om = nameMap.get("o(" + i + "," + j + ")");
					double ovalue = row[om - 1];
					lines.add(ant + "-" + anaphor + "-" + ovalue + "\t" + "O");

					if (XProb.containsKey(ant + "-" + anaphor)) {
						int xm = nameMap.get("x(" + i + "," + j + ")");
						int em = nameMap.get("e(" + i + "," + j + ")");
						double xvalue = row[xm - 1];
						lines.add(ant + "-" + anaphor + "-" + xvalue + "\t"
								+ "X");
						double evalue = row[em - 1];
						lines.add(ant + "-" + anaphor + "-" + evalue + "\t"
								+ "E");

						if (ovalue != xvalue || ovalue != evalue) {
							Common.bangErrorPOS("");
						}
					}
				}

			}
			Common.outputLines(lines, this.path + ".out");
			/* we are done now */
		}

		/* clean up such that all used memory by lp_solve is freeed */
		if (lp.getLp() != 0)
			lp.deleteLp();

		System.out.println("=============");
		return (ret);
	}

	public static void main(String args[]) throws Exception {
		if (args.length != 2) {
			System.out.println("java ~ lang part");
			System.exit(1);
		}
		String path = "/users/yzcchen/chen3/ijcnlp2013/ilp/" + args[0]
				+ args[1] + "/";

		ilpFiles = new Vector<String>();

		for (File f : (new File(path)).listFiles()) {
			if (f.getAbsolutePath().endsWith(".ilp")) {

				if (!f.getAbsolutePath().contains("bc-cctv-00-cctv_0005.7.ilp")) {
					// continue;
				}

				ilpFiles.add(f.getAbsolutePath());
			}
		}
		for (int i = 0; i < 5; i++) {
			Thread th = new ILPPronoun(i);
			th.start();
		}

	}

	static Vector<String> ilpFiles;

	public static synchronized String getFileStr() {
		if (ilpFiles.size() == 0) {
			return null;
		}
		System.out.println(ilpFiles.size());
		String file = ilpFiles.remove(0);
		return file;
	}

	@Override
	public void run() {
		try {
			while (true) {
				String file = getFileStr();
				if (file == null) {
					break;
				}
				this.readin(file);
				this.execute();
			}
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}
}
