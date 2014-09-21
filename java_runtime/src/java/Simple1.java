package java;

import libcore.Native;

public class Simple1 {
	static public int sum(int[] args) {
		int sum = 0;
		for (int arg : args) sum += arg;
		return sum;
	}

	static public int main(String[] args) {
		int[] list = new int[10];

		list[0] = 1;
		list[2] = 20;

		System.arraycopy(list, 0, list, 4, 4);

		int len = sum(list);

		Native.putchar(':');

		System.out.println("" + System.currentTimeMillis());
		System.out.println("" + System.currentTimeMillis());

		for (int n = 0; n < len; n++) {
			if ((n % 2) == 0) {
				System.out.println(("Hello world!" + n).toUpperCase());
				//System.out.println("Hello world!" + n);
			}
		}

		System.out.println("" + System.currentTimeMillis());

		//for (int n = 0; n < 10; n++) {
		//	for (int m = 0; m < 10; m++) {
		//		System.out.println("" + n + "x" + m + "=" + (n * m));
		//	}
		//}

		return 0;
	}
}
