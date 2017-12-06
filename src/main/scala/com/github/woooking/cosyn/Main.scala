package com.github.woooking.cosyn

import com.github.javaparser.JavaParser
import com.github.woooking.cosyn.ir.{IRBuiltVisitor, NoArg}

object Main {
    def main(args: Array[String]): Unit = {
        val cu = JavaParser.parse(
            """
              |public class MethodPrinter {
              |    public static void main(String[] args) throws Exception {
              |        // creates an input stream for the file to be parsed
              |        FileInputStream in = new FileInputStream("test.java");
              |
              |        // parse it
              |        CompilationUnit cu = JavaParser.parse(in);
              |
              |        // visit and print the methods names
              |        cu.accept(new MethodVisitor(), null);
              |    }
              |
              |    /**
              |     * Simple visitor implementation for visiting MethodDeclaration nodes.
              |     */
              |    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
              |        @Override
              |        public void visit(MethodDeclaration n, Void arg) {
              |            /* here you can access the attributes of the method.
              |             this method will be called for all methods in this
              |             CompilationUnit, including inner class methods */
              |            System.out.println(n.getName());
              |            super.visit(n, arg);
              |        }
              |    }
              |}
            """.stripMargin)

        cu.accept(new IRBuiltVisitor, NoArg)
    }
}
