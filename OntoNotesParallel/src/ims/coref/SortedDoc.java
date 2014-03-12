package ims.coref;

public class SortedDoc implements Comparable<SortedDoc> {
	double all;
	double match;
	String lang;
	String id;
	double percent;

	public SortedDoc(String id, String lang, double all, double match) {
		this.id = id;
		this.lang = lang;
		this.all = all;
		this.match = match;
		this.percent = match / all;
	}

	public void tostring() {
		System.out.format(
				"%s\t%s\nOverall: %f,\t Match: %f,\t Percent:%f\n=========\n",
				this.id, this.lang, this.all, this.match, this.percent);
	}

	@Override
	public int compareTo(SortedDoc sd2) {
		if (this.percent > sd2.percent) {
			return 1;
		} else if (this.percent == sd2.percent) {
			return 0;
		} else {
			return -1;
		}
	}
}