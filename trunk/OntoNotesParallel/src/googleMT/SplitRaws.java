package googleMT;

import java.util.ArrayList;

import util.Common;

public class SplitRaws {

	static String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/";

	public static void main(String args[]) {
		ArrayList<String> lineNos = Common.getLines(base + "lineNos");
		ArrayList<String> engs = Common.getLines(base + "docs/docs.e");
		ArrayList<String> chis = Common.getLines(base + "docs/docs.f");

		if(chis.size()!=engs.size()) {
			Common.bangErrorPOS(chis.size() + "#" + engs.size());
		}
		
		int current = 0;

		for (int i = 0; i < lineNos.size(); i++) {
//			int size = Integer.parseInt(lineNos.get(i).split(":")[1].trim());
			int size = Integer.parseInt(lineNos.get(i));
			ArrayList<String> chi = new ArrayList<String>(chis.subList(current,
					current + size));

			ArrayList<String> eng = new ArrayList<String>(engs.subList(current,
					current + size));

			Common.outputLines(chi, base + "/raws/" + i + ".chi");
			Common.outputLines(eng, base + "/raws/" + i + ".eng");
			current += size;
		}
	}
}
