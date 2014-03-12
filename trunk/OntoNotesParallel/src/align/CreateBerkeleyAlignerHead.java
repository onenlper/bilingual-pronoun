package align;

import ims.coref.Options;
import ims.coref.data.Chain;
import ims.coref.data.Document;
import ims.coref.data.Sentence;
import ims.coref.data.Span;
import ims.coref.io.DocumentReader;
import ims.coref.io.ReaderWriterFactory;
import ims.coref.lang.Language;
import ims.coref.markables.IMarkableExtractor;
import ims.coref.markables.MarkableExtractorFactory;
import ims.coref.training.GoldStandardChainExtractor;

import java.util.HashSet;
import java.util.Set;

import util.Common;
import util.Util;
import align.DocumentMap.DocForAlign;

public class CreateBerkeleyAlignerHead {

	public static void main(String args[]) throws Exception {
		if (args.length != 1) {
			System.out.println("java ~ gold|sys");
			System.exit(1);
		}
		if(!args[0].equals("gold") && !args[0].equals("sys")) {
			Common.bangErrorPOS("java ~ gold|sys");
		}
		boolean gold = args[0].equalsIgnoreCase("gold");
		String outputBase;
		if (gold) {
			outputBase = Util.headBAAlignBaseGold;
		} else {
			outputBase = Util.headBAAlignBaseSys;
		}
		DocumentMap.loadSentenceAlignResult();

		String engArg = "-gender /users/yzcchen/Downloads/gender.data.gz -lang eng "
				+ "-in engCoNLL.key -markableExtractors NT-NP,T-PRP,T-PRP$,NER-ALL";
		run(engArg.split("\\s+"), gold);

		String chiArg = "-gender /users/yzcchen/Downloads/gender.data.gz -lang chi "
				+ "-in chiCoNLL.key -markableExtractors NT-NP,T-PN,T-NR";
		run(chiArg.split("\\s+"), gold);
		DocumentMap.outputAlignFormatForBA(outputBase, gold);
		System.out.println(outputBase);

		int chiOverall = 0;
		int engOverall = 0;
		for (DocumentMap documentMap : DocumentMap.documentMaps) {
			chiOverall += documentMap.chiDoc.spanSize;
			engOverall += documentMap.engDoc.spanSize;
//			System.out.println(documentMap.parallel);
//			System.out.println(documentMap.chiDoc.spanSize + " # "
//					+ documentMap.engDoc.spanSize);
		}
		System.out.println("EngOverall: " + engOverall);
		System.out.println("ChiOverall: " + chiOverall);
	}

	public static void run(String args[], boolean gold) throws Exception {
		Options options = new Options(args);
		Language.initLanguage(options.lang);
		final IMarkableExtractor markableExtractor = MarkableExtractorFactory
				.getExtractorS(options.markableExtractors == null ? Language
						.getLanguage().getDefaultMarkableExtractors()
						: options.markableExtractors);

		DocumentReader reader = ReaderWriterFactory.getReader(
				options.inputFormat, options.input);

		if (markableExtractor.needsTraining()) {
			System.out.println("Training anaphoricy classifier");
			markableExtractor.train(reader);
		}
		int k = 0;
		for (Document d : reader) {
			String docName = d.docName;
//			System.out.println(d.docName + ":" + d.docNo + " " + (k++));
			DocumentMap documentMap = DocumentMap.getDocumentMap(docName,
					options.lang);
			DocForAlign docForAlign = null;
			if (options.lang.equalsIgnoreCase("eng")) {
				docForAlign = documentMap.engDoc;
			} else {
				docForAlign = documentMap.chiDoc;
			}

			Set<Span> spans = new HashSet<Span>();
			if (gold) {
				GoldStandardChainExtractor goldExtractor = new GoldStandardChainExtractor();
				Chain[] goldChain = goldExtractor.getGoldChains(d);
				for (Chain c : goldChain) {
					for (Span s : c.spans) {
						spans.add(s);
					}
				}
			}
			spans.addAll(markableExtractor.extractMarkables(d));
//			System.out.println(spans.size());

			docForAlign.spanSize += spans.size();

			for (Span s : spans) {
				// System.err.println(s.hd);
				Sentence sent = s.s;
				boolean CC = false;
				for(int i=s.start;i<=s.end;i++) {
					String tag = sent.tags[i];
					if(tag.equalsIgnoreCase("CC")) {
//						System.out.println(s.getText() + " @ " + sent.forms[s.hd]);
						CC = true;
						break;
					}
				}
				//skip NE and CC construct
				if(s.ne!=null || CC) {
					continue;
				}				
				for (int i = s.start; i <= s.end; i++) {
					int id = sent.ids[i];
					if (docForAlign.getUnit(id) == null
							|| !docForAlign.getUnit(id).token
									.equals(sent.forms[i])) {
						System.out.println(sent.forms[i] + " # " + id);
						Common.bangErrorPOS("");
					}
					if (docForAlign.getUnit(id).visable != 1) {
						docForAlign.getUnit(id).visable = -1;
					}
				}
				docForAlign.getUnit(sent.ids[s.hd]).visable = 1;
				// System.err.println(sent.ids[s.start] + " # " +
				// sent.ids[s.end]);
			}
		}

	}
}
