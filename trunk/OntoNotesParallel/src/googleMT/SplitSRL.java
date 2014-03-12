package googleMT;

import java.util.ArrayList;

import util.Common;

public class SplitSRL {

	public static void main(String args[]) {
		String base = "/users/yzcchen/chen3/ijcnlp2013/googleMTRED/chi_MT/srl/";
		ArrayList<String> allLines = Common.getLines(base + "all.srl");
		ArrayList<String> counts = Common.getLines(base + "counts");

		int currentLineNo = 0;
		for (int i = 0; i < counts.size(); i++) {
			String count = counts.get(i);
			String tks[] = count.trim().split("\\s+");
			if (tks[1].equalsIgnoreCase("total") || tks[1].startsWith("_")) {
				continue;
			}
			int size = Integer.parseInt(tks[0]);

			String id = tks[1].split("\\.")[0];

			Common.outputLines(
					new ArrayList<String>(allLines.subList(currentLineNo,
							currentLineNo + size)), base + id + ".srl");
			currentLineNo += size;
		}
	}

}
