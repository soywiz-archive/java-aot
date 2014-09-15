package java.lang;

public class Object {
	native public boolean equals(Object other);
	native public int hashCode();
	native public Class<? extends Object> getClass();
	native public String toString();
}