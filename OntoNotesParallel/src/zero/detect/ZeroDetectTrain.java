package zero.detect;


public class ZeroDetectTrain extends ZeroDetect{

	public ZeroDetectTrain(String folder) {
		super(true, folder);
	}
	
	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("java ~ folder");
			System.exit(1);
		}
		String folder = args[0];

		ZeroDetectTrain train = new ZeroDetectTrain(folder);
		// //TODO
		train.generateInstances();
		
		System.out.println(train.positive);
		System.out.println(train.negative);
		System.out.println(train.negative/train.positive);
		
		System.out.println("Docs: " + ZeroDetectTrain.docs);
		System.out.println("ZPs: " + ZeroDetectTrain.zps);
		System.out.println("AZPs: " + ZeroDetectTrain.azps);
	}
}
