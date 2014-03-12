package ims.coref.resolver;

import ims.coref.data.CorefSolution;
import ims.coref.data.Document;
import ims.coref.data.SpanListStruct;
import ims.coref.postprocessor.IChainPostProcessor;

public class StackedResolver implements ICoreferenceResolver{

	final private AbstractResolver r1;
	final private AbstractResolver r2;

	private final IChainPostProcessor pp;
	
	public StackedResolver(AbstractResolver r1,AbstractResolver r2,IChainPostProcessor pp){
		this.r1=r1;
		this.r2=r2;
		this.pp=pp;
	}
	
	@Override
	public CorefSolution resolve(Document d) {
		if(d.stackMap!=null)
			return resolveGivenStackMap(d);
		SpanListStruct spans=SpanListStruct.fromCollection(r1.me.extractMarkables(d));
		CorefSolution s1=r1.doResolve(spans, d);
		s1.assignStackMap(d);
		CorefSolution s2=r2.doResolve(spans, d);
		pp.postProcess(s2, d, spans);
		return s2;
	}

	public CorefSolution resolveGivenStackMap(Document d){
		SpanListStruct spans=SpanListStruct.fromCollection(r1.me.extractMarkables(d));
		CorefSolution s2=r2.doResolve(spans, d);
		pp.postProcess(s2, d, spans);
		return s2;
	}
	
	@Override
	public IChainPostProcessor getPostProcessor() {
		return pp;
	}
	
	public String toString(){
		return "Stacked resolver:\nStage 1:  "+r1.toString()+"\nStage 2:  "+r2.toString();
	}
}
