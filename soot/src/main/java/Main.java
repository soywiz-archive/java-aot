import java.io.File;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import sootutils.SootUtils;
import sootutils.SootVisitor;


public class Main {

	public static void main(String... args) {
		
		StringBuffer sb = new StringBuffer();
		sb.append(new File("target/classes").getAbsolutePath()).append(';');
		
		// IDEALMENTE USARÍAMOS ESTO PARA LOCALIZAR EL RT
		System.out.println("JAVA_HOME: " + System.getenv("JAVA_HOME"));
		
		sb.append(new File("c:/Dev/jdk7/jre/lib/rt.jar").getAbsolutePath()).append(';');

		SootUtils.init(sb.toString());
		
		SootUtils.visit(new SootVisitor() {

			@Override
			public void on(SootClass sootClass) {
				out("class " + sootClass.getJavaPackageName() +"." + sootClass.getJavaStyleName() + "{");
			}			
			
			@Override
			public void on(SootClass sootClass, SootField sootField) {
				out("  " + sootField.getDeclaration());
			}
			
			@Override
			public void on(SootClass sootClass, SootMethod sootMethod) {
				out("\n  " + sootMethod.getDeclaration());
			}
			
			@Override
			public void on(SootClass sootClass, SootMethod sootMethod, Unit unit) {
				System.out.println("      " + unit.getClass()+" : " + unit);
			}


		}, "test.Person");
	}

	private static void out(String string) {
		System.out.println(string);
	}
}
