package ims.coref;

import java.io.File;
import java.util.ArrayList;

import util.Common;

public class Check {

	public static void main(String args[]) {
		String base = "/users/yzcchen/chen3/ijcnlp2013/ilp/";
		File folder = new File(base + "/chi4/");
		int all = 0;
		int dif = 0;
		for(File f : folder.listFiles()) {
			if(!f.getName().endsWith(".ilp")) {
				continue;
			}
			all++;
			ArrayList<String> lines1 = Common.getLines(f.getAbsolutePath());
			ArrayList<String> lines2 = Common.getLines(base+"/chi22/"+f.getName());
			boolean diff = false;
			if(lines1.size()==lines2.size()) {
				for(int i=0;i<lines1.size();i++) {
					String l1 = lines1.get(i);
					String l2 = lines2.get(i);
					if(!l1.equals(l2)) {
						diff = true;
						break;
					}
				}
			} else {
				diff = true;
			}
			if(diff) {
				dif++;
				System.out.println(f.getAbsolutePath());
			}
		}
		System.out.println(all + "/" + dif);
	}
}
