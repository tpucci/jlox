package tools

import java.io.IOException
import java.io.PrintWriter
import java.util.*
import kotlin.system.exitProcess


object GenerateAst {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Usage: generate_ast <output directory>")
            exitProcess(64)
        }
        val outputDir = args[0]
        defineAst(
            outputDir, "Expr", listOf(
                "Binary   > left: Expr, operator: Token, right: Expr",
                "Grouping > expression: Expr",
                "Literal  > value: Any",
                "Unary    > operator: Token, right: Expr"
            )
        )
    }

    @Throws(IOException::class)
    private fun defineAst(
        outputDir: String, baseName: String, types: List<String>
    ) {
        val path = "$outputDir/$baseName.kt"
        val writer = PrintWriter(path, "UTF-8")
        writer.println("package ast")
        writer.println()
        writer.println("import Token")
        writer.println()
        writer.println("abstract class $baseName {")

        defineVisitor(writer, baseName, types)

        // The base accept() method.
        writer.println()
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")
        writer.println()

        // The AST classes.
        for (type in types) {
            val className = type.split(">").toTypedArray()[0].trim { it <= ' ' }
            val fields = type.split(">").toTypedArray()[1].trim { it <= ' ' }
            defineType(writer, baseName, className, fields)
        }

        writer.println("}")

        writer.close()
    }

    private fun defineType(
        writer: PrintWriter, baseName: String,
        className: String, fieldList: String
    ) {
        val fields = fieldList.split(",").joinToString(", ") { "val ${it.trim()}" }

        // Constructor.
        writer.println("    class $className($fields) : $baseName() {")

        // Visitor pattern.
        writer.println("        override fun <R> accept(visitor: Visitor<R>): R {")
        writer.println(
            "            return visitor.visit" +
                    className + baseName + "(this)"
        )
        writer.println("        }")

        writer.println("    }")
        writer.println()
    }

    private fun defineVisitor(
        writer: PrintWriter, baseName: String, types: List<String>
    ) {
        writer.println("    interface Visitor<R> {")
        for (type: String in types) {
            val typeName = type.split(">").toTypedArray()[0].trim { it <= ' ' }
            writer.println(
                "        fun visit" + typeName + baseName + "(" +
                        baseName.lowercase(Locale.getDefault()) + ": " + typeName + "): R"
            )
        }
        writer.println("    }")
    }
}