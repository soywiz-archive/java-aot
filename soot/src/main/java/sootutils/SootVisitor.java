package sootutils;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JInvokeStmt;

public interface SootVisitor {

	void on(SootClass sootClass);

	void on(SootClass sootClass, SootField sootField);

	void on(SootClass sootClass, SootMethod sootMethod);

	void on(SootClass sootClass, SootMethod sootMethod, Unit unit);

	void on(SootClass sootClass, SootMethod sootMethod, JInvokeStmt jinvokestmt);
}
