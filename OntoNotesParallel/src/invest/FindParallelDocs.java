package invest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import util.Common;

public class FindParallelDocs {

	static String baseChi = "/users/yzcchen/CoNLL-2012/conll-2012-train-v0/data/files/data/chinese/annotations/";

	static String baseEng = "/users/yzcchen/CoNLL-2012/conll-2012-train-v0/data/files/data/english/annotations/";

	static String annotationsStr = "annotations/";

	public static void main(String args[]) {
		ArrayList<String> allChi = Common.getLines("chinese_list_all");
		ArrayList<String> allEng = Common.getLines("english_list_all");
		
		HashSet<String> chiIDs = new HashSet<String>();
		for(String chi : allChi) {
			int start = chi.indexOf("annotations/") + annotationsStr.length();
			int dot = chi.lastIndexOf(".");
			String id = chi.substring(start, dot);
			chiIDs.add(id);
		}
		
		HashSet<String> engIDs = new HashSet<String>();
		for(String eng : allEng) {
			int start = eng.indexOf("annotations/") + annotationsStr.length();
			int dot = eng.lastIndexOf(".");
			String id = eng.substring(start, dot);
			if(id.equals("nw/xinhua/00/chtb_0001"))
				System.out.println(id);
			engIDs.add(id);
		}
		
		
		ArrayList<String> chiParas = Common.getLines("chi.para");
		ArrayList<String> engParas = Common.getLines("eng.para");
		
		HashMap<String, String> chi_engMap = new HashMap<String, String>();
		HashMap<String, String> eng_chiMap = new HashMap<String, String>();
		int engSize = 0;
		ArrayList<String> maps = new ArrayList<String>();
		loop: for(String para : chiParas) {
			String eng = "";
			ArrayList<String> contents = Common.getLines(para);
			for(int i=1;i<contents.size();i++) {
				String content = contents.get(i);
				if(content.startsWith("original") || content.startsWith("translation")) {
					engSize ++;
					int k = content.lastIndexOf(" ");
					eng += " " + content.substring(k+1);
					
					if(i!=1) {
						continue loop;
					}
					
				}
			}
			int start = para.indexOf("annotations/") + annotationsStr.length();
			int dot = para.lastIndexOf(".");
			String chi = para.substring(start, dot);
			eng = eng.trim();
			chi = chi.trim();
			
			if(chiIDs.contains(chi) && engIDs.contains(eng)) {
				chi_engMap.put(chi, eng);
				System.out.println(chi + " # " + eng);
				System.out.println(chiIDs.contains(chi) + " # " + engIDs.contains(eng));
				maps.add(chi + " # " + eng);
			}
		}
		System.out.println(chi_engMap.size());
		System.out.println(engSize);
		Common.outputLines(maps, "parallelMap");
//		int gg = 0;
//		for(String para : engParas) {
//			String file = Common.getLines(para).get(1);
//			int k = file.lastIndexOf(" ");
//			String chi = file.substring(k+1);
//			int start = para.indexOf("annotations/") + annotationsStr.length();
//			int dot = para.lastIndexOf(".");
//			String eng =  para.substring(start, dot);
//			if(eng_chiMap.containsValue(chi)) {
//				System.out.println(para);
//				System.out.println(chi);
//				System.out.println("G");
//				System.exit(1);
//			}
//			eng_chiMap.put(eng, chi);
//		}
//		
//		for(String eng : eng_chiMap.keySet()) {
//			String chi = eng_chiMap.get(eng);
//			if(!chi_engMap.containsKey(chi)) {
//				System.out.println("en " + eng + " # chi " + chi);
//				gg++;
//			} else {
//				chi_engMap.remove(chi);
//			}
//		}
//		System.out.println(chi_engMap.size());
//		System.out.println(eng_chiMap.size());
//		System.out.println(gg);
		
	}
}
