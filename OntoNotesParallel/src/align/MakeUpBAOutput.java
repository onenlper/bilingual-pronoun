package align;

import java.io.File;
import java.util.ArrayList;

import util.Common;

public class MakeUpBAOutput {

	public static void makeUp(String folder) {

		ArrayList<String> baLines = Common.getLines(folder
				+ "/ba/training.alignsoft");
		ArrayList<String> bachiDocs = Common
				.getLines(folder + "/ba/training.f");
		ArrayList<String> chiDocs = Common.getLines(folder + "/docs/docs.f");
		int e = 0;
		System.out.println(baLines.size());
		for (int i = 0; i < baLines.size(); i++) {
			String bachi = bachiDocs.get(i);
			String chi = chiDocs.get(i);

			if (!bachi.trim().equalsIgnoreCase(chi.trim())) {
				System.out.println(bachi + "\n" + chi);
				bachiDocs.add(i, chi);
				baLines.add(i, "");
				e++;
				System.exit(1);
			}
		}
		Common.outputLines(baLines, folder + "/align/training.alignsoft");
		System.err.println("Error1:" + e);

		Common.outputLines(Common.getLines(folder + "/ba/training.align"),
				folder + "/align/training.align");
	}

	public static void main(String args[]) {
		// if (args.length != 2) {
		// System.out.println("java ~ head|token gold|sys");
		// System.exit(1);
		// }
		// String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlign/" + args[0]
		// + "Base_BA_" + args[1] + File.separator;

		String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/"
				+ File.separator;

		makeUp(base);
	}

}
