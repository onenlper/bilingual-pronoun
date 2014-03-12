package setting;

import java.util.ArrayList;

import util.Common;
import util.Util;

public class CreateSTSetting {

	public static void main(String args[]) {
		
		getCoNLLs("engST.train", "engCoNLL.train.2", true, "eng");
		getCoNLLs("engST.test", "engCoNLL.test.2", false, "eng");

		
		getCoNLLs("chiST.train", "chiCoNLL.train.2", true, "chi");
		getCoNLLs("chiST.test", "chiCoNLL.test.2", false, "chi");
		
		getParallelInParalle();
//		getMTInParallel();
//		getMTCoNLL("chiST.train", "MT.chiCoNLL.train.3", "chi");
//		getMTCoNLL("engST.train", "MT.engCoNLL.train.3", "eng");
		
		getMTCoNLL("chiST.test", "MT.chiCoNLL.test.2", "chi");
		getMTCoNLL("engST.test", "MT.engCoNLL.test.2", "eng");
	}
	
	static void getParallelInParalle() {
		ArrayList<String> paraAll = Common.getLines("parallelMap.all");
		
		ArrayList<String> chiConllFiles = new ArrayList<String>();
		ArrayList<String> engConllFiles = new ArrayList<String>();
		
		for(String para : paraAll) {
			String tks[] = para.split("#");
			String chID = tks[0].trim();
			String enID = tks[1].trim();
			
			chiConllFiles.addAll(Common.getLines(Util.getFullPath(enID, "eng", true)));
			engConllFiles.addAll(Common.getLines(Util.getFullPath(chID, "chi", true)));
			
		}
		
		Common.outputLines(chiConllFiles, "MT.chiCoNLL.train.2");
		Common.outputLines(engConllFiles, "MT.engCoNLL.train.2");
	}
	
	static void getMTInParallel() {
		ArrayList<String> paraAll = Common.getLines("parallelMap.all");
		ArrayList<String> paraGlobal = Common.getLines("parallelMap");
		
		ArrayList<String> chiConllFiles = new ArrayList<String>();
		ArrayList<String> engConllFiles = new ArrayList<String>();
		
		for(String para : paraAll) {
			int ID = paraGlobal.indexOf(para);
			chiConllFiles.addAll(Common.getLines(Util.getMTPath(ID, "chi")));
			engConllFiles.addAll(Common.getLines(Util.getMTPath(ID, "eng")));
		}
		
		Common.outputLines(chiConllFiles, "MT.chiCoNLL.train.3");
		Common.outputLines(engConllFiles, "MT.engCoNLL.train.3");
	}
	
	static void getMTCoNLL(String list, String outputPath, String lang) {
		ArrayList<String> allIDs = new ArrayList<String>();
		ArrayList<String> allFiles = null;
		if(lang.equalsIgnoreCase("chi")) {
			allFiles = Common.getLines("chinese_list_all");
		} else {
			allFiles = Common.getLines("english_list_all");
		}
		for(String file : allFiles) {
			String ID = Util.getID(file);
			allIDs.add(ID);
		}
		String base = "/users/yzcchen/chen3/ijcnlp2013/googleMTALL/" + lang + "_MT/conll/";

		ArrayList<String> output = new ArrayList<String>();
		
		for(String file : Common.getLines(list)) {
			String ID = Util.getID(file);
			int i = allIDs.indexOf(ID);
			String path = base + i + ".conll";
			output.addAll(Common.getLines(path));
		}
		
		Common.outputLines(output, outputPath);
	}
	
	private static void getCoNLLs(String list, String outputPath, boolean train, String lang) {
		ArrayList<String> files = Common.getLines(list);
		ArrayList<String> output = new ArrayList<String>();
		for(String file : files) {
			String ID = Util.getID(file);
			ArrayList<String> conll = Common.getLines(Util.getFullPath(ID, lang, train));
			
			if(!train) {
				ArrayList<String> goldConll = Common.getLines(Util.getFullPath(ID, lang, true));
				CreateTrainTestFile.replaceGoldCorefColumn(conll, goldConll);
			}
			output.addAll(conll);
		}
		Common.outputLines(output, outputPath);
	}
}
