package ims.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Util {

	public static BufferedReader getReader(File file) throws IOException{
		return getReader(file,file.toString().endsWith(".gz"));
	}
	
	public static BufferedReader getReader(File file,boolean gzipped) throws IOException{
		return getReader(file,gzipped,Charset.forName("UTF-8"));
	}

	public static BufferedReader getReader(File file,boolean gzipped,Charset charset) throws IOException{
		InputStream is=new FileInputStream(file);
		if(gzipped){
			is=new GZIPInputStream(is);
		}
		BufferedReader br=new BufferedReader(new InputStreamReader(is,charset));
		return  br;
	}

	public static BufferedWriter getWriter(File file) throws IOException{
		return getWriter(file,file.toString().endsWith(".gz"));
	}
	
	public static BufferedWriter getWriter(File file,boolean gzipped) throws IOException{
		return getWriter(file,gzipped,Charset.forName("UTF-8"));
	}

	public static BufferedWriter getWriter(File file,boolean gzipped,Charset charset) throws IOException{
		OutputStream os=new FileOutputStream(file);
		if(gzipped){
			os=new GZIPOutputStream(os);
		}
		BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(os,charset));
		return writer;
	}
	
	public static String insertCommas(long l){
		StringBuilder ret=new StringBuilder(Long.toString(l));
		ret.reverse();
		for(int i=3;i<ret.length();i+=4){
			if(i+1<=ret.length())
				ret.insert(i,",");
		}
		return ret.reverse().toString();
	}
	
	public static boolean sameTwice(String[] n){
		Set<String> seen=new HashSet<String>();
		for(String a:n){
			if(seen.contains(a))
				return true;
			seen.add(a);
		}
		return false;
	}
	
	public static <T> void incrementMapValue(Map<T,MutableInt> tm, T key){
		MutableInt mi=tm.get(key);
		if(mi==null){
			tm.put(key,new MutableInt(1));
		} else {
			mi.increment();
		}
	}

}
