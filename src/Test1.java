public class Test1 {
	static public int sum(int a, int b) {
		return a + b;
	}
	static public int mul(int a, int b) {
		return a * b;
	}
	static public long lmul(long a, long b) {
		return a * b;
	}
	static public long lmul2(long a, long b) {
		long z = a * b;
		return z * z;
	}
	static public long lmul4(long a, long b) {
		return a * b + a * b * a + b;
	}
	static public long call2(long a, long b) {
		return lmul4(a, b) + mul((int)a, (int)b);
	}
	static public Test1 newInstance() {
		Test1 test1 = new Test1();
		return test1;
	}
}
