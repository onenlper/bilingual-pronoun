package ims.ml.liblinear;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import de.bwaldvogel.liblinear.FeatureNode;

public class LibLinearOnDiskSink implements InstanceSink{

	private final BufferedWriter out;
	
	public LibLinearOnDiskSink(BufferedWriter out){
		this.out=out;
	}
	
	@Override
	public void sink(int label, List<FeatureNode> nodes) {
		try {
			StringBuilder sb=new StringBuilder(Integer.toString(label));
			int lastIndex=-1;
			for(FeatureNode fn:nodes){
				if(fn.index<=lastIndex)
					throw new RuntimeException("Indices not sorted!");
				sb.append(' ').append(fn.index).append(":").append(fn.value);
			}
			out.write(sb.toString());
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException("Failed while writing to file");
			
		}
	}

	@Override
	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to close writer");
		}
	}

	@Override
	public LibLinearModel train() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getAllInstance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPosInstance() {
		// TODO Auto-generated method stub
		return 0;
	}

}
