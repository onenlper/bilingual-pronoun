package ims.coref.util;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.ZipFile;

import ims.coref.Options;
import ims.coref.features.FeatureSet;
import ims.coref.features.IFeature;
import ims.ml.liblinear.LibLinearModel;

public class ListFeatureSpace {

	public static final boolean WITH_WEIGHTS=true;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		Options options=new Options(args);
		ZipFile zf=new ZipFile(options.model);
		FeatureSet fs=(FeatureSet) ModelReaderWriter.loadObjectFromEntry(ModelReaderWriter.FS_ENTRY,zf);
		if(options.stacked){
			FeatureSet fs2=(FeatureSet) ModelReaderWriter.loadObjectFromEntry(ModelReaderWriter.STACKFS_ENTRY,zf);
			fs=FeatureSet.concat(fs, fs2);
		}
		if(WITH_WEIGHTS)
			listStringToIndexToWeight(fs,zf,options);
		else
			listStringToIndex(fs);
	}

	private static void listStringToIndexToWeight(FeatureSet fs,ZipFile zf, Options options) throws IOException, ClassNotFoundException{
		final String entry;
		if(options.stacked)
			entry=ModelReaderWriter.STACK_MODEL2_ENTRY;
		else
			entry=ModelReaderWriter.LL_MODEL_ENTRY;
		LibLinearModel llModel=(LibLinearModel) ModelReaderWriter.loadObjectFromEntry(entry,zf);
		double[] w=llModel.getModel().getFeatureWeights();
		int offset=0;
		for(IFeature f:fs.getFeatures()){
			Map<String,Integer> m=f.getMap();
			Map<Integer,String> m2=reverse(m);
			System.out.println("FEATURE: '"+f.getName()+"' -- offset: "+offset);
			String fn=String.format("%-55s", f.getName());
			for(Entry<Integer,String> e:m2.entrySet()){
				System.out.printf("%s %-8d %30s %.8f\n",fn,(offset+e.getKey()),e.getValue(),w[offset+e.getKey()-1]);
			}
			offset+=f.size();
		}
		
	}
	
	private static void listStringToIndex(FeatureSet fs) throws IOException,ClassNotFoundException {
		int offset=0;
		for(IFeature f:fs.getFeatures()){
			Map<String,Integer> m=f.getMap();
			Map<Integer,String> m2=reverse(m);
			System.out.println("FEATURE: '"+f.getName()+"' -- offset: "+offset);
			for(Entry<Integer,String> e:m2.entrySet())
				System.out.printf(" %-8d %s\n",(offset+e.getKey()),e.getValue());
			offset+=f.size();
		}
	}
	
	public static TreeMap<Integer,String> reverse(Map<String,Integer> m){
		TreeMap<Integer,String> tm=new TreeMap<Integer,String>();
		for(Entry<String,Integer> e:m.entrySet()){
			if(tm.containsKey(e.getValue())){
				System.err.println("DUPLICATE ENTRY");
				throw new Error("!");
			}
			tm.put(e.getValue(),e.getKey());
		}
		return tm;
	}
	
}
