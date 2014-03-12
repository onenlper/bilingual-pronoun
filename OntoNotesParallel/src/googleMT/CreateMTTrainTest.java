package googleMT;

import java.util.ArrayList;

import util.Common;
import util.Util;

public class CreateMTTrainTest {

	public static void main(String args[]) {
		ArrayList<String> allParaMaps = Common.getLines("parallelMap"); 
		// train
		for (int i = 0; i < 5; i++) {
			if(i!=1) {
				continue;
			}
			ArrayList<String> chiConllFiles = new ArrayList<String>();
			ArrayList<String> engConllFiles = new ArrayList<String>();
			ArrayList<String> paras = Common.getLines("parallelMap.train." + i);
			for (String para : paras) {
				int ID = allParaMaps.indexOf(para);
				chiConllFiles.addAll(Common.getLines(Util.getMTPath(ID, "chi")));
				engConllFiles.addAll(Common.getLines(Util.getMTPath(ID, "eng")));
			}
			Common.outputLines(chiConllFiles, "MT.chiCoNLL.train." + i);
			Common.outputLines(engConllFiles, "MT.engCoNLL.train." + i);
		}
		// test
		for (int i = 0; i < 5; i++) {
			if(i!=1) {
				continue;
			}
			ArrayList<String> chiConllFiles = new ArrayList<String>();
			ArrayList<String> engConllFiles = new ArrayList<String>();

			ArrayList<String> paras = Common.getLines("parallelMap.test." + i);
			for (String para : paras) {
				int ID = allParaMaps.indexOf(para);

				ArrayList<String> sysChiLines = Common.getLines(Util.getMTPath(ID, "chi"));
				ArrayList<String> sysEngLines = Common.getLines(Util.getMTPath(ID, "eng"));

				chiConllFiles.addAll(sysChiLines);
				engConllFiles.addAll(sysEngLines);

			}
			Common.outputLines(chiConllFiles, "MT.chiCoNLL.test." + i);
			Common.outputLines(engConllFiles, "MT.engCoNLL.test." + i);
		}
	}

	public static void replaceGoldCorefColumn(ArrayList<String> sysLines,
			ArrayList<String> goldLines) {

		int size = sysLines.size();
		for (int i = 0; i < size; i++) {
			String sysLine = sysLines.get(i);
			String goldLine = goldLines.get(i);

			if (sysLine.trim().isEmpty() || sysLine.startsWith("#")) {
				continue;
			} else {
				int k1 = sysLine.lastIndexOf(' ');
				if (k1 == -1) {
					k1 = sysLine.lastIndexOf('\t');
				}

				int k2 = goldLine.lastIndexOf(' ');
				if (k2 == -1) {
					k2 = goldLine.lastIndexOf('\t');
				}

				String goldCoref = goldLine.substring(k2 + 1);
				String newSysLine = sysLine.substring(0, k1 + 1) + goldCoref;
				sysLines.set(i, newSysLine);
			}
		}

	}

	public static ArrayList<String> coverCorefColumn(ArrayList<String> lines) {
		ArrayList<String> ret = new ArrayList<String>();
		for (String line : lines) {
			if (line.trim().isEmpty() || line.startsWith("#")) {
				ret.add(line);
			} else {
				int k = line.lastIndexOf(' ');
				if (k == -1) {
					k = line.lastIndexOf('\t');
				}
				ret.add(line.substring(0, k + 1) + "-");
			}
		}
		return ret;
	}
}
