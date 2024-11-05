# MikroJava-Compiler
# MicroJava Compiler Project

### Overview

This project involves implementing a compiler for MicroJava, a simplified language based on Java. The project consists of four main stages:

1. **Lexical Analysis**  
   Tokenize the source code, creating a sequence of tokens that represent the lexical elements of the language.

2. **Syntax Analysis**  
   Verify that the sequence of tokens adheres to the grammar of the language. This stage creates an abstract syntax tree (AST).

3. **Semantic Analysis**  
   Traverse the abstract syntax tree to build a symbol table, check type compatibility, and enforce context conditions.

4. **Code Generation**  
   Generate executable code by traversing the AST, producing code that can run on the MicroJava virtual machine.

---

### Tools and Libraries

1. **JFlex.Antt** – Generates the lexer.
2. **cup_v10k.jar** – Generates the parser.
3. **symboltable-1-1.jar** – Generates the abstract syntax tree.
4. **mj-runtime-1.1.jar** – For code generation and disassembly.

---

### build.xml Configuration

1. **lexerGen** – Generates the lexer.
2. **parserGen** – Generates the parser.
3. **compile** – Compiles the source code.
4. **runObj** – Runs the generated program.

---

### Tests

1. **program.mj** – A test program that executes correctly, covering all grammar rules.
2. **programLekErr.mj** – A test with lexical errors.
3. **programSinErr.mj** – A test containing syntax errors.
4. **programSemErr.mj** – A test with semantic errors.

---

### New Classes

All new classes are located in `src/rs/ac/bg/etf/pp1`:

1. **MyTab.java**  
   Extends `Tab.java`, adds a `Bool` type, and redefines the `dump` method.

2. **MyDumpSymbolTableVisitor.java**  
   Extends `DumpSymbolTableVisitor.java` and redefines the `visitStructNode` method for custom output.

3. **Compiler.java**  
   Responsible for executing all four stages of the compilation process.
