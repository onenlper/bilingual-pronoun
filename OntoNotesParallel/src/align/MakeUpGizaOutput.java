package align;

import java.io.File;
import java.util.ArrayList;

import util.Common;

public class MakeUpGizaOutput {

	public static void makeUp(String gizaOutput, String chiDocsFn,
			String engDocsFn) {

		ArrayList<String> gizaLines = Common.getLines(gizaOutput);
		ArrayList<String> chiDocs = Common.getLines(chiDocsFn);
		ArrayList<String> engDocs = Common.getLines(engDocsFn);
		int e1 = 0;
		int e2 = 0;
		int e3 = 0;
		for (int i = 0; i < gizaLines.size() / 3; i++) {
			String head = gizaLines.get(i * 3).trim();
			String chinese = gizaLines.get(i * 3 + 1).trim();
			String english = getEngSource(gizaLines.get(i * 3 + 2));
			int a = head.indexOf("(");
			int b = head.indexOf(")");
			int num = Integer.parseInt(head.substring(a + 1, b));
			if (num != (i + 1)) {
				// missing line
				String correctEng = getCorrectEnglish(engDocs, i);
//				System.out.println(correctEng);
				ArrayList<String> insert = new ArrayList<String>();
				insert.add("# Sentence pair (" + (i+1) + ") ");
				insert.add(chiDocs.get(i));
				insert.add(correctEng);
				gizaLines.addAll(i * 3, insert);
				e1++;
				continue;
			}

			// if chinese not match
			if (!chiDocs.get(i).trim().equals(chinese)) {
				// System.out.println("Correct chinese: ");
				// System.out.println(head);
				// System.out.println(chinese);
				// System.out.println(chiDocs.get(i));
				gizaLines.set(i * 3 + 1, chiDocs.get(i));
				e2++;
			}
			// if english not match
			if (!engDocs.get(i).trim().equals(english)) {

				// System.out.println("Correct english: ");
				// System.out.println(head);
//				if (!gizaLines.get(i * 3 + 2).trim().endsWith("})")) {
//
//				}
				// System.out.println(engDocs.get(i));
				String correctEng = gizaLines.get(i * 3 + 2).trim() + " " + fillEnglish(engDocs.get(i), english);
				gizaLines.set(i * 3 + 2, correctEng);
				e3++;
			}
		}
		Common.outputLines(gizaLines, gizaOutput + ".co");
		System.err.println("Error1:" + e1);
		System.err.println("Error2:" + e2);
		System.err.println("Error3:" + e3);
	}

	private static String fillEnglish(String engWhole, String engPart) {
		StringBuilder sb = new StringBuilder();
		String tokens[] = engWhole.trim().split("\\s+");
		String tokens2[] = engPart.trim().split("\\s+");

		for (int i = tokens2.length; i < tokens.length; i++) {
			String token = tokens[i];
			sb.append(token).append(" ({ }) ");
		}
		return sb.toString().trim();
	}

	private static String getEngSource(String str) {
		StringBuilder sb = new StringBuilder();
		int a = str.indexOf("})");
		int b = str.indexOf("({", a + 2);

		while (b != -1) {
			sb.append(str.substring(a + 2, b).trim()).append(" ");
			a = str.indexOf("})", b + 2);
			b = str.indexOf("({", a + 2);
		}
		return sb.toString().trim();
	}

	private static String getCorrectEnglish(ArrayList<String> engDocs, int i) {
		StringBuilder sb = new StringBuilder();
		sb.append("NULL ({ }) ");
		String tokens[] = engDocs.get(i).split("\\s+");
		for (String token : tokens) {
			sb.append(token).append(" ({ }) ");
		}
		return sb.toString().trim();
	}

	public static String wordAlignBase;
	
	public static void main(String args[]) {
		if(args.length!=1) {
			System.out.println("java ~ wordAlignBase");
			System.exit(1);
		}
		wordAlignBase = args[0];
		
		String gizaOutput = wordAlignBase + File.separator + "yzcchen.A3.final";
		String chiDocsFn = wordAlignBase + File.separator + "chiDocs";
		String engDocsFn = wordAlignBase + File.separator + "engDocs";
		makeUp(gizaOutput, chiDocsFn, engDocsFn);
	}

}
