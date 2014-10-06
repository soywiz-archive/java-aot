package sample1;

public class Sample1b {
	static public int main(String[] args) {
		//libcore.Native.putchar('H');
		final int value = 7;

		System.out.println("Sample1b:" + new MyTestInterface2() {
			@Override
			public int run() {
				return -value;
			}
		}.run());
		return 0;
	}
}

interface MyTestInterface2 {
	int run();
}