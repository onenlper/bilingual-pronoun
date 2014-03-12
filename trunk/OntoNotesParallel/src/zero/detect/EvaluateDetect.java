package zero.detect;

import java.util.ArrayList;

import model.EntityMention;
import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.OntoCorefXMLReader;
import util.Common;
import util.Util;

public class EvaluateDetect {

	static boolean singleF = false;
	static double gold = 0;
	static double sys = 0;
	static double hit = 0;

	public static void main(String args[]) {
		int start = 0;
		if (args.length == 1) {
			singleF = true;
			start = Integer.parseInt(args[0]);
		}

		for (int i = start; i < 5; i++) {
			ArrayList<String> parafiles = Common.getLines("parallelMap.test."
					+ i);
			for (String file : parafiles) {
				String tokens[] = file.split("#");
				String docName = tokens[0].trim();
				CoNLLDocument chiDoc = new CoNLLDocument(Util.getFullPath(
						docName, "chi", true));
				OntoCorefXMLReader.addGoldZeroPronouns(chiDoc, false);
				
				if (chiDoc.noZeroAnnotation) {
					continue;
				}
				
				for (CoNLLPart part : chiDoc.getParts()) {

//					ArrayList<EntityMention> goldZerosArr = ZeroUtil
//							.getGoldZeros(part.getChains());
					ArrayList<EntityMention> goldZerosArr = ZeroUtil.getAnaphorZeros(part.getChains());
					gold += goldZerosArr.size();

					ArrayList<EntityMention> sysZerosArr = ZeroUtil
							.loadClassifiedMention(part, Integer.toString(i));
					sys += sysZerosArr.size();

					for (EntityMention gold : goldZerosArr) {
						for (EntityMention sys : sysZerosArr) {
							if (gold.start == sys.start) {
								hit++;
								break;
							}
						}
					}
				}
			}
			if (singleF) {
				break;
			}
		}
		Common.printRPF(gold, sys, hit);
	}
}
