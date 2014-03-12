package invest;

import java.util.ArrayList;

import util.Common;
import util.Util;

public class GenerateAllMap {

	public static void main(String args[]) {
		String lang = args[0];
		ArrayList<String> lines = null;
		if(lang.equalsIgnoreCase("chi")) {
			lines = Common.getLines("chinese_list_all");
		} else {
			lines = Common.getLines("english_list_all");
		}
		ArrayList<String> output = new ArrayList<String>();
		for(String line : lines) {
			String id = Util.getID(line);
			output.add(id + " # " + id);
		}
		Common.outputLines(output, "allMTMap." + lang);
	}
}
