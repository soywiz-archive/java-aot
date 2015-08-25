import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import soot.SootClass;
import soot.SootMethod;
import sootutils.SootUtils;
import sootutils.visitors.MethodCollectorSootVisitor;

public class Main3 {

	public static void main(String... args) {

		StringBuffer sb = new StringBuffer();
		sb.append(new File("target/classes").getAbsolutePath()).append(';');

		// IDEALMENTE USARÍAMOS ESTO PARA LOCALIZAR EL RT
		System.out.println("JAVA_HOME: " + System.getenv("JAVA_HOME"));

		sb.append(new File("c:/Dev/jdk7/jre/lib/rt.jar").getAbsolutePath())
				.append(';');

		SootUtils.init(sb.toString());

		// Print visitor example
		// SootUtils.visit(new PrintSootVisitor(), "test.Person");

		MethodCollectorSootVisitor visitor = new MethodCollectorSootVisitor();
		SootUtils.visit(visitor, "test.Person");
		while (!visitor.getPending().isEmpty()) {
			for (Entry<SootClass, List<SootMethod>> entry : visitor
					.getPending().entrySet()) {
				for (SootMethod sootMethod : new ArrayList<>(entry.getValue())) {
					SootUtils.visit(visitor, sootMethod);
				}
			}
		}

		System.out.println("----------------------");
		for (Entry<SootClass, List<SootMethod>> entry : visitor.getVisited()
				.entrySet()) {
			System.out.println("class " + entry.getKey().getName());
			for (SootMethod sootMethod : new ArrayList<>(entry.getValue())) {
				System.out.println("   - " + sootMethod.getDeclaration());
			}
		}
	}

}
