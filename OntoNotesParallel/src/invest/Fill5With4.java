package invest;

import java.io.File;
import java.util.ArrayList;

import util.Common;

public class Fill5With4 {

	static String v4Base = "/users/yzcchen/chen2/LDC2011T03/ontonotes-release-4.0/data/files/data/";

	static String v5Base = "/users/yzcchen/CoNLL-2012/conll-2012-test-v0/data/files/data/";

	static String prefix = ".parallel";

	public static void main(String args[]) {

		ArrayList<String> files = Common.getLines("v5TestData");

		for (String file : files) {
			int k = file.lastIndexOf(".");
			String ID = file.substring(v5Base.length(), k);

			System.out.println(ID);

			String v4FileName = v4Base + ID + prefix;
			if ((new File(v4FileName)).exists()) {
				System.out.println(v4FileName);
				ArrayList<String> content = Common.getLines(v4FileName);
				String v5FileName = v5Base + ID + prefix;
				if (!(new File(v5FileName)).exists()) {
					Common.outputLines(content, v5FileName);
				} else {
					System.out.println("@@@@");
				}
			}
		}
	}
}
