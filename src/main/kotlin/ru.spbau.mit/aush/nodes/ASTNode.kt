package ru.spbau.mit.aush.nodes

import java.io.InputStream
import java.io.OutputStream

abstract class ASTNode {
    abstract fun evaluate(
            input : InputStream = System.`in`,
            output : OutputStream = System.out,
            error : OutputStream = System.err
    )
}