package ims.coref;

import java.util.HashMap;

public class Test {
	
	public static void main(String args[]) {
		HashMap<String, Val> map = new HashMap<String, Val>();
		Val val = new Val();
		val.k = 100;
		map.put("1", val);
		
		Test t = new Test();
		t.v = map.get("1");
		
		map.clear();
		
		System.out.println(t.v.k);
	}
	
	Val v;
	
	public static class Val {
		int k = 100;
	}
}
