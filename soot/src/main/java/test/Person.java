package test;
import java.sql.Date;


public class Person {

	private final String name;
	private final Date birthDate;
	
	public Person(String name, Date birthDate) {
		this.name = name;
		this.birthDate = birthDate;
	}

	public String getName() {
		return name;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void mimetodo() {
		for (int i = 0; i<10; i++) {
			System.out.println(i);
		}
	}
}
