package align;

import java.io.File;
import java.util.ArrayList;

import util.Common;

public class SplitGizaOutput {

	public static void split(String gizaOutput, String lineNumber,
			String outputFolder) {
		ArrayList<String> contents = Common.getLines(gizaOutput);

		ArrayList<String> lineNumbers = Common.getLines(lineNumber);
		int k = 0;
		for (int i = 0; i < lineNumbers.size(); i++) {
			String numberStr = lineNumbers.get(i).trim();
			int number = Integer.parseInt(numberStr.substring(numberStr
					.lastIndexOf(' ') + 1));
			k += number;
			ArrayList<String> content = new ArrayList<String>(contents.subList(
					0, number * 3));
			for (int j = 0; j < number * 3; j++) {
				contents.remove(0);
			}
			Common.outputLines(content, outputFolder + File.separator + i
					+ ".align");
		}
		System.out.println(contents.size());
		System.out.println(k);
	}

	static String wordAlignBase;
	
	public static void main(String args[]) {
		if(args.length!=1) {
			System.out.println("java ~ wordAlignBase");
			System.exit(1);
		}
		wordAlignBase = args[0];
		
		String gizaOutput = wordAlignBase + File.separator + "yzcchen.A3.final.co";
		String lineNo = "/users/yzcchen/chen3/ijcnlp2013/wordAlign/tokenBase/lineNos";
		String gizaTokenMap = wordAlignBase + File.separator +  "/align/";
		split(gizaOutput, lineNo, gizaTokenMap);

	}
}
