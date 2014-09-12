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
		test1.demo();
		return test1;
	}

	private void demo() {

	}

	private int test = 0;

	public void increment() {
		test++;
	}

	static public int doIf(int a, int b) {
		if (a < b) {
			return a;
		} else {
			return a + b;
		}
	}

	static public int doFor(int a, int b) {
		int sum = 0;
		for (int n = a; n < b; n++) {
			sum += n;
		}
		return sum;
	}

	static public int doFor2(int a, int b) {
		int sum = 0;
		for (int n = a; n < b; n++) {
			if ((n % 2) != 0) sum += n;
		}
		return sum;
	}

	static public void doTryCatch() {
		try {
			throw(new Exception("aaa"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void doSwitch(int a) {
		switch (a) {
			case 0: System.out.println(0); break;
			case 1: System.out.println(1); break;
			case 2: System.out.println(2); break;
		}
	}

	static public void doSwitchMap(int a) {
		switch (a) {
			case 0: System.out.println(0); break;
			case 1111: System.out.println(1); break;
			case 2222: System.out.println(2); break;
		}
	}
}
