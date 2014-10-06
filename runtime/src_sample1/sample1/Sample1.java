package sample1;

public class Sample1 {
	static public int main(String[] args) {
		//libcore.Native.putchar('H');
		System.out.println("Hello world!");
		for (int n = 0; n < 10; n++) System.out.print("" + n);
		System.out.println("");
		Sample1b.main(args);
		Sample2.main(args);
		return 0;
	}
}