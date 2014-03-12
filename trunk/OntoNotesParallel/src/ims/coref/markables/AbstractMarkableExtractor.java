package ims.coref.markables;

import ims.coref.data.Document;
import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.io.DocumentReader;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import align.DocumentMap.Unit;

public abstract class AbstractMarkableExtractor implements IMarkableExtractor {
	private static final long serialVersionUID = 1L;

	@Override
	public Set<Span> extractMarkables(Document d) {
		Set<Span> sink = new TreeSet<Span>();
		for (Sentence s : d.sen) {
			extractMarkables(s, sink, d.docName);
		}

		// TODO; remove same head smaller ones here??
		HashSet<Span> remove = new HashSet<Span>();
		loop: for (Span s1 : sink) {
			for (Span s2 : sink) {
				if (s1.s == s2.s && s1.hd == s2.hd
						&& (s1.end - s1.start > s2.end - s2.start)) {
					// CC construct
					for (int i = s1.start; i <= s1.end; i++) {
						String pos = s1.s.tags[i];
						if (pos.equalsIgnoreCase("CC")
								|| pos.equalsIgnoreCase("to")) {
							continue loop;
						}
					}
					remove.add(s2);
					if (d.goldChainMap.containsKey(s2.getReadName())) {
//						System.out.println("Remove: "
//								+ s1.getText()
//								+ " # "
//								+ s2.getText()
//								+ " # "
//								+ s1.s.d.goldChainMap.containsKey(s2
//										.getReadName()));
					} else {
//						System.out.println("Remove: "
//								+ s1.getText()
//								+ " # "
//								+ s2.getText()
//								+ " # "
//								+ s1.s.d.goldChainMap.containsKey(s2
//										.getReadName()));
					}
				}
			}
		}
//		sink.removeAll(remove);

		return sink;
	}

	@Override
	public boolean needsTraining() {
		return false;
	}

	@Override
	public void train(DocumentReader reader) {
		throw new Error("you are wrong here");
	}

}
