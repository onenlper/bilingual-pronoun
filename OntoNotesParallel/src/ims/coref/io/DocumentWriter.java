package ims.coref.io;

import java.io.IOException;

import ims.coref.data.Document;

public interface DocumentWriter {

	public void write(Document d) throws IOException;
	public void close() throws IOException;
	
}
