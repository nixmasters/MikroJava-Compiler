package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;
import org.apache.log4j.Logger;

public class CodeGenerator extends VisitorAdaptor {
	private int mainPc;
	Logger log = Logger.getLogger(getClass());

	public int getMainPc() {
		return mainPc;
	}

	// constants:
	public void visit(NumFactor numFact) {
		
		Obj con = new Obj(Obj.Con, "numCnst", MyTab.intType, numFact.getNumCnst().intValue(), 0);
		
		Code.load(con);
	}

	public void visit(CharFactor charFact) {
		
		Obj con = new Obj(Obj.Con, "charCnst", MyTab.charType, charFact.getCharCnst().charValue(), 0);
		
		Code.load(con);
	}

	public void visit(BoolFactor boolFact) {
		
		Obj con = new Obj(Obj.Con, "boolCnst", MyTab.boolType, boolFact.getBoolCnst().compareTo(false), 0);
		
		Code.load(con);
	}

	// f-je:
	public void visit(MethodTypeName methodTypeName) {
		
		if (methodTypeName.getMethName().equalsIgnoreCase("main")) {
			this.mainPc = Code.pc;
		}

		Obj methObj = methodTypeName.obj;
		methObj.setAdr(Code.pc);

		int numOfArg = methObj.getLevel();
		int nummOfLocals = methObj.getLocalSymbols().size();

		Code.put(Code.enter);
		Code.put(numOfArg);
		Code.put(numOfArg + nummOfLocals);
	}

	public void visit(MethodDecll methodDecl) {
		
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	// inc i dec:
	public void visit(DesignatorInc desigInc) {
		
		Obj desigObj = desigInc.getDesignator().obj;
		Struct dType = desigObj.getType();

		if (desigInc.getDesignator() instanceof DesignatorArray) {
			Code.put(Code.dup2);
			if (dType == MyTab.charType)
				Code.put(Code.baload);
			else
				Code.put(Code.aload);
		}

		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(desigObj);
	}

	public void visit(DesignatorDec desigDec) {
		
		Obj desigObj = desigDec.getDesignator().obj;
		Struct dType = desigObj.getType();

		if (desigDec.getDesignator() instanceof DesignatorArray) {
			Code.put(Code.dup2);
			if (dType == MyTab.charType)
				Code.put(Code.baload);
			else
				Code.put(Code.aload);
		}

		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(desigObj);
	}

	// print i read:
	public void visit(PrintStmt printStmt) {
		
		Struct p = printStmt.getExpr().struct;
		
		if (p == MyTab.intType || p == MyTab.boolType) {
			Code.loadConst(5);
			Code.put(Code.print);
		} else {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}

	public void visit(PrintStmtNumber printStmtNum) {

		int val = printStmtNum.getNumCnst().intValue();

		if (printStmtNum.getExpr().struct == MyTab.charType) {
			Code.loadConst(val);
			Code.put(Code.bprint);
		} else {
			Code.loadConst(val);
			Code.put(Code.print);
		}
	}

	public void visit(ReadStmt readStmt) {
		
		Obj readObj = readStmt.getDesignator().obj;
		
		if (readObj.getType() == MyTab.charType)
			Code.put(Code.bread);
		else
			Code.put(Code.read);

		Code.store(readObj);
	}

	// akcije za konstrukciju niza
	public void visit(ConstructorFact conFact) {
		
		int b;
		if (conFact.getType().struct != MyTab.charType)
			b = 1;
		else
			b = 0;

		Code.put(Code.newarray);
		Code.loadConst(b);
	}

	// designator:
	public void visit(DesignatorAssignment desigAssign) {
		Code.store(desigAssign.getDesignator().obj);
	}

	public void visit(DesignatorNoArray designatorNoArray) {
		if (designatorNoArray.getParent().getClass() != ReadStmt.class && designatorNoArray.getParent().getClass() != DesignatorAssignment.class)
			Code.load(designatorNoArray.obj);
	}

	
	public void visit(DesignatorArray da) {
	}

	public void visit(ArrayName arrayName) {
		// adr niza na stek
		Code.load(arrayName.obj); 
	}

	public void visit(DesigFactor desigFact) {
		
		// ako je prom elem niza, ucitaj na stek:
		if (desigFact.getDesignator() instanceof DesignatorArray) {
			if (desigFact.getDesignator().obj.getType() != MyTab.charType) {
				Code.put(Code.aload);
			} else {
				Code.put(Code.baload);
			}
		}
	}

	public void visit(FindAnyStmt findAnyStmt) {
		
		Obj d = findAnyStmt.getDesignator().obj;
		Obj d1 = findAnyStmt.getDesignator1().obj;

		Obj niz = new Obj(Obj.Var, "numVar", MyTab.intType, 0, 1);
		Obj val = new Obj(Obj.Var, "numVar", MyTab.intType, 1, 1);
		Obj tek = new Obj(Obj.Var, "numVar", MyTab.intType, 2, 1);

		int adr1, adr2, adr3;
		
		// save context:
		Code.load(niz);
		Code.put(Code.dup_x2);
		Code.put(Code.pop);

		Code.load(val);
		Code.put(Code.dup_x2);
		Code.put(Code.pop);

		Code.load(tek);
		Code.put(Code.dup_x2);
		Code.put(Code.pop);

		// set inicial values:
		Code.store(val);
		Code.store(niz);
		Code.loadConst(0);
		Code.store(tek);

		// povratna vrednost
		Code.loadConst(0);

		// perform operation
		adr1 = Code.pc;
		Code.load(niz);
		Code.put(Code.arraylength);
		Code.load(tek);
		// jump to exit if equal
		Code.put(Code.jcc + Code.eq);
		Code.put2(0);
		adr3 = Code.pc - 2;

		Code.load(niz);
		Code.load(tek);
		if (d1.getType().getElemType() != MyTab.charType)
			Code.put(Code.aload);
		else
			Code.put(Code.baload);
		Code.load(val);
		// ifequal jump to set res:
		Code.put(Code.jcc + Code.eq);
		Code.put2(0);
		adr2 = Code.pc - 2;
		
		// increment tek
		Code.load(tek);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(tek);
		Code.putJump(adr1);

		Code.fixup(adr2);

		Code.put(Code.pop);
		Code.loadConst(1);

		Code.fixup(adr3);
		
		// obnovi konteks:
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.store(tek);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.store(val);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.store(niz);

		// upisi rezultat u d
		Code.store(d);
	}

	// expressions:
	public void visit(AddSubExpr addSubExpr) {
		
		if (addSubExpr.getAddop() instanceof Plus)
			Code.put(Code.add);
		if (addSubExpr.getAddop() instanceof Minus)
			Code.put(Code.sub);
		
	}

	public void visit(MinusStartingExpr minusTermExpr) {
		Code.put(Code.neg);
	}

	public void visit(MulopTerm mulTerm) {
		
		if (mulTerm.getMulop() instanceof Div)
			Code.put(Code.div);
		if (mulTerm.getMulop() instanceof Mul)
			Code.put(Code.mul);
		if (mulTerm.getMulop() instanceof Mod)
			Code.put(Code.rem);
		
	}
}
