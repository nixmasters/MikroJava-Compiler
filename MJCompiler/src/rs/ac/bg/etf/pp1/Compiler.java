package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;

public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}

	public static void tsdump() {
		MyTab.dump();
	}

	public static void main(String[] args) throws Exception {

		Logger log = Logger.getLogger(Compiler.class);

		Reader br = null;
		try {
			String inFile = args[0];
			String outFile = args[1];

			File sourceCode = new File(inFile);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());

			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);

			MJParser p = new MJParser(lexer);
			Symbol s = p.parse(); // pocetak parsiranja

			if (!p.errorDetected && !lexer.errorDetected) {
				Program prog = (Program) (s.value);
				MyTab.init();
				// ispis sintaksnog stabla
				log.info(prog.toString(""));
				log.info("===================================");

				// ispis prepoznatih programskih konstrukcija
				SemanticPass v = new SemanticPass();
				prog.traverseBottomUp(v);

				log.info("===================================");

				tsdump();

				if (v.passed()) {
					File objFile = new File(outFile);
					if (objFile.exists())
						objFile.delete();

					CodeGenerator codeGenerator = new CodeGenerator();
					prog.traverseBottomUp(codeGenerator);
					Code.dataSize = v.nVars;
					Code.mainPc = codeGenerator.getMainPc();
					Code.write(new FileOutputStream(objFile));
					log.info("Parsiranje uspesno zavrseno");
				} else {
					log.info("Greska u semantickoj analizi");
				}
			} else {
				if (lexer.errorDetected)
					log.error("Greska u leksickoj analizi!");
				else
					log.error("Greska u sintaksnoj analizi!");
			}
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e1) {
					log.error(e1.getMessage(), e1);
				}
		}

	}

}
