package sootutils;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;

public interface SootVisitor {

	void on(SootClass sootClass);

	void on(SootClass sootClass, SootField sootField);

	void on(SootClass sootClass, SootMethod sootMethod);

	void on(SootClass sootClass, SootMethod sootMethod, Unit unit);

}
