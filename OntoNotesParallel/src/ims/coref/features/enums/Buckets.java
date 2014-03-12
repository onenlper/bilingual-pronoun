package ims.coref.features.enums;

public enum Buckets implements IBuckets<Buckets> {
	Zero, One, Two, Three, Four, LTEight, LTTwelve, LTTwenty, Rest;
	
	public Buckets getBucket(int dist){
		switch(dist){
		case 0: return Buckets.Zero;
		case 1: return Buckets.One;
		case 2: return Buckets.Two;
		case 3: return Buckets.Three;
		case 4: return Buckets.Four;
		}
		
		if(dist<8)
			return Buckets.LTEight;
		if(dist<12)
			return Buckets.LTTwelve;
		if(dist<20)
			return Buckets.LTTwenty;
		
		return Buckets.Rest;
	}
}
