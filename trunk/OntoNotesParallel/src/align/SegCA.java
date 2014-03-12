package align;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import util.Common;

public class SegCA {

	public static void main(String args[]) throws Exception{
		String folder = "/users/yzcchen/tool/stanford-chinese-segmenter-2012-01-08/orig";
		File fold = new File(folder);
		for(File f : fold.listFiles()) {
			if(f.getAbsolutePath().endsWith(".txt")) {
				ArrayList<String> lines = Common.getLines(f.getAbsolutePath());
				for(String line : lines) {
					System.out.println(line);
				}
				
				InputStream is = new FileInputStream(f.getAbsolutePath());
				InputStreamReader reader = new InputStreamReader(is, "GB18030");
//				String file = reader.r
			}
		}
	}
}
