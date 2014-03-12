package zero.detect;


public class ZeroDetectTest extends ZeroDetect{

	public ZeroDetectTest(String folder) {
		super(false, folder);
	}
	
	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("java ~ folder");
			System.exit(1);
		}
		String folder = args[0];

		ZeroDetectTest test = new ZeroDetectTest(folder);
		test.generateInstances();
		
		System.out.println("Docs: " + ZeroDetectTrain.docs);
		System.out.println("ZPs: " + ZeroDetectTrain.zps);
		System.out.println("AZPs: " + ZeroDetectTrain.azps);
	}
}
