package evaluate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLWord;
import model.CoNLL.OntoCorefXMLReader;
import util.Common;
import util.Util;
import zero.detect.ZeroUtil;

public class EvaluateZ {

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
			skipSet.add(tk.trim().replace("/", "-"));
		}

		lang = args[0];
		String mode = args[1];
		String base = "/users/yzcchen/chen3/ijcnlp2013/ilp/";

		double goldA = 0;
		double hitA = 0;
		double sysA = 0;
		int i = 0;
		if (args.length == 3) {
			i = Integer.parseInt(args[2]);
		}
		HashMap<String, HashMap<String, HashSet<String>>> goldMapses = loadGold();
		// for (; i < 5; i++) {
		File folder = new File(base + lang + i);
		for (File file : folder.listFiles()) {
			if (file.getAbsolutePath().endsWith(".ilp")) {
				String ilpFile = file.getAbsolutePath();

				int a = ilpFile.lastIndexOf(File.separator);
				int b = ilpFile.lastIndexOf(".");
				String stem = ilpFile.substring(a + 1, b).replace(".", "_");

				HashMap<String, HashSet<String>> goldMaps = goldMapses
						.get(stem);

				HashMap<String, String> sysMap = null;

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
				} else if (mode.equalsIgnoreCase("3vote")) {
					sysMap = loadClosestEnsembleVote(ilpFile);
				} else if (mode.equalsIgnoreCase("3avg")) {
					sysMap = load3Avg(ilpFile);
				} else if (mode.equalsIgnoreCase("hard")) {
					sysMap = loadHard(ilpFile);
				} else if (mode.equalsIgnoreCase("combineH")) {
					sysMap = loadCombineHard(ilpFile);
				} else if (mode.equalsIgnoreCase("combineC")) {
					sysMap = loadCombineC(ilpFile);
				}
				sysA += sysMap.size();
				goldA += goldMaps.size();

				for (String key : goldMaps.keySet()) {
					HashSet<String> goldAnts = goldMaps.get(key);
					String sysAnt = sysMap.get(key);
					if (sysAnt != null 
							&& goldAnts.contains(sysAnt)
							) {
						hitA++;
					}
				}
			}
		}

		double rec = hitA / goldA;
		double pre = hitA / sysA;
		System.out.format("Rec: %f / %f = %f\n", hitA, goldA, hitA / goldA
				* 100);
		System.out.format("Pre: %f / %f = %f\n", hitA, sysA, hitA / sysA * 100);
		System.out.format("F-1: %f\n", 2 * rec * pre / (rec + pre) * 100);
	}

	static Pattern mentionP = Pattern.compile("([^\\s]*)\\s(.*)");

	static Pattern probP = Pattern
			.compile("([^\\-]*)-([^\\-]*)-([^\\s]*)\\s(.)");


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
						if(ant.endsWith(",0")) {
							if(map.containsKey(ant)) {
								map.put(anaphor, map.get(ant));
							}
						} else {
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
							if(ant.endsWith(",0")) {
								if(map.containsKey(ant)) {
									map.put(anaphor, map.get(ant));
								}
							} else {
								map.put(anaphor, ant);
							}
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
						if(ant.endsWith(",0")) {
							if(map.containsKey(ant)) {
								map.put(anaphor, map.get(ant));
							}
						} else {
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
							if(ant.endsWith(",0")) {
								if(map.containsKey(ant)) {
									map.put(anaphor, map.get(ant));
								}
							} else {
								map.put(anaphor, ant);
							}
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
						if(ant.endsWith(",0")) {
							if(map.containsKey(ant)) {
								map.put(anaphor, map.get(ant));
							}
						} else {
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
						if(ant.endsWith(",0")) {
							if(map.containsKey(ant)) {
								map.put(anaphor, map.get(ant));
							}
						} else {
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
						if(ant.endsWith(",0")) {
							if(map.containsKey(ant)) {
								map.put(anaphor, map.get(ant));
							}
						} else {
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
//						map.put(anaphor, ant);
						if(ant.endsWith(",0")) {
							if(map.containsKey(ant)) {
								map.put(anaphor, map.get(ant));
							}
						} else {
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
						if(ant.endsWith(",0")) {
							if(map.containsKey(ant)) {
								map.put(anaphor, map.get(ant));
							}
						} else {
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
							if(ant.endsWith(",0")) {
								if(map.containsKey(ant)) {
									map.put(anaphor, map.get(ant));
								}
							} else {
								map.put(anaphor, ant);
							}
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

	public static String getPosition(EntityMention m, CoNLLPart part) {
		int sid = 0, startid = 0, endid = 0;
		CoNLLWord sw = part.getWord(m.start);
		sid = sw.sentence.getSentenceIdx();
		startid = sw.indexInSentence + 1;
		if (m.end == -1) {
			endid = 0;
		} else {
			endid = part.getWord(m.end).indexInSentence + 1;
		}
		return sid + ":" + startid + "," + endid;
	}

	public static HashMap<String, HashMap<String, HashSet<String>>> loadGold() {
		HashMap<String, HashMap<String, HashSet<String>>> goldKeys = new HashMap<String, HashMap<String, HashSet<String>>>();
		ArrayList<String> files = Common.getLines("zero.test.1");
		for (String file : files) {
			String tks[] = file.split("#");
			CoNLLDocument d = new CoNLLDocument(Util.getFullPath(tks[0].trim(),
					"chi", true));
			OntoCorefXMLReader.addGoldZeroPronouns(d, false);
			for (CoNLLPart part : d.getParts()) {
				ArrayList<EntityMention> anaphorZeros = ZeroUtil
						.getAnaphorZeros(part.getChains());
				HashMap<String, HashSet<String>> goldKey = new HashMap<String, HashSet<String>>();
				goldKeys.put(part.getPartName(), goldKey);
//				System.out.println(part.getPartName());
				for (EntityMention zero : anaphorZeros) {
					HashSet<String> antStrs = new HashSet<String>();
					String zStr = getPosition(zero, part);
					goldKey.put(zStr, antStrs);
					for (EntityMention ant : zero.entity.mentions) {
						if (ant.end != -1) {
							antStrs.add(getPosition(ant, part));
						}
					}
				}
			}
		}
		return goldKeys;
	}
}
