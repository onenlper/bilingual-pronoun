package ims.coref.features;

import java.util.ArrayList;

public class Atom {
	public String orig;
	public ArrayList<String> patterns;
	public Atom(String orig) {
		this.orig = orig;
		this.patterns = new ArrayList<String>();
		this.patterns.add(orig);
	}
	public void addPattern(String pattern) {
		this.patterns.add(pattern);
	}
}
