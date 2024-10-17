package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class MyTab extends Tab {

	public static final Struct boolType = new Struct(Struct.Bool);

	public static void init() {
		Tab.init();
		// add bool type!!
		currentScope.addToLocals(new Obj(Obj.Type, "bool", boolType));
	}

	public static void dump(SymbolTableVisitor stv) {
		System.out.println("=====================MY SYMBOL TABLE DUMP=========================");
		if (stv == null)
			stv = new MyDumpSimbolTableVisitor();
		for (Scope s = currentScope; s != null; s = s.getOuter()) {
			s.accept(stv);
		}
		System.out.println(stv.getOutput());
	}

	public static void dump() {
		// has to be redifined so method dump above is called!
		dump(null);
	}
}
