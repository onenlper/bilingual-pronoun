package createConfuseParallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import model.CoNLL.CoNLLDocument;
import util.Common;

public class CreateParallelMap {

	public static void main(String args[]) {
		

		HashSet<String> goldEngParallel = new HashSet<String>();
		//TODO
		ArrayList<String> parallelTrain = Common.getLines("engCoNLL.train.1");
		for(String line : parallelTrain) {
			if (line.startsWith("#begin")) {
				int a = line.indexOf("(");
				int b = line.indexOf(")");
				String eng = line.substring(a + 1, b);
				goldEngParallel.add(eng);
			}
		}
		
		String base = "/users/yzcchen/chen3/ijcnlp2013/parallelMTMix/chi_MT/align/";

		ArrayList<String> MTLines = Common
				.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/parallelMap");
		ArrayList<String> ParallelLines = Common
				.getLines("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/parallelMap");

		ArrayList<String> newLines = new ArrayList<String>();
		HashMap<String, String> goldParallelMap = new HashMap<String, String>();
		for (String line : ParallelLines) {
			String tk[] = line.split("#");
			String chi = tk[0].trim();
			String eng = tk[1].trim();
			if(goldEngParallel.contains(chi)) {
				goldParallelMap.put(chi, eng);
			}
		}


		for (int i = 0; i < MTLines.size(); i++) {
			String line = MTLines.get(i);
			String tk[] = line.split("#");
			String chi = tk[0].trim();
			String eng = tk[1].trim();

			ArrayList<String> alignF = new ArrayList<String>();
			ArrayList<String> alignSoftF = new ArrayList<String>();
			ArrayList<String> chiF = new ArrayList<String>();
			ArrayList<String> engF = new ArrayList<String>();
			ArrayList<String> idF = new ArrayList<String>();

			if (goldParallelMap.containsKey(chi)) {
				newLines.add(chi + " # _" + goldParallelMap.get(chi));
				int j = ParallelLines.indexOf(chi + " # "
						+ goldParallelMap.get(chi));

				alignF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/"
								+ j + ".align");
				alignSoftF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/"
								+ j + ".alignsoft");
				chiF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/"
								+ j + ".chi");
				engF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/"
								+ j + ".eng");
				idF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/wordAlignCor/align/"
								+ j + ".id");
//				skippedEng.add(eng);
			} else {
				alignF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/"
								+ i + ".align");
				alignSoftF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/"
								+ i + ".alignsoft");
				chiF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/"
								+ i + ".chi");
				engF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/"
								+ i + ".eng");
				idF = Common
						.getLines("/users/yzcchen/chen3/ijcnlp2013/googleMTALL/chi_MT/align/"
								+ i + ".id");
				newLines.add(line);
			}

			Common.outputLines(alignF, base + i + ".align");
			Common.outputLines(alignSoftF, base + i + ".alignsoft");
			Common.outputLines(chiF, base + i + ".chi");
			Common.outputLines(engF, base + i + ".eng");
			Common.outputLines(idF, base + i + ".id");
		}

		Common.outputLines(newLines, base + "parallelMap");

		// build mix eng
		ArrayList<String> mixEng = new ArrayList<String>();
		ArrayList<String> origEng = Common.getLines("engCoNLL.train.1");
		for(String line : origEng) {
			if (line.startsWith("#begin")) {
				int a = line.indexOf("(");
				line = line.substring(0, a+1) + "_" + line.substring(a+1);
			} else if(line.startsWith("#end")) {
				
			} else if(line.isEmpty()) {
				
			} else {
				line = "_" + line;
			}
			mixEng.add(line);
		}
				
				
		boolean skip = false;
		int i = 0;
		for (String line : Common.getLines("MT.chiCoNLL.train.0")) {
			if (line.startsWith("#begin")) {
				int a = line.indexOf("(");
				int b = line.indexOf(")");
				String eng = line.substring(a + 1, b);
				if(goldEngParallel.contains(eng)) {
					skip = true;
//					System.out.println("Skip: " + (i++));
				} else {
					skip = false;
				}
			}
			if(!skip) {
				mixEng.add(line);
			}
		}
		
		Common.outputLines(mixEng, "MixMT.chiCoNLL.train.0");
		
		CoNLLDocument doc = new CoNLLDocument("MT.chiCoNLL.train.0");
		System.out.println(doc.getParts().size() + "#");
		
		CoNLLDocument doc2 = new CoNLLDocument("MixMT.chiCoNLL.train.0");
		System.out.println(doc2.getParts().size() + "#");
		
//		System.out.println(goldEngParallel.size() + "##");
//		System.out.println(goldEngParallel);
	}
}
