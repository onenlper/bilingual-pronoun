package invest;

import java.util.ArrayList;
import java.util.HashSet;

import util.Common;
import util.Util;

public class FindAllOtherDocs {

	static String annotationsStr = "annotations/";

	public static void main(String args[]) {
		ArrayList<String> allChi = Common.getLines("chinese_list_all");
		ArrayList<String> allEng = Common.getLines("english_list_all");

		ArrayList<String> paraTest = Common.getLines("parallelMap.test.1");

		HashSet<String> chiTest = new HashSet<String>();
		HashSet<String> engTest = new HashSet<String>();
		for (String line : paraTest) {
			String tks[] = line.trim().split("#");
			chiTest.add(tks[0].trim());
			engTest.add(tks[1].trim());
		}

		ArrayList<String> chiTrainOther = new ArrayList<String>();
		for (String chiLine : allChi) {
			int start = chiLine.indexOf("annotations/")
					+ annotationsStr.length();
			int dot = chiLine.lastIndexOf(".");
			String id = chiLine.substring(start, dot);
			if (!chiTest.contains(id)) {
				chiTrainOther.add(id);
			} else {
				System.out.println("Skip: " + id);
			}
		}
		Common.outputLines(chiTrainOther, "chiOther");

		ArrayList<String> chiTrains = new ArrayList<String>();
		for (String chiTr : chiTrainOther) {
			chiTrains.addAll(Common.getLines(Util.getFullPath(chiTr, "chi",
					true)));
		}
		Common.outputLines(chiTrains, "chiCoNLL.train.0");

		ArrayList<String> mtChiTrain = new ArrayList<String>();
		ArrayList<String> mtChiTest = new ArrayList<String>();
		int size = 0;
		for (int i = 0; i < allChi.size(); i++) {
			String id = Util.getID(allChi.get(i));

			if (!chiTest.contains(id)) {
				mtChiTrain
						.addAll(Common
								.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/conll/"
										+ i + ".conll"));
				size++;
			} else {
				mtChiTest.addAll(Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/conll/"
								+ i + ".conll"));
			}
		}
		System.out.println(size);
		Common.outputLines(mtChiTest, "MT.chiCoNLL.test.0");
		Common.outputLines(mtChiTrain, "MT.chiCoNLL.train.0");

		ArrayList<String> engTrainOther = new ArrayList<String>();
		for (String engLine : allEng) {
			int start = engLine.indexOf("annotations/")
					+ annotationsStr.length();
			int dot = engLine.lastIndexOf(".");
			String id = engLine.substring(start, dot);
			if (!engTest.contains(id)) {
				engTrainOther.add(id);
			} else {
//				System.out.println("Skip: " + id);
			}
		}
//		Common.outputLines(engTrainOther, "engOther");

		ArrayList<String> engTrains = new ArrayList<String>();
		for (String engTr : engTrainOther) {
			engTrains.addAll(Common.getLines(Util.getFullPath(engTr, "eng",
					true)));
		}
		 Common.outputLines(engTrains, "engCoNLL.train.0");
		
		ArrayList<String> mtEngTrain = new ArrayList<String>();
		ArrayList<String> mtEngTest = new ArrayList<String>();
		size = 0;
		for (int i = 0; i < allEng.size(); i++) {
			String id = Util.getID(allEng.get(i));

			if (!engTest.contains(id)) {
				mtEngTrain
						.addAll(Common
								.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/eng_MT/conll/"
										+ i + ".conll"));
				size++;
			} else {
				mtEngTest.addAll(Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/eng_MT/conll/"
								+ i + ".conll"));
			}
		}
		System.out.println(size);
		System.out.println(engTest.size());
		Common.outputLines(mtEngTrain, "MT.engCoNLL.train.0");
		Common.outputLines(mtEngTest, "MT.engCoNLL.test.0");
		
	}
}
