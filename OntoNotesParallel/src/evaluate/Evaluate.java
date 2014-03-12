package evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Common;

public class Evaluate {

	static String lang = "";

	public static void main(String args[]) {
		if (args.length == 0) {
			System.out.println("java ~ chi|eng close|best|enclose");
			System.exit(1);
		}

		String skipStr = "nw/xinhua/00/chtb_0029, "
				+ "bc/phoenix/00/phoenix_0009, " + "nw/xinhua/00/chtb_0079, "
				+ "nw/xinhua/01/chtb_0179, " + "nw/xinhua/02/chtb_0229, "
				+ "wb/cmn/00/cmn_0009, " + "nw/xinhua/01/chtb_0129, "
				+ "nw/xinhua/02/chtb_0279";
		String tks[] = skipStr.split(",");
		HashSet<String> skipSet = new HashSet<String>();
		for (String tk : tks) {
			// skipSet.add(tk.trim().replace("/", "-"));
		}

		lang = args[0];
		String mode = args[1];
		String base = "/users/yzcchen/chen3/ijcnlp2013/ilp/";

		double goldA = 0;
		double goldNA = 0;
		double hitA = 0;
		double hitNA = 0;

		double sysA = 0;

		double chiA = 0;
		double chiNA = 0;
		double engA = 0;
		double engNA = 0;
		double mixA = 0;
		double mixNA = 0;

		int i = 0;
		if (args.length == 3) {
			i = Integer.parseInt(args[2]);
		}
		// for (; i < 5; i++) {
		int fID = 0;
		File folder = new File(base + lang + i);
		for (File file : folder.listFiles()) {
			if (file.getAbsolutePath().endsWith(".ilp")) {
				fID++;
				if (fID % 5 != 4) {
					// continue;
				}
				String ilpFile = file.getAbsolutePath();
				int k = ilpFile.lastIndexOf(".");

				int a = ilpFile.lastIndexOf(File.separator);
				int b = ilpFile.indexOf(".");
				String stem = ilpFile.substring(a + 1, b);
				if (skipSet.contains(stem)) {
					// System.out.println("skip " + stem);
					continue;
				}

				String goldFile = ilpFile.substring(0, k) + ".gold";

				HashMap<String, HashSet<String>> goldMaps = loadGold(goldFile);
				HashMap<String, String> sysMap = null;

				HashMap<String, HashSet<String>> chiMap = loadClosest2(ilpFile);
				HashMap<String, HashSet<String>> engMap = loadXLang2(ilpFile);
				HashMap<String, HashSet<String>> mixMap = loadCombine2(ilpFile);

				if (mode.equalsIgnoreCase("close")) {
					sysMap = loadClosest(ilpFile);
				} else if (mode.equalsIgnoreCase("best")) {
					sysMap = loadBest(ilpFile);
				} else if (mode.equalsIgnoreCase("combine")) {
					sysMap = loadCombine(ilpFile);
				} else if (mode.equalsIgnoreCase("xlang")) {
					sysMap = loadXLang(ilpFile);
				} else if (mode.equalsIgnoreCase("xLangC")) {
					sysMap = loadXLangC(ilpFile);
				} else if (mode.equalsIgnoreCase("enclose2")) {
					sysMap = loadClosestConfEnsemble(ilpFile);
				} else if (mode.equalsIgnoreCase("enclose3")) {
					sysMap = loadClosestConfEnsemble(ilpFile);
				} else if (mode.equalsIgnoreCase("3vote")) {
					sysMap = loadClosestEnsembleVote(ilpFile);
				} else if (mode.equalsIgnoreCase("enclose5")) {
					sysMap = loadClosestEnsembleAll2(ilpFile);
				} else if (mode.equalsIgnoreCase("ilp")) {
					sysMap = loadClosest(ilpFile + ".out");
				} else if (mode.equalsIgnoreCase("3avg")) {
					sysMap = load3Avg(ilpFile);
				} else if (mode.equalsIgnoreCase("hard")) {
					sysMap = loadHard(ilpFile);
				} else if (mode.equalsIgnoreCase("combineH")) {
					sysMap = loadCombineHard(ilpFile);
				} else if (mode.equalsIgnoreCase("combineC")) {
					sysMap = loadCombineC(ilpFile);
				} else if(mode.equalsIgnoreCase("tune")) {
					sysMap = loadTune(ilpFile);
				}
				sysA += sysMap.size();

				for (String key : goldMaps.keySet()) {
					HashSet<String> goldAnts = goldMaps.get(key);
					String sysAnt = sysMap.get(key);

					HashSet<String> chiAnt = chiMap.get(key);
					HashSet<String> engAnt = engMap.get(key);
					HashSet<String> mixAnt = mixMap.get(key);

					if (goldMaps.get(key).size() == 0) {
						goldNA++;
						// System.out.println(key);
						// System.out.println(ilpFile);
						if (sysAnt == null) {
							hitNA++;
							if (chiAnt == null) {
								chiNA++;
							}
							if (engAnt == null) {
								engNA++;
							}
							if (mixAnt == null) {
								mixNA++;
							}
						}
					} else {
						goldA++;
						if (sysAnt != null && goldAnts.contains(sysAnt)) {
							hitA++;
							if (chiAnt != null && chiAnt.contains(sysAnt)) {
								chiA++;
							}
							if (engAnt != null && engAnt.contains(sysAnt)) {
								engA++;
							}
							if (mixAnt != null && mixAnt.contains(sysAnt)) {
								mixA++;
							}
						}
					}
				}
			}
		}
		// if (args.length == 3) {
		// break;
		// }
		// }

		double rec = hitA / goldA;
		double pre = hitA / sysA;
		System.out.format("Rec: %f / %f = %f\n", hitA, goldA, hitA / goldA
				* 100);
		System.out.format("Pre: %f / %f = %f\n", hitA, sysA, hitA / sysA * 100);
		System.out.format("F-1: %f\n", 2 * rec * pre / (rec + pre) * 100);

		System.out.println("Correct Non-Anaphor: " + hitNA / goldNA * 100);

		System.out.format("Accu: (%f + %f) / (%f + %f) = %f\n", hitA, hitNA,
				goldA, goldNA, (hitA + hitNA) / (goldA + goldNA) * 100);
		// System.out
		// .format("Metric 2: %f / %f = %f\n", hitA, goldA, hitA / goldA * 100);
		System.out.println("ChiA: " + chiA + " EngA: " + engA + " mixA: "
				+ mixA);
		System.out.println("ChiA: " + chiA/2417 + " EngA: " + engA/2417 + " mixA: "
				+ mixA/2417);
		System.out.println("ChiNA: " + chiNA + " EngNA: " + engNA + " mixNA: "
				+ mixNA);
		System.out.println("ChiNA: " + chiNA/1274 + " EngNA: " + engNA/1274 + " mixNA: "
				+ mixNA/1274);
	}

	static Pattern mentionP = Pattern.compile("([^\\s]*)\\s(.*)");

	static Pattern probP = Pattern
			.compile("([^\\-]*)-([^\\-]*)-([^\\s]*)\\s(.)");

	public static HashMap<String, String> loadClosestConfEnsemble(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						double enProb = Double.parseDouble(lines.get(i + 2)
								.substring(a, b));

						if ((prob - .5) * (enProb - .5) < 0) {

							int op = (int) (prob * 10);
							int xp = (int) (enProb * 10);
							String key = op + "-" + xp;
							String keys = "1-7,2-5,2-6,2-7,2-8,3-5,3-6,3-7,3-8,3-9,4-5,4-6,4-7,4-8,5-1,5-2,5-3,5-4,6-0,6-1,6-2,6-3,6-4,7-2,7-4,";
							if (keys.contains(key)) {
								prob = enProb;
							}
						}
					}

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);

					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadXLangC(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						prob = Double.parseDouble(lines.get(i + 1).substring(a,
								b));

					}

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);
					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadXLang(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						double prob = Double.parseDouble(lines.get(i + 1)
								.substring(a, b));

						if (model.equalsIgnoreCase("o") && prob > 0.5
								&& !map.containsKey(anaphor)) {
							// if(map.containsKey(ant)) {
							// map.put(anaphor, map.get(ant));
							// } else if(!pronouns.contains(ant))
							map.put(anaphor, ant);
						}
					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}
	
	public static HashMap<String, HashSet<String>> loadXLang2(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						double prob = Double.parseDouble(lines.get(i + 1)
								.substring(a, b));

						if (model.equalsIgnoreCase("o") && prob > 0.5
								) {
							// if(map.containsKey(ant)) {
							// map.put(anaphor, map.get(ant));
							// } else if(!pronouns.contains(ant))
							HashSet<String> ants = map.get(anaphor);
							if(ants==null) {
								ants = new HashSet<String>();
								map.put(anaphor, ants);
							}
							ants.add(ant);
						}
					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadCombineC(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						prob = Double.parseDouble(lines.get(i + 2).substring(a,
								b));
					}
					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);
					}
				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}
	
	public static HashMap<String, HashSet<String>> loadCombine2(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						prob = Double.parseDouble(lines.get(i + 2).substring(a,
								b));

						if (model.equalsIgnoreCase("o") && prob > 0.5) {
							// if(map.containsKey(ant)) {
							// map.put(anaphor, map.get(ant));
							// } else if(!pronouns.contains(ant))
							HashSet<String> ants = map.get(anaphor);
							if(ants==null) {
								ants = new HashSet<String>();
								map.put(anaphor, ants);
							}
							ants.add(ant);
						}
					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadCombine(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						prob = Double.parseDouble(lines.get(i + 2).substring(a,
								b));

						if (model.equalsIgnoreCase("o") && prob > 0.5
								&& !map.containsKey(anaphor)) {
							// if(map.containsKey(ant)) {
							// map.put(anaphor, map.get(ant));
							// } else if(!pronouns.contains(ant))
							map.put(anaphor, ant);
						}
					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadClosestEnsembleVote(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);

					double xprob = prob;
					double eprob = prob;

					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						xprob = Double.parseDouble(lines.get(i + 1).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						eprob = Double.parseDouble(lines.get(i + 2).substring(
								a, b));
					}

					if ((xprob - .5) * (prob - .5) < 0
							&& (eprob - .5) * (prob - .5) < 0) {
						prob = xprob;
					}

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);

					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadCombineHard(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);

					double xprob = prob;
					double eprob = prob;

					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						xprob = Double.parseDouble(lines.get(i + 1).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						eprob = Double.parseDouble(lines.get(i + 2).substring(
								a, b));
					}

					prob = (prob + eprob) / 2;

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);

					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadHard(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);

					double xprob = prob;
					double eprob = prob;

					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						xprob = Double.parseDouble(lines.get(i + 1).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						eprob = Double.parseDouble(lines.get(i + 2).substring(
								a, b));
					}

					prob = (prob + xprob) / 2;

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);

					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> load3Avg(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);

					double xprob = prob;
					double eprob = prob;

					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						xprob = Double.parseDouble(lines.get(i + 1).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						eprob = Double.parseDouble(lines.get(i + 2).substring(
								a, b));
					}

					prob = (prob + xprob + eprob) / 3;

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);
					}
				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}
	
	public static HashMap<String, String> loadTune(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, Double> alignCach = new HashMap<String, Double>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
					int k = line.lastIndexOf("@");
					double alignProb = Double.parseDouble(line.substring(k+1));
					alignCach.put(mStr, alignProb);
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double oprob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					
					double xprob = oprob;
					double eprob = oprob;

					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						xprob = Double.parseDouble(lines.get(i + 1).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						eprob = Double.parseDouble(lines.get(i + 2).substring(
								a, b));
					}
					double antAlign = alignCach.get(ant);
					double anaphorAlign = alignCach.get(anaphor);

					double align = antAlign > anaphorAlign ? anaphorAlign
							: antAlign;
					
					double prob;
//					String k = wX + "," + wE + "\t" + pX + "," + pE + "\t" + cO + ","
//							+ cX + "," + cE;
//					0.9,0.0  0.8,1.0 0.2,0.13,0.23
					double wX = 0.9;
					double wE = 0;
					double pX = 0.8;
					double pE = 1.0;
					double cO = 0.2;
					double cX = 0.13;
					double cE = 0.23;
					if (.5 - Math.abs(.5 - oprob) < cO) {
						prob = oprob;
					} else if (.5 - Math.abs(.5 - xprob) < cX) {
						prob = xprob;
					} else if (.5 - Math.abs(.5 - eprob) < cE) {
						prob = eprob;
					} else {
//						prob = (oprob + xprob * wX * Math.pow(align, pX) + eprob
//								* wE * Math.pow(align, pE))
//								/ (wX + wE + 1);
						
						prob = (oprob + xprob * wX * Math.pow(align, pX) + eprob
								* wE * Math.pow(align, pE))
								/ (1 + wX * Math.pow(align, pX) + wE * Math.pow(align, pE));
					}

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);
					}
				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadClosestEnsembleAll2(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.startsWith("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);

					double xprob = prob;
					double eprob = prob;
					double oxprob = prob;

					if (model.equalsIgnoreCase("o") && i + 1 < lines.size()
							&& lines.get(i + 1).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 1).endsWith("X")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 1).lastIndexOf("\t");
						xprob = Double.parseDouble(lines.get(i + 1).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 2 < lines.size()
							&& lines.get(i + 2).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 2).endsWith("E")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 2).lastIndexOf("\t");
						eprob = Double.parseDouble(lines.get(i + 2).substring(
								a, b));
					}

					if (model.equalsIgnoreCase("o") && i + 3 < lines.size()
							&& lines.get(i + 3).startsWith(ant + "-" + anaphor)
							&& lines.get(i + 3).endsWith("S")) {
						int a = (ant + "-" + anaphor + "-").length();
						int b = lines.get(i + 3).lastIndexOf("\t");
						oxprob = Double.parseDouble(lines.get(i + 3).substring(
								a, b));
					}

					boolean pB = prob - .5 > 0;
					boolean xB = xprob - .5 > 0;
					boolean enB = eprob - .5 > 0;
					boolean oxB = oxprob - .5 > 0;

					String prTx = pronouns.get(anaphor);

					// "他", "他们","你", "你们","双方",
					// "其", "她", "它", "它们", "我", "我们",
					// "自己","谁", "这", "这个", "这里", "那"

					// true false false 他 35 63 0.5555555555555556
					// true false false 他们 45 8 5.625
					// true false false 你 84 26 3.230769230769231
					// true false false 你们 1 0 Infinity
					// true false false 其 8 2 4.0
					// true false false 双方 2 4 0.5
					// true false false 她 1 0 Infinity
					// true false false 它 16 3 5.333333333333333
					// true false false 它们 2 0 Infinity
					// true false false 我 29 68 0.4264705882352941
					// true false false 我们 51 26 1.9615384615384615
					// true false false 自己 25 4 6.25
					// true false false 谁 1 0 Infinity
					// true false false 这 28 1 28.0
					// true false false 这个 8 0 Infinity
					// true false false 这里 1 0 Infinity
					// true false false 那 6 0 Infinity

					HashSet<String> chiTFF = new HashSet<String>(Arrays.asList(
							"他", "他们", "你", "你们", "双方", "其", "她", "它", "它们",
							"我", "我们", "自己", "谁", "这", "这个", "这里", "那"));
					HashSet<String> chiFTT = new HashSet<String>(Arrays.asList(
							"我们", "你"));
					HashSet<String> chiTFT = new HashSet<String>(Arrays.asList(
							"我们", "这", "那", "那里"));

					HashSet<String> chiFTF = new HashSet<String>(
							Arrays.asList("他"));
					if (lang.equalsIgnoreCase("chi")
							&& ((pB == true && xB == false && enB == false && chiTFF
									.contains(prTx))
									|| (pB == false && xB == true
											&& enB == true && !chiFTT
												.contains(prTx))
									|| (pB == true && xB == false
											&& enB == true && chiTFT
												.contains(prTx)) || (pB == false
									&& xB == true && enB == false && chiFTF
										.contains(prTx)))) {
						prob = xprob;
					}

					// if (lang.equalsIgnoreCase("chi")
					// && ((pB == true && xB == false && enB == false) ||
					// (pB == false && xB == true && enB == true))) {
					// prob = eprob;
					// }

					HashSet<String> engTFF = new HashSet<String>(Arrays.asList(
							"his", "him"));
					HashSet<String> engFTT = new HashSet<String>(Arrays.asList(
							"it", "our", "them", "us", "we", "your"));
					HashSet<String> engFFT = new HashSet<String>(Arrays.asList(
							"i", "his", "he"));

					HashSet<String> engTFT = new HashSet<String>(Arrays.asList(
							"it", "we"));

					if (lang.equalsIgnoreCase("eng")
							&& ((pB == true && xB == false && enB == false && !engTFF
									.contains(prTx))
									|| (pB == false && xB == true
											&& enB == true && !engFTT
												.contains(prTx))
							// || (pB == false && xB == false && enB == true &&
							// engFFT.contains(prTx))
							|| (pB == true && xB == false && enB == true && engTFT
									.contains(prTx)))) {
						prob = xprob;
					}

					// if((xprob-.5)*(prob-.5)<0 && (eprob-.5)*(prob-.5)<0) {
					// prob = xprob;
					// }

					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);

					}

				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadClosest(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (String line : lines) {
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					String word = "#";
					if (type.length() > 1) {
						word = type.substring(2);
					}
					if (type.startsWith("p")) {
						pronouns.put(mStr, word);
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && prob > 0.5
							&& !map.containsKey(anaphor)) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						map.put(anaphor, ant);

					}
				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, HashSet<String>> loadClosest2(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, HashSet<String>> map = new HashMap<String, HashSet<String>>();

		for (String line : lines) {
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					String word = "#";
					if (type.length() > 1) {
						word = type.substring(2);
					}
					if (type.startsWith("p")) {
						pronouns.put(mStr, word);
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && prob > 0.5) {
						// if(map.containsKey(ant)) {
						// map.put(anaphor, map.get(ant));
						// } else if(!pronouns.contains(ant))
						HashSet<String> ants = map.get(anaphor);
						if (ants == null) {
							ants = new HashSet<String>();
							map.put(anaphor, ants);
						}
						ants.add(ant);
					}
				} else {
					Common.bangErrorPOS(line);
				}
			}
		}
		return map;
	}

	public static HashMap<String, String> loadBest(String file) {
		ArrayList<String> lines = Common.getLines(file);
		boolean collectMention = true;
		// TODO
		HashMap<String, String> pronouns = new HashMap<String, String>();
		HashMap<String, Double> confMap = new HashMap<String, Double>();
		HashMap<String, String> map = new HashMap<String, String>();

		for (String line : lines) {
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			if (collectMention) {
				Matcher m = mentionP.matcher(line);
				if (m.find()) {
					String mStr = m.group(1);
					String type = m.group(2).toLowerCase();
					if (type.equalsIgnoreCase("p")) {
						pronouns.put(mStr, type.substring(2));
					}
				} else {
					Common.bangErrorPOS(line);
				}
			} else {
				Matcher m = probP.matcher(line);
				if (m.find()) {
					String ant = m.group(1);
					String anaphor = m.group(2);
					double prob = Double.parseDouble(m.group(3));
					String model = m.group(4);
					if (model.equalsIgnoreCase("o") && prob > 0.5) {
						Double maxConf = confMap.get(anaphor);
						if (maxConf == null || maxConf.doubleValue() < prob) {
							confMap.put(anaphor, prob);
							map.put(anaphor, ant);

							// if(map.containsKey(ant)) {
							// map.put(anaphor, map.get(ant));
							// } else if(!pronouns.contains(ant)) {
							// map.put(anaphor, ant);
							// }
						}
					}
				} else {
					Common.bangErrorPOS("");
				}
			}
		}
		return map;
	}

	public static HashMap<String, HashSet<String>> loadGold(String filePath) {
		HashMap<String, HashSet<String>> goldMap = new HashMap<String, HashSet<String>>();
		ArrayList<String> lines = Common.getLines(filePath);

		HashSet<String> pronouns = new HashSet<String>();

		boolean collectMention = true;
		for (String line : lines) {
			if (line.startsWith("###")) {
				collectMention = false;
				continue;
			}
			String tokens[] = line.split("\\s+");
			if (collectMention) {
				if (tokens[1].equalsIgnoreCase("p")) {
					pronouns.add(tokens[0]);

					HashSet<String> ants = new HashSet<String>();
					goldMap.put(tokens[0], ants);

				}
			} else {
				for (int i = 0; i < tokens.length; i++) {
					String token = tokens[i];
					if (pronouns.contains(token)) {
						for (int j = 0; j < i; j++) {
							// if(!pronouns.contains(tokens[j])) {
							goldMap.get(token).add(tokens[j]);
							// }
						}
					}
				}
			}
		}
		return goldMap;
	}
}
