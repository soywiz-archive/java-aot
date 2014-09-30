package sootutils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JInvokeStmt;
import soot.options.Options;

public class SootUtils {

	public static void init(String classpath) {
		soot.G.reset();
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_include_all(true);
		Options.v().set_print_tags_in_output(true);

		Options.v().set_allow_phantom_refs(false);
		// Options.v().set_allow_phantom_refs(true)

		Options.v().set_keep_line_number(true);

		Options.v().set_soot_classpath(classpath);

		Options.v().setPhaseOption("jb.dae", "enabled:false");
		Options.v().setPhaseOption("jb.uce", "enabled:false");
		Options.v().setPhaseOption("jap.npc", "enabled:true");
		Options.v().setPhaseOption("jap.abc", "enabled:true");
		Options.v().setPhaseOption("jop", "enabled:true");
		Options.v().setPhaseOption("jop.cse", "enabled:false");
		Options.v().setPhaseOption("jop.bcm", "enabled:false");
		Options.v().setPhaseOption("jop.lcm", "enabled:false");
		Options.v().setPhaseOption("jop.cp", "enabled:false");
		Options.v().setPhaseOption("jop.cpf", "enabled:false");
		Options.v().setPhaseOption("jop.cbf", "enabled:false");
		Options.v().setPhaseOption("jop.dae", "enabled:false");
		Options.v().setPhaseOption("jop.nce", "enabled:false");
		Options.v().setPhaseOption("jop.uce1", "enabled:false");
		Options.v().setPhaseOption("jop.ubf1", "enabled:false");
		Options.v().setPhaseOption("jop.uce2", "enabled:false");
		Options.v().setPhaseOption("jop.ubf2", "enabled:false");
		Options.v().setPhaseOption("jop.ule", "enabled:false");
		Scene.v().loadNecessaryClasses();
	}

	public static void visit(SootVisitor sootVisitor, String className) {
		SootClass sootClass = Scene.v().loadClassAndSupport(className);

		sootVisitor.on(sootClass);

		for (SootField sootField : sootClass.getFields()) {
			sootVisitor.on(sootClass, sootField);
		}

		for (SootMethod sootMethod : sootClass.getMethods()) {
			visit(sootVisitor, sootMethod);
		}
	}

	public static void visit(SootVisitor sootVisitor, SootMethod sootMethod) {
		SootClass sootClass = sootMethod.getDeclaringClass();
		
		sootVisitor.on(sootClass, sootMethod);

		Body body = sootMethod.retrieveActiveBody();
		for (Unit unit : body.getUnits()) {
			if (unit instanceof JInvokeStmt) {
				sootVisitor.on(sootClass, sootMethod, (JInvokeStmt) unit);
			} else {
				sootVisitor.on(sootClass, sootMethod, unit);
			}
		}

	}
}
