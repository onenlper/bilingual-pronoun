package zero.detect;

import java.io.File;
import java.util.ArrayList;

import util.Common;

public class Split {
	public static void main(String args[]) {
		String outputPath = "eng_mt_zero/";
		ArrayList<String> lineNos = Common.getLines("eng_mt_zero//lineNos");
		ArrayList<String> allFiles = Common.getLines("eng_train_zero/test.mt.output");

		int current = 0;
		for (int i = 0; i < lineNos.size(); i++) {
			int size = Integer.parseInt(lineNos.get(i));

			ArrayList<String> out = new ArrayList<String>(allFiles.subList(
					current, current + size));

			current += size;

			Common.outputLines(out, outputPath + File.separator + i + ".conll");
		}
		
		if(current!=allFiles.size()) {
			Common.bangErrorPOS("");
		}
	}
}
