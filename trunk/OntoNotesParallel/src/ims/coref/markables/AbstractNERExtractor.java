package ims.coref.markables;

import ims.coref.data.NE;
import ims.coref.data.Sentence;
import ims.coref.data.Span;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class AbstractNERExtractor extends AbstractMarkableExtractor {
	private static final long serialVersionUID = 1L;

	@Override
	public void extractMarkables(Sentence s, Set<Span> sink, String docName) {
		
		Set<Span> newSpan = new HashSet<Span>();
		
		for (NE ne : s.nes) {
			if (take(ne.getLabel()))
				newSpan.add(ne.getSpan());
		}

		// TODO; remove same head smaller ones here??
//		HashSet<Span> remove = new HashSet<Span>();
//		loop: for (Span s1 : newSpan) {
//			for (Span s2 : sink) {
//				if (s1.s.ids[s1.hd] == s2.s.ids[s2.hd] && s1.hd == s2.hd
//						&& (s1.end - s1.start > s2.end - s2.start)) {
//					//CC construct
//					for(int i=s2.start;i<=s2.end;i++) {
//						String pos = s.tags[i];
//						if(pos.equalsIgnoreCase("CC") || pos.equalsIgnoreCase("to")) {
//							continue loop;
//						}
//					}
//					remove.add(s2);
//					if (s.d.goldChainMap.containsKey(s2.getReadName())) {
//						System.out.println("Remove: " + s1.getText() + " # "
//								+ s2.getText() + " # "
//								+ s.d.goldChainMap.containsKey(s2.getReadName()));
//					} else {
//						System.out.println("Remove: " + s1.getText() + " # "
//								+ s2.getText() + " # "
//								+ s.d.goldChainMap.containsKey(s2.getReadName()));
//					}
//				}
//			}
//		}
//		newSpan.removeAll(remove);
		
		sink.addAll(newSpan);
	}

	abstract boolean take(String lbl);

	public static class AllNERExtractor extends AbstractNERExtractor {
		private static final long serialVersionUID = 1L;

		@Override
		boolean take(String lbl) {
			return true;
		}

		public String toString() {
			return "NER-ALL";
		}
	}

	public static class OneNERExtractor extends AbstractNERExtractor {
		private static final long serialVersionUID = 1L;

		public final String label;

		public OneNERExtractor(String label) {
			this.label = label;
		}

		@Override
		boolean take(String lbl) {
			return lbl.equals(label);
		}

		public String toString() {
			return "NER-" + label;
		}
	}

	public static class MultipleNERExtractor extends AbstractNERExtractor {
		private static final long serialVersionUID = 1L;

		private final Pattern pattern;

		public MultipleNERExtractor(String... labels) {
			StringBuilder sb = new StringBuilder("(?:" + labels[0]);
			for (int i = 1; i < labels.length; ++i)
				sb.append('|').append(labels[i]);
			sb.append(')');
			pattern = Pattern.compile(sb.toString());
		}

		@Override
		boolean take(String lbl) {
			return pattern.matcher(lbl).matches();
		}

	}

}
