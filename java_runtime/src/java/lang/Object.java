package java.lang;

public class Object {
	public boolean equals(Object other) {
		return this == other;
	}

	public int hashCode() {
		return 0;
	}

	public Class<? extends Object> getClass() {
		return null;
	}

	public String toString() {
		return "ObjectInstance";
	}
}