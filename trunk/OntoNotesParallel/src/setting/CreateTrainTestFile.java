package setting;

import java.util.ArrayList;

import util.Common;
import util.Util;

public class CreateTrainTestFile {

	public static void main(String args[]) {
		// train
		for (int i = 0; i < 5; i++) {
			if(i!=1) {
				continue;
			}
			ArrayList<String> chiConllFiles = new ArrayList<String>();
			ArrayList<String> engConllFiles = new ArrayList<String>();
			ArrayList<String> paras = Common.getLines("parallelMap.train." + i);
			for (String para : paras) {
				String tokens[] = para.split("#");
				chiConllFiles.addAll(Common.getLines(Util.getFullPath(
						tokens[0].trim(), "chi", true)));
				engConllFiles.addAll(Common.getLines(Util.getFullPath(
						tokens[1].trim(), "eng", true)));
			}
			Common.outputLines(chiConllFiles, "chiCoNLL.train." + i);
			Common.outputLines(engConllFiles, "engCoNLL.train." + i);
		}
		// test
		ArrayList<String> chiOverallKey = new ArrayList<String>();
		ArrayList<String> engOverallKey = new ArrayList<String>();

		ArrayList<String> chiOverallTest = new ArrayList<String>();
		ArrayList<String> engOverallTest = new ArrayList<String>();

		for (int i = 0; i < 5; i++) {
			if(i!=1) {
				continue;
			}
			ArrayList<String> chiConllFiles = new ArrayList<String>();
			ArrayList<String> engConllFiles = new ArrayList<String>();

			ArrayList<String> chiKeyConllFiles = new ArrayList<String>();
			ArrayList<String> engKeyConllFiles = new ArrayList<String>();

			ArrayList<String> paras = Common.getLines("parallelMap.test." + i);
			for (String para : paras) {
				String tokens[] = para.split("#");

				ArrayList<String> sysChiLines = Common.getLines(Util
						.getFullPath(tokens[0].trim(), "chi", false));
				ArrayList<String> sysEngLines = Common.getLines(Util
						.getFullPath(tokens[1].trim(), "eng", false));

				ArrayList<String> goldChiLines = Common.getLines(Util
						.getFullPath(tokens[0].trim(), "chi", true));
				ArrayList<String> goldEngLines = Common.getLines(Util
						.getFullPath(tokens[1].trim(), "eng", true));

				replaceGoldCorefColumn(sysChiLines, goldChiLines);
				replaceGoldCorefColumn(sysEngLines, goldEngLines);

				chiConllFiles.addAll(sysChiLines);
				engConllFiles.addAll(sysEngLines);

				chiOverallTest.addAll(sysChiLines);
				engOverallTest.addAll(sysEngLines);

				chiOverallKey.addAll(goldChiLines);
				engOverallKey.addAll(goldEngLines);

				chiKeyConllFiles.addAll(goldChiLines);
				engKeyConllFiles.addAll(goldEngLines);

			}
			Common.outputLines(chiConllFiles, "chiCoNLL.test." + i);
			Common.outputLines(engConllFiles, "engCoNLL.test." + i);

			Common.outputLines(chiKeyConllFiles, "chiCoNLL.key." + i);
			Common.outputLines(engKeyConllFiles, "engCoNLL.key." + i);
		}
		Common.outputLines(chiOverallKey, "chiCoNLL.key");
		Common.outputLines(engOverallKey, "engCoNLL.key");

		Common.outputLines(chiOverallTest, "chiCoNLL.test");
		Common.outputLines(engOverallTest, "engCoNLL.test");
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
