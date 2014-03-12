package ims.coref.io;

import ims.coref.data.Document;
import ims.coref.data.Sentence;
import ims.coref.lang.Language;
import ims.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractReader implements DocumentReader{

	protected final File input;
	
	public AbstractReader(File input){
		this.input=input;
	}
	
	static final Pattern WS=Pattern.compile("\\s+");
	static final Pattern BLANK=Pattern.compile("^(_|-)$");
	static final Pattern BAR=Pattern.compile("\\|");
	
	
	
	@Override
	public Iterator<Document> iterator() {
		try {
			return new DocumentIterator();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	class DocumentIterator implements Iterator<Document> {
		private BufferedReader reader;
		private Document next;
		
		DocumentIterator() throws IOException{
			reader=Util.getReader(input);
			next=readNext();
		}
		
		@Override
		public boolean hasNext() {
			return next!=null;
		}

		@Override
		public Document next() {
			Document ret=next;
			try {
				next=readNext();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return ret;
		}

		@Override
		public void remove() {
			throw new RuntimeException("not implemented");
		}
		
		private Document readNext() throws IOException{
			String header=reader.readLine();
			if(header==null){
				reader.close();
				return null;
			}
			List<String> lines=new ArrayList<String>();
			Document d=new Document(header);

//			List<Sentence> sen=new ArrayList<Sentence>();
			String line;
			String footer=null;
			int senCount=0;
			String lastSpeaker=null;
			Sentence lastSen=null;
			while((line=reader.readLine())!=null){
				if(line.isEmpty()){
					Sentence s=createSentence(lines,senCount++,d,lastSpeaker);
					if(lastSen!=null && !lastSen.speaker[1].equals(s.speaker[1]))
						lastSpeaker=lastSen.speaker[1];
					lastSen=s;
					Language.getLanguage().preprocessSentence(s);
					d.addSentence(s);
					lines.clear();
					continue;
				}
				if(line.startsWith("#end document")){
					footer=line;
					break;
				}
				lines.add(line);
			}
			if(footer==null)
				throw new RuntimeException("Reached eof before #end document marker");
			d.setFooter(footer);
			return d;
		}
	}
	
	abstract Sentence createSentence(List<String> lines,int senIndex,Document d,String lastSpeaker);
}
