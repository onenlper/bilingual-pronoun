package align;

import java.util.ArrayList;

import model.CoNLL.CoNLLDocument;
import util.Common;
import util.Util;

public class HeuristcAlignOmitted {
	
	static String matchBase = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/align/";
	static String outputBase = "/users/yzcchen/chen3/ijcnlp2013/sentenceAlign/senAlignOut_modify/";

	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("parallelMap");

		for (int i = 0; i < lines.size(); i++) {
			String parallel = lines.get(i);
			System.out.println(parallel);
			String tokens[] = parallel.split("#");
			CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(
					tokens[0].trim(), "chi", true));

			CoNLLDocument engDoc = new CoNLLDocument(Util.getFullPath(
					tokens[1].trim(), "eng", true));
			ArrayList<String[]> aligns = new ArrayList<String[]>();
			ArrayList<String> matches = Common.getLines(matchBase + i
					+ ".match");

			System.out.println(chiDoc.getSentences().size() + " # "
					+ engDoc.getSentences().size() + " # " + i);

			for (int j = 0; j < matches.size(); j++) {
				String align[] = new String[2];
				String match = matches.get(j);
				String tks[] = match.split("<=>");

				String enSide = tks[0];
				String chSide = tks[1];

				if (enSide.trim().equals("omitted")) {
					align[0] = "";
				} else {
					StringBuilder sb = new StringBuilder();
					String enIDs[] = enSide.split(",");
					for (String enID : enIDs) {
						int sid = Integer.parseInt(enID.trim()) - 1;
						String text = engDoc.getSentence(sid).getText();
						sb.append(text.trim()).append(" ");
					}
					align[0] = sb.toString().trim();
				}

				if (chSide.trim().equals("omitted")) {
					align[1] = "";
				} else {
					StringBuilder sb = new StringBuilder();
					String chIDs[] = chSide.split(",");
					for (String chID : chIDs) {
						int sid = Integer.parseInt(chID.trim()) - 1;
						String text = chiDoc.getSentence(sid).getText();
						sb.append(text.trim()).append(" ");
					}
					align[1] = sb.toString().trim();
				}
				aligns.add(align);
			}
			// en - ch
			loop: for(int j=0;j<aligns.size();j++) {
				String[] align = aligns.get(j);
				if(align[0].isEmpty()) {
					// a
					if(j==0) {
						// next;
						aligns.get(j+1)[1] = align[1] + " " + aligns.get(j+1)[1];
						aligns.remove(j);
						j--;
					} else if(j==aligns.size()-1){
						aligns.get(j-1)[1] = aligns.get(j-1)[1] + " " + align[1];  
					} else {
						// previous ratio
						double pChi = getLength(align[1]) + getLength(aligns.get(j-1)[1]);
						double pEng = getLength(aligns.get(j-1)[0]);
						double pRatio = pChi/pEng;
						
						// next ratio
						int tmpJ = j;
						StringBuilder nextSb = new StringBuilder();
						double nChi = getLength(align[1]);
						nextSb.append(align[1]).append(" ");
						while(aligns.get(tmpJ)[0].isEmpty()) {
							tmpJ++;
							if(tmpJ==aligns.size()) {
								aligns.get(j-1)[1] = aligns.get(j-1)[1] + " " + nextSb.toString();
								continue loop;
							}
							nChi += getLength(aligns.get(tmpJ)[1]);
							nextSb.append(aligns.get(tmpJ)[1]).append(" ");
						}
						double nEng = getLength(aligns.get(tmpJ)[0]);
						double nRatio = nChi/nEng;
						
						boolean next = combineNext(pRatio, nRatio);
						if(next) {
							aligns.get(tmpJ)[1] = nextSb.toString().trim();
							for(int m=j;m<tmpJ;m++) {
								aligns.remove(j);
							}
						} else {
							aligns.get(j-1)[1] = aligns.get(j-1)[1] + " " + align[1];
							aligns.remove(j);
							j--;
						}
					}
				} else if(align[1].isEmpty()) {
					if(j==0) {
						aligns.get(j+1)[0] = align[0] + " " + aligns.get(j+1)[0]; 
						aligns.remove(j);
						j--;
					} else if(j==aligns.size()-1){
						aligns.get(j-1)[0] = aligns.get(j-1)[0] + " " + align[0];
					} else {
						// previous ratio
						double pChi = getLength(aligns.get(j-1)[1]);
						double pEng = getLength(align[0]) + getLength(aligns.get(j-1)[0]);
						double pRatio = pChi/pEng;
						
						// next ratio
						int tmpJ = j;
						StringBuilder nextSb = new StringBuilder();
						double nEng = getLength(align[0]);
						nextSb.append(align[0]).append(" ");
						while(aligns.get(tmpJ)[1].isEmpty()) {
							tmpJ++;
							if(tmpJ==aligns.size()) {
								aligns.get(j-1)[0] = aligns.get(j-1)[0] + " " + nextSb.toString();
								continue loop;
							}
							nEng += getLength(aligns.get(tmpJ)[0]);
							nextSb.append(aligns.get(tmpJ)[0]).append(" ");
						}
						double nChi = getLength(aligns.get(tmpJ)[1]);
						double nRatio = nChi/nEng;
						
						boolean next = combineNext(pRatio, nRatio);
						if(next) {
							aligns.get(tmpJ)[0] = nextSb.toString().trim();
							for(int m=j;m<tmpJ;m++) {
								aligns.remove(j);
							}
						} else {
							aligns.get(j-1)[0] = aligns.get(j-1)[0] + " " + align[0];
							aligns.remove(j);
							j--;
						}
					}
				}
			}
			ArrayList<String> output = new ArrayList<String>();
			for(String align[] : aligns) {
				if(!align[0].isEmpty() && !align[1].isEmpty()) {
					output.add(align[0]);
					output.add(align[1]);
					output.add("==============================");
				}
			}
			Common.outputLines(output, outputBase + i + ".align");
		}
	}
	
	private static int getLength(String str) {
		return str.split("\\s+").length;
	}
	private static double ratio = 0.8605035204961684;
	private static boolean combineNext(double pRatio, double nRatio) {
		if(Math.abs(pRatio-ratio) > Math.abs(nRatio-ratio)) {
			return true;
		} else {
			return false;
		}
	}
}
