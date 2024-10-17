package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;

public class MyDumpSimbolTableVisitor extends DumpSymbolTableVisitor {

	public void visitStructNode(Struct structToVisit) {
		int kind = structToVisit.getKind();

		if (kind == Struct.Int) {
			output.append("int");
		} else if (kind == Struct.Char) {
			output.append("char");
		} else if (kind == Struct.Bool) {
			output.append("bool");
		} else if (kind == Struct.None) {
			output.append("notype");
		} else if (kind == Struct.Class) {
			output.append("class [");
			for (Obj obj : structToVisit.getMembers()) {
				obj.accept(this);
			}
			output.append("]");
		} else if (kind == Struct.Array && structToVisit.getElemType().getKind() == Struct.Int) {
			output.append("Array[int]");
		} else if (kind == Struct.Array && structToVisit.getElemType().getKind() == Struct.Char) {
			output.append("Array[char]");
		} else if (kind == Struct.Array && structToVisit.getElemType().getKind() == Struct.Bool) {
			output.append("Array[bool]");
		} else if (kind == Struct.Array && structToVisit.getElemType().getKind() == Struct.None) {
			output.append("Array[noType]");
		} else if (kind == Struct.Array && structToVisit.getElemType().getKind() == Struct.Class) {
			output.append("Array[class]");
		}
	}
}
