package ims.coref.io;

import java.io.File;
import java.io.IOException;

import ims.coref.Options.Format;

public class ReaderWriterFactory {

	public static DocumentReader getReader(Format format,File input) throws IOException{
		switch(format){
//		case Custom:	return new CustomFormatReader(input);
		case C12:		return new C12Reader(input);
		default:
		}
		throw new Error("Not implemented");
	}
	
	public static DocumentWriter getWriter(Format format,File output) throws IOException{
		switch(format){
		case C12:		return new C12Writer(output);
		case Custom:
		default:
		}
		throw new Error("Not implemented");
	}
	
}
