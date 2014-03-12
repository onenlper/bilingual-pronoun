package ims.coref;

import java.io.IOException;

import ims.coref.data.Document;
import ims.coref.io.DocumentReader;
import ims.coref.io.DocumentWriter;
import ims.coref.io.ReaderWriterFactory;

public class InOut {

	public static void main(String[] args) throws IOException{
		Options options=new Options(args);
		DocumentReader reader=ReaderWriterFactory.getReader(options.inputFormat, options.input);
		DocumentWriter writer=ReaderWriterFactory.getWriter(options.outputFormat, options.output);
		for(Document d:reader){
			System.out.println(d.header);
			writer.write(d);
		}
		writer.close();
	}
	
}
