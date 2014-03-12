package ilp;

import ims.coref.data.Span;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import util.Common;

public class ILP extends Thread {

	static Pattern mentionP = Pattern
			.compile("(\\d*)\\:(\\d*)\\:(\\d*)-(\\d*)\\s(.)");
	static Pattern probP = Pattern.compile("(\\d*)-(\\d*)\\:([^\\s]*)\\s(.)");
	public HashMap<Integer, Span> spans;
	public HashMap<String, Double> OProb;
	public HashMap<String, Double> XProb;
	public HashMap<String, Double> EProb;
	public HashMap<String, Integer> nameMap;
	public HashSet<Integer> pronouns;

	int threadID;

	public ILP(int threadID) {
		this.threadID = threadID;
	}

	public void readin(String path) {
		spans = new HashMap<Integer, Span>();
		OProb = new HashMap<String, Double>();
		XProb = new HashMap<String, Double>();
		EProb = new HashMap<String, Double>();
		nameMap = new HashMap<String, Integer>();
		pronouns = new HashSet<Integer>();

		ArrayList<String> lines = Common.getLines(path);
		boolean parseMention = true;
		for (String line : lines) {
			if (line.startsWith("#")) {
				parseMention = false;
				continue;
			}
			Matcher m;
			if (parseMention) {
				m = mentionP.matcher(line);
				if (m.find()) {
					int mid = Integer.parseInt(m.group(1));
					if (mid != spans.size()) {
						Common.bangErrorPOS("GEEE mention not match");
					}
					int sid = Integer.parseInt(m.group(2));
					int start = Integer.parseInt(m.group(3));
					int end = Integer.parseInt(m.group(4));

					String type = m.group(5);
					Span s = new Span(sid, start, end);
					if (type.equalsIgnoreCase("P")) {
						s.isPronoun = true;
						pronouns.add(mid);
					} else {
						s.isPronoun = false;
					}
					spans.put(mid, s);
				}
			} else {
				m = probP.matcher(line);
				if (m.find()) {
					int anaIdx = Integer.parseInt(m.group(1));
					int antIdx = Integer.parseInt(m.group(2));
					Double prob = Double.valueOf(m.group(3));
					String type = m.group(4);
					String key = anaIdx + "-" + antIdx;

					// only store pronouns
					if (type.equalsIgnoreCase("O")) {
						OProb.put(key, prob);
					} else if (type.equalsIgnoreCase("X")) {
						XProb.put(key, prob);
					} else {
						EProb.put(key, prob);
					}
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
		int[] colno = new int[Ncol * 2];
		double[] row = new double[Ncol * 2];

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
					String name = "z(" + i + "," + j + ")";
					// if (spans.get(i).isPronoun || spans.get(j).isPronoun) {
					lp.setColName(vNo, name);
					nameMap.put(name, vNo);
					vNo++;
					// }
				}
			}
			lp.setAddRowmode(true);
		}
		// System.out.println("Register Variables");
		// // constraint 6: if transitive constraint
		int constraint = 0;
		if (ret == 0) {
			for (int i = 0; i < spans.size(); i++) {
				for (int j = i + 1; j < spans.size(); j++) {
					if (pronouns.contains(j)) {
						continue;
					}
					for (int k = j + 1; k < spans.size(); k++) {
						if (pronouns.contains(k)) {
							int zij = nameMap.get("z(" + i + "," + j + ")");
							int zjk = nameMap.get("z(" + j + "," + k + ")");
							int zik = nameMap.get("z(" + i + "," + k + ")");

							/* construct z(i,j)+z(j,k)-z(i,k)<=1 */
							m = 0;
							colno[m] = zij;
							row[m++] = 1;

							colno[m] = zjk;
							row[m++] = 1;

							colno[m] = zik;
							row[m++] = -1;

							/* add the row to lp_solve */
							// lp.addConstraintex(m, row, colno, LpSolve.LE, 1);
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
							// lp.addConstraintex(m, row, colno, LpSolve.LE, 1);

							/* construct z(i,k)+z(j,k)-z(i,j)<=1 */
							m = 0;
							colno[m] = zik;
							row[m++] = 1;

							colno[m] = zjk;
							row[m++] = 1;

							colno[m] = zij;
							row[m++] = -1;
							/* add the row to lp_solve */
							// lp.addConstraintex(m, row, colno, LpSolve.LE, 1);
						}
					}
				}
			}
		}
		// System.out.println("Add constraint 6:\t" + constraint);
		int deter = 0;
		int noDeter = 0;
		// constraint: np-np default coref
		if (ret == 0) {
			for (int i = 0; i < spans.size(); i++) {
				for (int j = i + 1; j < spans.size(); j++) {
					if (!spans.get(i).isPronoun && !spans.get(j).isPronoun) {
						int zij = nameMap.get("z(" + i + "," + j + ")");
						m = 0;
						colno[m] = zij;
						row[m++] = 1;
						double prob = OProb.get(i + "-" + j);
						if (prob > 0.5) {
							lp.addConstraintex(m, row, colno, LpSolve.EQ, 1);
						} else {
							lp.addConstraintex(m, row, colno, LpSolve.EQ, 0);
						}
						deter++;
					} else {
						noDeter++;
					}
				}
			}
		}
		// System.out.println("Deter: " + deter);
		// System.out.println("noDeter: " + noDeter);

		if (ret == 0) {
			/* set the objective function */
			m = 0;
			for (int i = 0; i < spans.size(); i++) {
				for (int j = i + 1; j < spans.size(); j++) {
					// if (spans.get(i).isPronoun || spans.get(j).isPronoun) {
					int zij = nameMap.get("z(" + i + "," + j + ")");
					Double pij = OProb.get(i + "-" + j);
					if (pij != null) {
						colno[m] = zij;
						// row[m++] = pij;
						row[m++] = 2 * pij.doubleValue() - 1;
					}
					// }
					// obj.put(zij, pij);
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
			System.out.println("=============");
			// /* a solution is calculated, now lets get some results */
			// /* objective value */
			// System.err.println("Objective value: " + lp.getObjective());
			//
			// /* variable values */
			// lp.getVariables(row);
			//
			// double sum = 0;
			// for (Integer key : obj.keySet()) {
			// double time = obj.get(key);
			// double term = time * row[key.intValue() - 1];
			// sum += term;
			// }
			// System.err.println("left:\t" + sum);
			// System.err.println("right:\t" + (lp.getObjective() - sum));
			// for (m = 0; m < Ncol; m++) {
			// System.out.println(lp.getColName(m + 1) + ": " + row[m]);
			//
			// String name = lp.getColName(m + 1);
			// int a = name.indexOf("(");
			// int b = name.indexOf(")");
			// String content = name.substring(a + 1, b);
			// double value = row[m];
			// if (name.startsWith("x")) {
			// int idx = Integer.valueOf(content);
			// if (value == 0) {
			// mentions.get(idx).confidence = -1;
			// } else {
			// mentions.get(idx).confidence = 1;
			// }
			// } else if (name.startsWith("y")) {
			// String tokens[] = content.split(",");
			// int idx = Integer.valueOf(tokens[0]);
			// String subType = ACECommon.subTypes.get(Integer
			// .parseInt(tokens[1]) - 1);
			// if (value == 1) {
			// mentions.get(idx).subType = subType;
			// }
			// } else if (name.startsWith("z")) {
			// String tokens[] = content.split(",");
			// EventMention m1 = mentions.get(Integer.parseInt(tokens[0]));
			// EventMention m2 = mentions.get(Integer.parseInt(tokens[1]));
			// EventMention pair[] = new EventMention[2];
			// pair[0] = m1;
			// pair[1] = m2;
			// if (value == 1 && !m1.subType.equals("null")
			// && !m2.subType.equals("null")) {
			// this.corefOutput.put(pair, 1);
			// if (!m1.subType.equals(m2.subType) || m1.confidence < 0
			// || m2.confidence < 0) {
			// System.err
			// .println("GEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
			// }
			// } else {
			// this.corefOutput.put(pair, -1);
			// }
			// }
			// }
			// for (int i = 0; i < s; i++) {
			// EventMention mention = mentions.get(i);
			// if (mention.confidence > 0 && mention.subType.equals("null")) {
			// System.err.println("GEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
			// } else if (mention.confidence < 0
			// && !mention.subType.equals("null")) {
			// System.err.println("GEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
			// }
			// }
			/* we are done now */
		}

		/* clean up such that all used memory by lp_solve is freeed */
		if (lp.getLp() != 0)
			lp.deleteLp();
		return (ret);
	}

	public static void main(String args[]) throws Exception {
		if (args.length != 1) {
			System.out.println("java ~ lang");
			System.exit(1);
		}
		String path = "/users/yzcchen/chen3/ijcnlp2013/ilp/" + args[0] + "/";

		ilpFiles = new Vector<String>();

		for (File f : (new File(path)).listFiles()) {
			if (f.getAbsolutePath().endsWith(".ilp")) {
				ilpFiles.add(f.getAbsolutePath());
			}
		}
		for (int i = 0; i < 5; i++) {
			Thread th = new ILP(i);
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
