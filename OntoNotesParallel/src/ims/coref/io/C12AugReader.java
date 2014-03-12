package ims.coref.io;

import ims.coref.data.DepTree;
import ims.coref.data.Document;
import ims.coref.data.Sentence;

import java.io.File;
import java.util.List;

public class C12AugReader extends C12Reader {

	public C12AugReader(File input) {
		super(input);
	}

	@Override
	Sentence createSentence(List<String> lines, int senIndex, Document d,String lastSpeaker) {
		String[] forms=new String[lines.size()+1];
		String[] lemmas=new String[lines.size()+1];
		String[] tags=new String[lines.size()+1];
		String[] cfgCol=new String[lines.size()+1];
		String[] neCol=new String[lines.size()+1];
		String[] speakerCol=new String[lines.size()+1];
		String[] corefCol=new String[lines.size()+1];
		int[] heads=new int[lines.size()+1];
		String[] labels=new String[lines.size()+1];
		
		forms[0]=ROOT_FORM;
		lemmas[0]=ROOT_LEMMA;
		tags[0]=ROOT_TAG;
		cfgCol[0]=ROOT_CFG;
		neCol[0]=ROOT_NE;
		speakerCol[0]=ROOT_SPEAKER;
		corefCol[0]=ROOT_COREF;
		heads[0]=ROOT_HEAD;
		labels[0]=ROOT_LBL;
		for(int i=0;i<lines.size();++i){
			String line=lines.get(i);
			String[] cols=WS.split(line);
			forms[i+1]=cols[FORM_COL];
			lemmas[i+1]=cols[LEMMA_COL];
			tags[i+1]=cols[TAG_COL];
			cfgCol[i+1]=cols[CFG_COL];
			neCol[i+1]=cols[NE_COL];
			speakerCol[i+1]=cols[SPEAKER_COL];
			corefCol[i+1]=cols[cols.length-3];
			heads[i+1]=Integer.parseInt(cols[cols.length-2]);
			labels[i+1]=cols[cols.length-1];
		}
		DepTree dt=new DepTree(heads,labels);
		Sentence s=new Sentence(senIndex,forms,tags,null,dt,corefCol,speakerCol,neCol,cfgCol,lemmas,d,lastSpeaker);
		return s;
	}

	

}
