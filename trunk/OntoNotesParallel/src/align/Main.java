package align;

import java.util.ArrayList;

import util.Common;

public class Main {
	
	public static void main(String args[]) {
		String file = "/users/yzcchen/test.data";
		ArrayList<String> lines = Common.getLines(file);
		
		ArrayList<String> output = new ArrayList<String>();
		
		for(String line : lines) {
			if(line.trim().isEmpty()) {
				output.add(line);
			} else {
				String tks[] = line.trim().split("\\s+");
				output.add(tks[0] + "\t" + tks[2]);
			}
		}
		Common.outputLines(output, file + "2");
	}
}
