package ims.coref.features;

import ims.coref.data.Document;
import ims.coref.data.PairInstance;
import ims.coref.data.Span;

import java.util.Collection;

public class F_loneFeas extends AbstractMultiDataDrivenFeature {

	protected F_loneFeas(String name, int cutOff) {
		super("lone" + name, cutOff);
		// TODO Auto-generated constructor stub
	}

	@Override
	public <T extends Collection<String>> T getFeatureStrings(PairInstance pi,
			Document d, T container) {
		// TODO Auto-generated method stub
		container.add("Y_");
		
		Span ana = pi.ana;
		String genra = d.genre;
		container.add("G_" + genra);
		
		String n1 = "-";
		if(ana.end+1<ana.s.forms.length) {
			n1 = ana.s.forms[ana.end+1].toLowerCase(); 
		}
		container.add("N1_" + n1);
		
		String n2 = "-";
		if(ana.end+2<ana.s.forms.length) {
			n2 = ana.s.forms[ana.end+2].toLowerCase(); 
		}
		container.add("N2_" + n2);
		

		String p1 = "-";
		if(ana.start>0) {
			p1 = ana.s.forms[ana.start-1].toLowerCase(); 
		}
		container.add("P1_" + p1);
		
		String p2 = "-";
		if(ana.start>1) {
			p2 = ana.s.forms[ana.start-2].toLowerCase(); 
		}
		container.add("P2_" + p2);
		
		container.add("Nu_" + ana.number.name());
		container.add("Ge_" + ana.gender.name());
		return container;
	}

}
