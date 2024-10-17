package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.*;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import rs.ac.bg.etf.pp1.ast.*;

public class SemanticPass extends VisitorAdaptor {
	int printCallCount = 0;
	int varDeclCount = 0;

	Obj currentMethod = null;
	Struct currentType = null;
	int nVars;

	private static boolean noMain = true;
	private static boolean errorDetected = false;

	Logger log = Logger.getLogger(getClass());

	// pre-defined methids:
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
	}

	// type:
	public void visit(Type type) {
		Obj typeObj = MyTab.find(type.getTypeName());

		if (typeObj == MyTab.noObj) {
			currentType = MyTab.noType;
			report_error("Error (line: " + type.getLine() + ") Ne postoji tip: " + type.getTypeName(), type);
			type.struct = MyTab.noType;
			return;
		}

		if (Obj.Type != typeObj.getKind()) {
			currentType = MyTab.noType;
			report_error("Error (line: " + type.getLine() + ") Ne postoji tip: " + type.getTypeName(), type);
			type.struct = MyTab.noType;
			return;
		}

		currentType = typeObj.getType();
		type.struct = typeObj.getType();
	}

	// program:
	public void visit(ProgName progName) {
		Obj myObj = MyTab.insert(Obj.Prog, progName.getProgName(), MyTab.noType);
		progName.obj = myObj;
		MyTab.openScope();
	}

	public void visit(Program program) {
		if (noMain)
			report_error("Error (line: " + program.getLine() + ") Main ne postoji", null);

		nVars = MyTab.currentScope.getnVars();
		MyTab.chainLocalSymbols(program.getProgName().obj);
		MyTab.closeScope();
	}

	// promenjive:
	public void visit(VarIdentifierDecl varIdentDecl) {
		// staro:
//		if (MyTab.find(varIdentDecl.getVarName()) == MyTab.noObj) {
//			MyTab.insert(Obj.Var, varIdentDecl.getVarName(), currentType);
//			report_info("Info (line: " + varIdentDecl.getLine() + ") Dodata promenjiva: " + varIdentDecl.getVarName(),
//					null);
//			return;
//		}
//		report_error(
//				"Error (line: " + varIdentDecl.getLine() + ") Promenjiva vec deklarisana: " + varIdentDecl.getVarName(),
//				null);
		// doovde
		// podrska za sakrivanje lokalnih prom:
		Obj obj = MyTab.find(varIdentDecl.getVarName());

		if (obj == MyTab.noObj) {
			MyTab.insert(Obj.Var, varIdentDecl.getVarName(), currentType);
			report_info("Info (line: " + varIdentDecl.getLine() + ") Dodata promenjiva: " + varIdentDecl.getVarName(),
					null);
			return;
		}

		if (obj.getKind() == Obj.Con && currentMethod == null) {
			report_error("Error (line: " + varIdentDecl.getLine() + ") Postoji konstanta sa ovim imenom: "
					+ varIdentDecl.getVarName(), null);
			return;
		}

		if ((currentMethod != null && obj.getLevel() == 1) || (currentMethod == null && obj.getLevel() == 0)) {
			report_error("Error (line: " + varIdentDecl.getLine() + ") Promenjiva vec deklarisana u ovom scope-u: "
					+ varIdentDecl.getVarName(), null);
			return;
		}

		MyTab.insert(Obj.Var, varIdentDecl.getVarName(), currentType);
		report_info("Info (line: " + varIdentDecl.getLine() + ") Dodata promenjiva: " + varIdentDecl.getVarName(),
				null);
		return;
	}

	// constants
	public void visit(ConstDecl constDecl) {
		Struct a = constDecl.getConstType().obj.getType();
		Struct b = constDecl.getType().struct;

		if (a != b) {
			report_error("Error (line: " + constDecl.getLine() + ") Neodgovarajuci tip: " + constDecl.getConstName(),
					null);
			return;
		}

		if (MyTab.find(constDecl.getConstName()) != MyTab.noObj) {
			report_error(
					"Error (line: " + constDecl.getLine() + ")  Konstanta vec postoji: " + constDecl.getConstName(),
					null);
			return;
		}

		MyTab.currentScope.addToLocals(new Obj(Obj.Con, constDecl.getConstName(), constDecl.getType().struct,
				constDecl.getConstType().obj.getAdr(), 0));
		report_info("Info (line: " + constDecl.getLine() + ") Nova kosntanta dodata: " + constDecl.getConstName(),
				null);
	}

	// za listu constanti
	public void visit(ConstIdentifierDecl constIdent) {
		Struct type2 = constIdent.getConstType().obj.getType();

		if (!currentType.compatibleWith(type2)) {
			report_error("Error (line: " + constIdent.getLine() + ") Neodgovarajuci tip: " + constIdent.getConstName(),
					null);
			return;
		}

		if (MyTab.find(constIdent.getConstName()) != MyTab.noObj) {
			report_error(
					"Error (line: " + constIdent.getLine() + ")  Konstanta vec postoji: " + constIdent.getConstName(),
					null);
			return;
		}

		MyTab.currentScope.addToLocals(new Obj(Obj.Con, constIdent.getConstName(),
				constIdent.getConstType().obj.getType(), constIdent.getConstType().obj.getAdr(), 0));
		report_info("Info (line: " + constIdent.getLine() + ") Nova kosntanta dodata: " + constIdent.getConstName(),
				null);
	}

	public void visit(NumberConst numCnst) {
		numCnst.obj = new Obj(Obj.Con, "numCnst", MyTab.intType, numCnst.getNumVal().intValue(), 0);
	}

	public void visit(BooleanConst boolCnst) {
		boolCnst.obj = new Obj(Obj.Con, "boolCnst", MyTab.boolType, boolCnst.getBoolVal().compareTo(false), 0);
	}

	public void visit(CharacterConst charCnst) {
		charCnst.obj = new Obj(Obj.Con, "charCnst", MyTab.charType, charCnst.getCharVal().charValue(), 0);
	}

	// methods:
	public void visit(MethodTypeName methodTypeName) {
		report_info("Info (line: " + methodTypeName.getLine() + ") F-ja: " + methodTypeName.getMethName(), null);

		if (methodTypeName.getMethName().equalsIgnoreCase("main"))
			noMain = false;

		Obj newObj = MyTab.insert(Obj.Meth, methodTypeName.getMethName(), methodTypeName.getMethodRetType().struct);
		currentMethod = newObj;
		methodTypeName.obj = newObj;

		MyTab.openScope();
	}

	public void visit(MethodDecll methodDecl) {
		if (currentMethod != null) {
			MyTab.chainLocalSymbols(currentMethod);
			MyTab.closeScope();
		}
		currentMethod = null;
	}

	public void visit(ReturnType retType) {
		retType.struct = retType.getType().struct;
	}

	public void visit(NoReturn noRetType) {
		noRetType.struct = MyTab.noType;
	}

	// arrays:
	public void visit(ArrayName arrayName) {
		arrayName.obj = MyTab.find(arrayName.getImeNiza());
	}

	public void visit(ArrayIdentDecl arrayIdentDecl) {
		// staro:
//		if (MyTab.find(arrayIdentDecl.getArrayName()) == MyTab.noObj) {
//			MyTab.insert(Obj.Var, arrayIdentDecl.getArrayName(), new Struct(Struct.Array, currentType));
//			return;
//		}
//
//		report_error("Error(line: " + arrayIdentDecl.getLine() + ") Niz vec postoji" + arrayIdentDecl.getArrayName(),
//				null);
		// doovde
		// sa podrzkom za sakrivanje globalnih promenjivih:
		Obj obj = MyTab.find(arrayIdentDecl.getArrayName());

		if (obj == MyTab.noObj) {
			MyTab.insert(Obj.Var, arrayIdentDecl.getArrayName(), new Struct(Struct.Array, currentType));
			report_info("Info (line: " + arrayIdentDecl.getLine() + ") Dodat niz: " + arrayIdentDecl.getArrayName(),
					null);
			return;
		}

		if (obj.getKind() == Obj.Con && currentMethod == null) {
			report_error("Error (line: " + arrayIdentDecl.getLine() + ") Postoji konstanta sa ovim imenom: "
					+ arrayIdentDecl.getArrayName(), null);
			return;
		}

		if ((currentMethod != null && obj.getLevel() == 1) || (currentMethod == null && obj.getLevel() == 0)) {
			report_error("Error (line: " + arrayIdentDecl.getLine()
					+ ") Promenjiva sa ovim imenom vec deklarisana u ovom scope-u: " + arrayIdentDecl.getArrayName(),
					null);
			return;
		}

		MyTab.insert(Obj.Var, arrayIdentDecl.getArrayName(), new Struct(Struct.Array, currentType));
		report_info("Info (line: " + arrayIdentDecl.getLine() + ") Dodat niz: " + arrayIdentDecl.getArrayName(), null);
		return;
	}

	// designator:
	
	
	public void visit(DesignatorNoArray designatorNoArray) {
		designatorNoArray.obj = MyTab.find(designatorNoArray.getDesigName());
		Obj o = MyTab.find(designatorNoArray.getDesigName());
		if (o == MyTab.noObj) {
			report_error("Error (line: " + designatorNoArray.getLine() + ") Nije deklarisano ime: "
					+ designatorNoArray.getDesigName(), null);
		o = MyTab.noObj;
		}
		else{
			if(o.getType().getKind() != Struct.Array) {
				// promenljiva
				report_info("Info (line: " + designatorNoArray.getLine() + ") Pronadjeno: " + designatorNoArray.getDesigName() + " Object: " + getObjType(o.getKind()) + " " + designatorNoArray.getDesigName() + ": " + getStructType(o.getType().getKind()) + ", " + o.getAdr() + ", " + o.getLevel(), null);                                                                                 
			} else {
				// referenca na niz (npr: niz = new int[size])
				report_info("Info (line: " + designatorNoArray.getLine() + ") Pronadjeno: " + designatorNoArray.getDesigName() + " Object: " + getObjType(o.getKind()) + " " + designatorNoArray.getDesigName() + ": " + getStructType(o.getType().getKind()) + " of " + getStructType(o.getType().getElemType().getKind()) + ", " + o.getAdr() + ", " + o.getLevel(), null);
			}
		}
	}

	public void visit(DesignatorArray desigArray) {
		Obj obj = MyTab.find(desigArray.getArrayName().getImeNiza());
		int kind = obj.getKind();
		Struct type = obj.getType();

		desigArray.obj = new Obj(Obj.Elem, desigArray.getArrayName().getImeNiza(), obj.getType().getElemType());
		
		if (obj == MyTab.noObj) {
			report_error("Error (line: " + desigArray.getLine() + ") Nije deklarisano ime: "
					+ desigArray.getArrayName().getImeNiza(), null);
			obj = MyTab.noObj;
			return;
		}
		if (obj.getKind() != Obj.Var) {
			report_error("Error (line: " + desigArray.getLine() + ") Nije promenjiva: "
					+ desigArray.getArrayName().getImeNiza(), null);
			obj = MyTab.noObj;
			return;
		}
		if (obj.getType().getKind() != Struct.Array) {
			report_error(
					"Error (line: " + desigArray.getLine() + ") Nije niz: " + desigArray.getArrayName().getImeNiza(),
					null);
			obj = MyTab.noObj;//izbrisi ovo ako radi
			return;
		}
		if (desigArray.getExpr().struct != MyTab.intType) {
			report_error("Error (line: " + desigArray.getLine() + ") Indeks nije int", null);
			obj = MyTab.noObj;
			return;
		}
		
		if(obj != MyTab.noObj) {
			report_info("Info (line: " + desigArray.getLine() + ") Pronadjeno: " + desigArray.getArrayName().getImeNiza() + " Object: " + getObjType(obj.getKind()) + " " + desigArray.getArrayName().getImeNiza() + ": " + getStructType(obj.getType().getKind()) + " of " + getStructType(obj.getType().getElemType().getKind()) + ", " + obj.getAdr() + ", " + obj.getLevel(), null);
		}
	}

	public void visit(DesignatorAssignment desigAssign) {
		Obj designator = desigAssign.getDesignator().obj;
		int desKind = designator.getKind();
		Struct type = designator.getType();
		Struct expression = desigAssign.getExpr().struct;
		
		//report_info("Info (line: " + desigAssign.getLine() + ") %%%%%%%%%" + designator.getName(),  null);
		
		//report_info(designator + " " +desKind + " "+ type+" "+expression +" ", null);
		
		if(designator == null || expression == null || type == null || designator == MyTab.noObj || type == MyTab.noType) {
			return;
		}

		if (desKind != Obj.Var && desKind != Obj.Elem) {
			report_error("Error (line: " + desigAssign.getLine() + ") Can not asign value to this type", null);
			return;
		}

		if (!expression.assignableTo(type)) {
			report_error("Error (line: " + desigAssign.getLine() + ") Not compatable types", null);
			return;
		}

		//report_info("Info (line: " + desigAssign.getLine() + ") Dodela uspesna", null);
	}

	// factor:
	public void visit(BoolFactor boolFact) {
		boolFact.struct = MyTab.boolType;
	}

	public void visit(CharFactor charFact) {
		charFact.struct = MyTab.charType;
	}

	public void visit(NumFactor numFact) {
		numFact.struct = MyTab.intType;
	}

	public void visit(DesigFactor desigFact) {
		desigFact.struct = desigFact.getDesignator().obj.getType();
	}

	public void visit(PriorityFact priorFact) {
		Struct type = priorFact.getExpr().struct;
		if (type == MyTab.intType) {
			priorFact.struct = type;
			return;
		}
		priorFact.struct = MyTab.noType;
		report_error("Error (line: " + priorFact.getLine() + ") u zagradama nije izraz tipa int!", null);
	}

	public void visit(ConstructorFact constrFact) {
		Struct type = constrFact.getExpr().struct;
		if (type == MyTab.intType) {
			constrFact.struct = new Struct(Struct.Array, constrFact.getType().struct);
			//report_info("Info (line: " + constrFact.getLine() + ") Konstrukcija novog niza ", null);
			return;
		}
		report_error("Error (line: " + constrFact.getLine() + ") Vel. niza mora biti tipa int!", null);
		constrFact.struct = MyTab.noType;
	}

	public void visit(FactorTerm facTerm) {
		facTerm.struct = facTerm.getFactor().struct;
	}

	// expression:
	public void visit(MulopTerm mulTerm) {
		if(mulTerm.getTerm().struct == null || mulTerm.getFactor().struct == null) {
			return;
		}
		
		if (mulTerm.getTerm().struct.equals(mulTerm.getFactor().struct) && mulTerm.getTerm().struct == MyTab.intType) {
			mulTerm.struct = mulTerm.getTerm().struct;
			return;
		}
		mulTerm.struct = MyTab.noType;
		report_error("Error (line: " + mulTerm.getLine() + ") Svi operatori morajubiti int!", null);
	}

	public void visit(MinusStartingExpr minusExpr) {
		minusExpr.struct = minusExpr.getTerm().struct;
	}

	public void visit(TermExpr termExpr) {
		termExpr.struct = termExpr.getTerm().struct;
	}

	public void visit(AddSubExpr asExpr) {
		if (asExpr.getExpr().struct.equals(asExpr.getTerm().struct) && asExpr.getExpr().struct == MyTab.intType) {
			asExpr.struct = asExpr.getExpr().struct;
		} else {
			asExpr.struct = MyTab.noType;
			report_error("Error (line: " + asExpr.getLine() + ") Svi operatori morajubiti int!", null);
		}
	}

	// read i print:
	public void visit(ReadStmt readStmt) {

		int kind = readStmt.getDesignator().obj.getKind();
		Struct type = readStmt.getDesignator().obj.getType();

		if (kind != Obj.Var && kind != Obj.Elem) {
			report_error("Error (line: " + readStmt.getLine() + ") Moze se ucitati u promenjivu i elem niza", null);
			return;
		}

		if (type != MyTab.intType && type != MyTab.charType && type != MyTab.boolType)
			report_error("Error (line: " + readStmt.getLine()
					+ ") read prihvata samo izraze tipa int, char i bool kao argument", null);

	}

	public void visit(PrintStmt printStmt) {
		Struct expr = printStmt.getExpr().struct;
		if ((expr != MyTab.intType) && (expr != MyTab.charType) && (expr != MyTab.boolType))
			report_error("Error (line: " + printStmt.getLine()
					+ ") print prihvata samo izraze tipa int, char i bool kao argument", null);
	}

	public void visit(PrintStmtNumber printStmtNum) {
		Struct expr = printStmtNum.getExpr().struct;
		if (expr != MyTab.intType && expr != MyTab.charType && expr != MyTab.boolType)
			report_error("Error (line: " + printStmtNum.getLine()
					+ ") print prihvata samo izraze tipa int, char i bool kao argument", null);
	}

	// inc i dec:
	public void visit(DesignatorInc designatorInc) {

		int kind = designatorInc.getDesignator().obj.getKind();
		Struct type = designatorInc.getDesignator().obj.getType();

		if (type != MyTab.intType) {
			report_error(
					"Error (line: " + designatorInc.getLine() + ") Operand mora biti tipa int ili elem niza tipa int!",
					null);
			return;
		}

		if (kind != Obj.Var && kind != Obj.Elem)
			report_error("Error (line: " + designatorInc.getLine() + ") Operand mora biti var ili element niza!", null);

	}

	public void visit(DesignatorDec designatorDec) {

		int kind = designatorDec.getDesignator().obj.getKind();
		Struct type = designatorDec.getDesignator().obj.getType();

		if (type != MyTab.intType) {
			report_error(
					"Error (line: " + designatorDec.getLine() + ") Operand mora biti tipa int ili elem niza tipa int!",
					null);
			return;
		}

		if (kind != Obj.Var && kind != Obj.Elem)
			report_error("Error (line: " + designatorDec.getLine() + ") Operand mora biti var ili element niza!", null);

	}

	public void visit(FindAnyStmt findAnyStmt) {
		// Struct te = findAnyStmt.getExpr().struct;
		// Struct dest = findAnyStmt.getDesignator().obj.getType();
		// Struct op = findAnyStmt.getDesignator1().obj.getType();
		Obj te = MyTab.find(findAnyStmt.getDesignator().obj.getName());
		Obj dest = MyTab.find(findAnyStmt.getDesignator1().obj.getName());
		if (findAnyStmt.getDesignator().obj.getType() != MyTab.boolType) {
			report_error("Error (line: " + findAnyStmt.getLine() + ") Promenjiva mora biti bool tipa: "
					+ findAnyStmt.getDesignator().obj.getName(), null);
			return;
		}
		if (findAnyStmt.getDesignator1().obj.getType().getKind() != Struct.Array
				&& findAnyStmt.getDesignator1().obj != MyTab.noObj) {
			report_error("Error (line: " + findAnyStmt.getLine() + ") Promenjiva mora biti niz: "
					+ findAnyStmt.getDesignator1().obj.getName(), null);
			return;
		}
		if ((findAnyStmt.getExpr().struct.getKind() == Struct.Array
				|| !findAnyStmt.getExpr().struct.equals(findAnyStmt.getDesignator1().obj.getType().getElemType()))
				&& findAnyStmt.getDesignator1().obj != MyTab.noObj) {
			report_error(
					"Error (line: " + findAnyStmt.getLine()
							+ ") findAny only accepts expressions that are same type as array elements as operands ",
					null);
			return;
		}
	}

	public boolean passed() {
		return !errorDetected;
	}

	// pomicne f-je:

	public String getObjType(int kind) {
		if (kind == 0) {
			return "Con";
		} else if (kind == 1) {
			return "Var";
		} else if (kind == 2) {
			return "Type";
		} else if (kind == 3) {
			return "Meth";
		} else if (kind == 4) {
			return "Fld";
		} else if (kind == 5) {
			return "Elem";
		} else if (kind == 6) {
			return "Prog";
		}
		return "";
	}

	public String getStructType(int kind) {
		if (kind == 0) {
			return "none";
		} else if (kind == 1) {
			return "int";
		} else if (kind == 2) {
			return "char";
		} else if (kind == 3) {
			return "array";
		} else if (kind == 4) {
			return "class";
		} else if (kind == 5) {
			return "bool";
		}
		return "";
	}
	
}