package ims.util;

public class IntPair {

	public static IntPairPool intPairPool=new IntPairPool();
	
	public final int i1;
	public final int i2;
	
	public IntPair(int i,int j){
		this.i1=i;
		this.i2=j;
	}
	
	public boolean equals(Object other){
		if(other instanceof IntPair)
			return equals((IntPair) other);
		else
			return false;
	}
	public boolean equals(IntPair other){
		return other.i1==i1 && other.i2==i2;
	}
	
	public int hashCode(){
		return 31*i1+2*i2;
	}
	
	public static class IntPairPool {
		private static final int INIT_SIZE=100;
		private IntPair[][] ips;
		private IntPairPool(){
			ips=new IntPair[INIT_SIZE][INIT_SIZE];
		}

		public final synchronized IntPair get(int i,int j){
			if(i>=ips.length)
				resizeI(i);
			if(j>=ips[i].length)
				resizeJ(i,j);
			if(ips[i][j]==null)
				ips[i][j]=new IntPair(i,j);
			return ips[i][j];
		}
		private final void resizeI(int i) {
			IntPair[][] q=new IntPair[i+1][];
			System.arraycopy(ips, 0, q, 0, ips.length);
			for(int k=ips.length;k<q.length;++k)
				q[k]=new IntPair[INIT_SIZE];
			ips=q;
		}
		private final void resizeJ(int i,int j) {
			IntPair[] q=new IntPair[j+1];
			System.arraycopy(ips[i], 0, q, 0, ips[i].length);
			ips[i]=q;
		}
		
	}
}
