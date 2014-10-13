package fixtures;

public class SimpleFixture {
	static public A test(boolean v) {
		A a;
		if (v) {
			a = new B();
		} else {
			a = new C();
		}
		return a;
	}
}

class A { }
class B extends A { }
class C extends A { }
