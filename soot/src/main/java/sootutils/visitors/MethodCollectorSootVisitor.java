package sootutils.visitors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import sootutils.SootVisitor;

public class MethodCollectorSootVisitor implements SootVisitor {

	private Map<SootClass, List<SootMethod>> visited = new HashMap<>();


	private Map<SootClass, List<SootMethod>> pending = new HashMap<>();

	private List<SootMethod> getVisitedMethodList(SootClass sootClass) {
		List<SootMethod> out = this.visited.get(sootClass);
		if (out == null) {
			out = new LinkedList<>();
			this.visited.put(sootClass, out);
		}
		return out;
	}

	private List<SootMethod> getPendingMethodList(SootClass sootClass) {
		List<SootMethod> out = this.pending.get(sootClass);
		if (out == null) {
			out = new LinkedList<>();
			this.pending.put(sootClass, out);
		}
		return out;
	}

	@Override
	public void on(SootClass sootClass, SootMethod sootMethod) {
		out("\n  " + sootMethod.getSignature());
		
		getVisitedMethodList(sootClass).add(sootMethod);
	}


	@Override
	public void on(SootClass sootClass, SootMethod sootMethod,
			JInvokeStmt jinvokestmt) {
		out("      Invk: " + jinvokestmt.getInvokeExpr());
		
		InvokeExpr invokeExpr = jinvokestmt.getInvokeExpr();
		SootClass sc = invokeExpr.getMethod().getDeclaringClass();
		SootMethod sm = invokeExpr.getMethod();
		if (!this.visited.containsKey(sc) || (this.visited.containsKey(sc) &&  !getVisitedMethodList(sc).contains(sm))) {
			getPendingMethodList(sc).add(sm);			
		}
	}
	
	public Map<SootClass, List<SootMethod>> getVisited() {
		return visited;
	}

	public Map<SootClass, List<SootMethod>> getPending() {
		return pending;
	}

	@Override
	public void on(SootClass sootClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void on(SootClass sootClass, SootField sootField) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void on(SootClass sootClass, SootMethod sootMethod, Unit unit) {
		out("      " + unit.getClass()+" : " + unit);
	}
	
	private void out(String string) {
		System.out.println(string);
	}
}
