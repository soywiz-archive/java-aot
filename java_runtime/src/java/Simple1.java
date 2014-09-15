package java;

public class Simple1 {
	static public int sum(int[] args) {
		int sum = 0;
		for (int n = 0; n < args.length; n++) {
			sum += args[n];
		}
		return sum;
	}

	static public int main(String[] args) {
		int[] list = new int[10];
		list[0] = 1;
		list[2] = 20;
		int len = sum(list);

		for (int n = 0; n < len; n++) {
			if ((n % 2) == 0) {
				System.out.println("Hello world!" + n);
			}
		}
		return 0;
	}
}
