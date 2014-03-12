package align;

import java.io.File;
import java.util.ArrayList;

import util.Common;

public class ConvertBA2Giza {

	public static void main(String args[]) {
//		if (args.length != 2) {
//			System.out.println("java ~ head|token gold|sys");
//			System.exit(1);
//		}
//		String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlign/" + args[0]
//				+ "Base_BA_" + args[1] + File.separator;
		
		base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/" + File.separator;
		
		System.out.println(base);
		convert(base);
	}
	
	static String base = "";

	public static void convert(String folder) {
		folder += File.separator;
		ArrayList<String> chiDocs = Common.getLines(folder + "/docs/docs.f");
		ArrayList<String> engDocs = Common.getLines(folder + "/docs/docs.e");
		ArrayList<String> lineNos = Common
				.getLines(base + "/lineNos");

		ArrayList<String> baResult = Common.getLines(folder
				+ "/align/training.align");

		int m = baResult.size();
		ArrayList<String> allGiza = new ArrayList<String>();

		if (m != chiDocs.size() || m != engDocs.size()) {
			Common.bangErrorPOS("line number not equal");
		}

		for (int i = 0; i < m; i++) {
			String chiDoc = chiDocs.get(i);
			String engDoc = engDocs.get(i);
			String ba = baResult.get(i);

			String engTks[] = engDoc.split("\\s+");
			String tokens[] = ba.split("\\s+");

			allGiza.add("# Sentence pair (" + (i + 1) + ")");
			allGiza.add(chiDoc);

			ArrayList<String[]> engPair = new ArrayList<String[]>();
			String[] empty = new String[2];
			empty[0] = "NULL";
			empty[1] = "";
			engPair.add(empty);
			for (String engTk : engTks) {
				String pair[] = new String[2];
				pair[0] = engTk;
				pair[1] = "";
				engPair.add(pair);
			}

			if (!ba.trim().isEmpty()) {
				for (String token : tokens) {
					int k = token.indexOf("-");
					int chID = Integer.parseInt(token.substring(0, k));
					int enID = Integer.parseInt(token.substring(k + 1));
					
//					System.out.println(i + "# " + ba);
					
					engPair.get(enID + 1)[1] += Integer.toString(chID + 1)
							+ " ";
				}
			}
			StringBuilder engSB = new StringBuilder();
			for (String[] pair : engPair) {
				engSB.append(pair[0]).append(" ({ ").append(pair[1])
						.append("}) ");
			}
			allGiza.add(engSB.toString().trim());
		}

		int k = 0;
		for (int i = 0; i < lineNos.size(); i++) {
			String numberStr = lineNos.get(i).trim();
			int number = Integer.parseInt(numberStr.substring(numberStr
					.lastIndexOf(' ') + 1));
			k += number;
			ArrayList<String> content = new ArrayList<String>(allGiza.subList(
					0, number * 3));
			for (int j = 0; j < number * 3; j++) {
				allGiza.remove(0);
			}
			Common.outputLines(content, folder + "/align/" + File.separator + i
					+ ".align");
		}
		System.out.println(allGiza.size());
		System.out.println(k);
	}
}
