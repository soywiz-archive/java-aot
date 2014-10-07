package sample1;

public class Sample1b {
	static public void main(String[] args) {
		//libcore._Native.putchar('H');
		final int value = 7;

		System.out.println("Sample1b:" + new MyTestInterface2() {
			@Override
			public int run() {
				return -value;
			}
		}.run());
	}
}

interface MyTestInterface2 {
	int run();
}