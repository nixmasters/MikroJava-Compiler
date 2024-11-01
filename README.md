# MikroJava-Compiler
Input micro java source code: input.mj

Simple and functional compiler for micro java. The compiler works in 4 phases.

The first phase is Lexical Analysis, which parses the input.mj source file and outputs lexical tokens used in the input.mj file. Lexical tokens are given in mjlexer.flex file. Lexical analysis uses JFlex library.
The second phase is Syntax Analysis, which performs syntax analysis based on the grammar provided in the mjparser.cup file. It takes lexical tokens obtained in the first phase as input and produces an object corresponding to the Program's parse tree from the .cup file as output. Syntax analysis uses cup library, which is implemented with LALR parser generator.
The third phase is Semantic Analysis, which is responsible for Semantic Analysys, inserting symbols into symbol table and detecting semantic errors. The symbol table is implemented using the school library SymbolTable. The implementation of the third phase is provided in the SemanticAnalyzer.java file.
The fourth phase is Code Generation. This phase generates microjava bytecode. The implementation of the fourth phase is provided in the CodeGenerator.java file. Code generator uses utility class Code.
