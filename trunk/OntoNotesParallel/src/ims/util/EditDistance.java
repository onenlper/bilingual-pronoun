package ims.util;

public class EditDistance {

	//this can be done more efficiently, but I'm lazy (see eg wikipedia)
	
	public static int[][] levenshteinDistanceTable(String s1,String s2){
		s1=s1.toLowerCase();
		s2=s2.toLowerCase();
		int[][] d=new int[s1.length()+1][s2.length()+1];
		for(int i=1;i<d.length;++i)
			d[i][0]=i;
		for(int j=1;j<d[0].length;++j)
			d[0][j]=j;
		for(int j=0;j<s2.length();++j){
			for(int i=0;i<s1.length();++i){
				if(s1.charAt(i)==s2.charAt(j))
					d[i+1][j+1]=d[i][j];
				else
					d[i+1][j+1]=min(d[i][j+1],d[i+1][j],d[i][j])+1;
			}
		}
		return d;		
	}
	
	public static int levenshteinDistance(String s1,String s2){
		int d[][]=levenshteinDistanceTable(s1,s2);
		return d[s1.length()][s2.length()];
	}
	
	public static String editScript(String s1,String s2){
		StringBuilder sb=new StringBuilder();
		int[][] d=levenshteinDistanceTable(s1,s2);
		int n=d.length;
		int m=d[0].length;
		
		int x=n-1;
		int y=m-1;
		while(true){
			if(d[x][y]==0)
				break;
			if(y>0 && x>0 && d[x-1][y-1]<d[x][y]){
				sb.append('R').append(Integer.toString((x-1))).append(s1.charAt(x-1)).append(s2.charAt(y-1));
//				sb.append('R').append((char)(x-1)).append(s1.charAt(x-1)).append(s2.charAt(y-1));
				--x;
				--y;
				continue;
			}
			if(y>0 && d[x][y-1]<d[x][y]){
				sb.append('I').append(Integer.toString(x)).append(s2.charAt(y-1));
//				sb.append('I').append((char)x).append(s2.charAt(y-1));
				--y;
				continue;
			}
			if(x>0 && d[x-1][y]<d[x][y]){
				sb.append('D').append(Integer.toString((x-1))).append(s1.charAt(x-1));
//				sb.append('D').append((char)(x-1)).append(s1.charAt(x-1));
				--x;
			}
			if (x>0&& y>0 && d[x-1][y-1]==d[x][y]) {
				x--; y--;
				continue ;
			}
			if (x>0&&  d[x-1][y]==d[x][y]) {
				x--; 
				continue;
			}
			if (y>0 && d[x][y-1]==d[x][y]) {
				y--;
				continue;
			}
		}
		if(sb.length()==0)
			return "0";
		else
			return sb.toString();
	}
	
	private static int min(int a, int b, int c){
		return Math.min(a, Math.min(b, c));
	}
	
	public static void main(String[] args){
		String[][] test={{"sitting","kitten"}, //should be 3
						{"fluff","floof"},     //should be 2
						{"eat","eats"},        //should be 1
						{"sunday","saturday"}, //should be 3
						{"Monday","TuesDay"},
						{"monday","sunday"},
						{"Monday","Sunday"}};
		for(String[] pair:test){
			int d=levenshteinDistance(pair[0],pair[1]);
			System.out.println(pair[0]+"\t"+pair[1]+"\t"+d);
		}
		System.out.println();
		String[][] esTest={{"Mondo","Monda"},
						   {"Mondo","Nondo"},
						   {"Fluff","Floef"},
						   {"Fluff","Floaaaef"},
						   {"AABB","BBBB"},
						   {"BBAA","BBBB"},
						   {"AAA","A  AAA"},
						   {"AAAAAA","A"},
						   {"SAME","SAME"}};
		for(String[][] q:new String[][][]{esTest,test}){
			for(String[] e:q){
				String s=editScript(e[0],e[1]);
				System.out.println(e[0]+'\t'+e[1]+'\t'+s);
			}
			System.out.println();
		}
	}
}
