package align;

import java.util.ArrayList;

import util.Common;

public class SplitBAOutput {

	public static void main(String args[]) {
//		if (args.length != 2) {
//			System.out.println("java ~ head|token gold|sys");
//			System.exit(1);
//		}
		String base = "/users/yzcchen/chen3/ijcnlp2013/wordAlignXXX/";
		System.out.println(base);
		
		ArrayList<String> lineNos = Common
				.getLines(base + "/lineNos");
		ArrayList<String> allAlign = Common.getLines(base + "/align/training.align");
		ArrayList<String> allSoftAlign = Common.getLines(base + "/align/training.alignsoft");
		
		ArrayList<String> allChi = Common.getLines(base + "/docs/docs.f");
		ArrayList<String> allEng = Common.getLines(base + "/docs/docs.e");
		
		int current = 0;
		for (int i = 0; i < lineNos.size(); i++) {
			int size = Integer.parseInt(lineNos.get(i).substring(lineNos.get(i).lastIndexOf(' ') + 1));
			ArrayList<String> subAlign = new ArrayList<String>(allAlign.subList(current, current
					+ size));
			ArrayList<String> subAlignSoft = new ArrayList<String>(allSoftAlign.subList(current, current+size));

			ArrayList<String> subChi = new ArrayList<String>(allChi.subList(current, current+size));
			ArrayList<String> subEng = new ArrayList<String>(allEng.subList(current, current+size));
			
			Common.outputLines(subAlign, base + "/align/" + i + ".align");
			Common.outputLines(subAlignSoft, base + "/align/" + i + ".alignsoft");
			
			Common.outputLines(subChi, base + "/align/" + i + ".chi");
			Common.outputLines(subEng, base + "/align/" + i + ".eng");
			
			
			ArrayList<String> ids = new ArrayList<String>();
			
			int chiID = 0;
			int engID = 0;
			
			
			for(int j=0;j<subChi.size();j++) {
				ids.add("===========================");
				String chi = subChi.get(j).trim();
				StringBuilder sb = new StringBuilder();
				for(String tk : chi.split("\\s+")) {
					sb.append(chiID++).append(" ");
				}
				ids.add(sb.toString().trim());
				
				String eng = subEng.get(j).trim();
				sb = new StringBuilder();
				for(String tk : eng.split("\\s+")) {
					sb.append(engID++).append(" ");
				}
				ids.add(sb.toString().trim());
				
			}
			
			
			Common.outputLines(ids, base + "/align/" + i + ".id");
			
			current += size;
		}
	}
}
