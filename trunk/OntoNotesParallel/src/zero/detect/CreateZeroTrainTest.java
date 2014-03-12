package zero.detect;

import java.util.ArrayList;
import java.util.HashSet;

import util.Common;
import util.Util;

public class CreateZeroTrainTest {

	public static void main(String args[]) {
		ArrayList<String> paraTest = Common.getLines("parallelMap.test.1");

		ArrayList<String> allChinese = Common.getLines("chinese_list_all");

		HashSet<String> chTest= new HashSet<String>();
		for(String line : paraTest) {
			String tks[] = line.split("#");
			chTest.add(tks[0].trim());
		}
		
		// train list chi # chi
		ArrayList<String> trainList = new ArrayList<String>();
		for (String line : allChinese) {
			String ID = Util.getID(line);
			if(!chTest.contains(ID)) {
				trainList.add(ID + " # " + ID);
			}
		}
//		Common.outputLines(trainList, "zero.train.1");
		
		if(true) {
			return;
		}
		// test list chi # eng
		ArrayList<String> testList = new ArrayList<String>();
		testList.addAll(paraTest);
//		Common.outputLines(testList, "zero.test.1");

		// english train
		ArrayList<String> englishTrainConll = new ArrayList<String>();
		HashSet<String> engInTest = new HashSet<String>();
		for (String line : paraTest) {
			String tks[] = line.split("#");
			engInTest.add(tks[1].trim());
		}
		ArrayList<String> allEnglish = Common.getLines("english_list_all");
		for (String english : allEnglish) {
			String ID = Util.getID(english);
			if (!engInTest.contains(ID)) {
				englishTrainConll.addAll(Common.getLines(Util.getFullPath(ID,
						"eng", true)));
			}
		}
		// Common.outputLines(englishTrainConll, "eng_train_zero/train.conll");

		// english test
		ArrayList<String> allGoldTest = new ArrayList<String>();
		ArrayList<String> allSysTest = new ArrayList<String>();

		ArrayList<String> lineNos = new ArrayList<String>();

		for (int i = 0; i < testList.size(); i++) {
			String ID = testList.get(i).split("#")[1].trim();
			ArrayList<String> goldCoNLL = Common.getLines(Util.getFullPath(ID,
					"eng", true));
//			Common.outputLines(goldCoNLL, "eng_gold_zero/" + i + ".conll");

			allGoldTest.addAll(goldCoNLL);

			ArrayList<String> sysCoNLL = Common.getLines(Util.getFullPath(ID,
					"eng", false));
//			Common.outputLines(sysCoNLL, "eng_sys_zero/" + i + ".conll");
			allSysTest.addAll(sysCoNLL);

			lineNos.add(Integer.toString(sysCoNLL.size()));
		}
//		Common.outputLines(allGoldTest, "eng_train_zero/test.conll");
//		Common.outputLines(allSysTest, "eng_train_zero/test.conll.sys");
//		// folder
//		Common.outputLines(lineNos, "eng_train_zero/lineNos");

		ArrayList<String> lineNos2 = new ArrayList<String>();
		ArrayList<String> paraGlobal = Common.getLines("parallelMap");
		ArrayList<String> allMT = new ArrayList<String>();
		for (String para : paraTest) {
			int id = paraGlobal.indexOf(para);
			ArrayList<String> mtCoNLL = Common
					.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTRED/chi_MT/conll/"
							+ id + ".conll");
			allMT.addAll(mtCoNLL);
//			Common.outputLines(mtCoNLL, "eng_mt_zero/" + paraTest.indexOf(para) + ".conll");
			lineNos2.add(Integer.toString(mtCoNLL.size()));
		}
//		Common.outputLines(lineNos2, "eng_mt_zero/lineNos");
//		Common.outputLines(allMT, "eng_mt_zero/test.conll.mt");
		
	}
}
