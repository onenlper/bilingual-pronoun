package ims.coref.util;

import ims.coref.data.Document;
import ims.coref.data.Sentence;

public class CorefUtil {

	public static boolean sameDocument(Document d, Document p) {
		return sameDocument(d,p,false);
	}
	
	public static boolean sameDocument(Document d, Document p,boolean tryHard) {
		if(d.docName!=null && !d.docName.equals(p.docName))
			return false;
		if(d.sen.size()!=p.sen.size())
			return false;
		for(int i=0;i<d.sen.size();++i)
			if(d.sen.get(i).forms.length!=p.sen.get(i).forms.length)
				return false;
		if(tryHard){
			for(int i=0;i<d.sen.size();++i){
				Sentence s1=d.sen.get(i);
				Sentence s2=p.sen.get(i);
				for(int j=1;j<s1.forms.length;++j)
					if(!s1.forms[j].equals(s2.forms[j]))
						return false;
			}
		}
		return true;
	}
}
