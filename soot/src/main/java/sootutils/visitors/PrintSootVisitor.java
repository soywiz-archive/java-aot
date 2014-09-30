package sootutils.visitors;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JInvokeStmt;
import sootutils.SootVisitor;

public class PrintSootVisitor implements SootVisitor {

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
		out("      " + unit.getClass()+" : " + unit);
	}

	@Override
	public void on(SootClass sootClass, SootMethod sootMethod,
			JInvokeStmt jinvokestmt) {
		out("      Invk: " + jinvokestmt.getInvokeExpr());
	}
	
	private void out(String string) {
		System.out.println(string);
	}
}
