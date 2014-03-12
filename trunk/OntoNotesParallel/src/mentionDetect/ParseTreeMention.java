package mentionDetect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import mentionDetection.chinese.ChineseMention;
import model.Element;
import model.EntityMention;
import model.CoNLL.CoNLLPart;
import util.OntoCommon;

public class ParseTreeMention extends MentionDetect {

	OntoCommon ontoCommon;

	@Override
	public ArrayList<EntityMention> getMentions(CoNLLPart part) {
		if (part.getDocument().getLanguage().equalsIgnoreCase("chinese")) {
			ChineseMention ch = new ChineseMention();
			return ch.getChineseMention(part);
		} else if (part.getDocument().getLanguage().equalsIgnoreCase("english")) {
			ontoCommon = new OntoCommon("english");
			return getEnglishMention(part);
		} 
		return null;
	}

	private ArrayList<EntityMention> getNamedEntityMention(CoNLLPart part) {
		ArrayList<EntityMention> mentions = new ArrayList<EntityMention>();
		ArrayList<Element> namedEntities = part.getNameEntities();
		for (Element element : namedEntities) {
			if (element.content.equalsIgnoreCase("QUANTITY") || element.content.equalsIgnoreCase("CARDINAL")
					|| element.content.equalsIgnoreCase("PERCENT")) {
				continue;
			}
			// Mr. Mandela
			if (element.start > 0
					&& this.ontoCommon.getEnDictionary().titles.contains(part.getWord(element.start - 1).word)) {
				continue;
			}
			int end = element.end;
			int start = element.start;
			if (element.end + 1 < part.getWordCount()) {
				String lastWord = part.getWord(element.end + 1).word;
				if (lastWord.equalsIgnoreCase("'s")) {
					end++;
				}
			}
			EntityMention mention = new EntityMention(start, end);
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for (int i = start; i <= end; i++) {
				sb.append(part.getWord(i).word).append(" ");
				sb2.append(part.getWord(i).orig).append(" ");
			}
			mention.source = sb.toString().trim().toLowerCase();
			mention.original = sb2.toString().trim();
			if (!mentions.contains(mention)) {
				mentions.add(mention);
			}
		}
		return mentions;
	}

	private ArrayList<EntityMention> getNPorPRPMention(CoNLLPart part) {
		ArrayList<EntityMention> npMentions = new ArrayList<EntityMention>();
		npMentions = ontoCommon.getAllNounPhrase(part.getCoNLLSentences());

		// MentionDetect md = new GoldBoundaryMentionTest();
		// npMentions = md.getMentions(part);
		for (int g = 0; g < npMentions.size(); g++) {
			EntityMention npMention = npMentions.get(g);
			int end = npMention.end;
			int start = npMention.start;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for (int i = start; i <= end; i++) {
				sb.append(part.getWord(i).word).append(" ");
				sb2.append(part.getWord(i).orig).append(" ");
			}
			npMention.source = sb.toString().trim().toLowerCase();
			npMention.original = sb2.toString().trim();
			// System.out.println(start + " " + end + " " + npMention.source);

			// for (Element NE : part.getNameEntities()) {
			// if (NE.content.equalsIgnoreCase("QUANTITY") ||
			// NE.content.equalsIgnoreCase("QUANTITY")
			// || NE.content.equalsIgnoreCase("PERCENT")) {
			// continue;
			// }
			// if (npMention.start >= NE.start && npMention.end <= NE.end) {
			// npMentions.remove(g);
			// g--;
			// break;
			// }
			// }
		}
		return npMentions;
	}


	public static HashMap<String, Double> stats;

	public static double t5 = -1;

	private ArrayList<EntityMention> getEnglishMention(CoNLLPart part) {
		ArrayList<EntityMention> mentions = new ArrayList<EntityMention>();
		mentions.addAll(this.getNamedEntityMention(part));
		mentions.addAll(this.getNPorPRPMention(part));
		removeDuplicateMentions(mentions);
		return mentions;
	}


	private void removeDuplicateMentions(ArrayList<EntityMention> mentions) {
		HashSet<EntityMention> mentionsHash = new HashSet<EntityMention>();
		mentionsHash.addAll(mentions);
		mentions.clear();
		mentions.addAll(mentionsHash);
	}
}
