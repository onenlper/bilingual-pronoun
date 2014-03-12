package setting;

import java.util.ArrayList;

import util.Common;

public class CreateExpPartition {

	public static void main(String args[]) {
		
		ArrayList<String> paraMap = Common.getLines("parallelMap");
		ArrayList<String> alignSens = new ArrayList<String>();
		
		ArrayList<ArrayList<String>> folders = new ArrayList<ArrayList<String>>();
		for(int i = 0;i<5;i++) {
			ArrayList<String> folder = new ArrayList<String>();
			folders.add(folder);
		}
		
		for (int i = 0;i<paraMap.size();i++) {
			// skip mz folder
			String parallel = paraMap.get(i);
			if(parallel.trim().startsWith("mz/")) {
				continue;
			}
			int id = i%5;
			folders.get(id).add(parallel);
		}
		
		for(int i =0;i<5;i++) {
			ArrayList<String> trainSet = new ArrayList<String>();
			ArrayList<String> testSet = new ArrayList<String>();
			testSet.addAll(folders.get(i));
			for(int j=0;j<5;j++) {
				if(j!=i) {
					trainSet.addAll(folders.get(j));
				}
			}
			Common.outputLines(trainSet, "parallelMap.train." + i);
			Common.outputLines(testSet, "parallelMap.test." + i);
		}
		
	}
	
}
