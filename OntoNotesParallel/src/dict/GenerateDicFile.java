package dict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;


public class GenerateDicFile {
	// = "/users/yzcchen/chen3/ijcnlp2013/wordAlign/tokenBase/"
	static String wordAlignBase;
	
	public static void main(String args[]) {
		if(args.length!=1) {
			System.out.println("java ~ wordAlignBase");
			System.exit(1);
		}
		wordAlignBase = args[0] + File.separator;
		new GenerateDicFile();
	}
	
	Hashtable<String, Integer> sourceVoc = new Hashtable<String, Integer>();
	Hashtable<String, Integer> targetVoc = new Hashtable<String, Integer>();
	
	public GenerateDicFile() {
		readInStemDic();
		readVocs();
		getPair();
	}
	
	private void readInStemDic() {
		readOneStemDic("adj_stems.txt");
		readOneStemDic("noun_stems.txt");
		readOneStemDic("verb_stems.txt");
		System.out.println("stem dic size: " + stemDic.size());
	}

	Hashtable<String, String> stemDic = new Hashtable<String, String>();
	private void readOneStemDic(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			while((line=br.readLine())!=null) {
				String tokens[] = line.split("\\s+");
				for(int i=0;i<tokens.length-1;i++) {
					stemDic.put(tokens[i], tokens[tokens.length-1]);	
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getPair() {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("ecdict.utf8.txt"));
			String line;
			while((line=br.readLine())!=null) {
				String tokens[] = line.split("\\<\\>");
				if(tokens.length!=2) {
					continue;
				}
				String en = tokens[0].trim();
				String ch = tokens[1].trim();
				if(this.stemDic.get(en)!=null) {
					en = this.stemDic.get(en);
				}
				if(sourceVoc.get(en)!=null && targetVoc.get(ch)!=null) {
//					System.out.println(en + " # " + ch);
					entries.add(new Entry(targetVoc.get(ch),sourceVoc.get(en)));
				}
			}
			br.close();
			System.out.println(entries.size());
			Collections.sort(entries);
			FileWriter dicFw = new FileWriter(wordAlignBase + "dic");
			for(Entry entry:entries) {
				dicFw.write(entry.F + " " + entry.E + "\n");
			}
			dicFw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readVocs() {
		sourceVoc = readVoc(wordAlignBase + "engDocs.vcb");
		targetVoc = readVoc(wordAlignBase + "chiDocs.vcb");
	}
	
	private static class Entry implements Comparable{
		int F;
		int E;
		
		public Entry(int F, int E) {
			this.F = F;
			this.E = E;
		}

		@Override
		public int compareTo(Object arg0) {
			// TODO Auto-generated method stub
			if(this.F<((Entry)arg0).F) {
				return -1;
			} else if(this.F==((Entry)arg0).F) {
				return 0;
			} else {
				return 1;
			} 
		}
		
	}
	
	private Hashtable<String,Integer> readVoc(String file) {
		Hashtable<String,Integer> voc=null;
		try {
			voc = new Hashtable<String, Integer>();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null) {
				String tokens[] = line.split("\\s+");
				voc.put(tokens[1], Integer.valueOf(tokens[0]));
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return voc;
	}
}
