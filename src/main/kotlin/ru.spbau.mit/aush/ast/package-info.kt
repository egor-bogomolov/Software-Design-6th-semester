package ru.spbau.mit.aush.ast

/**
 * Contains abstract class ASTNode representing AST node
 * and its subclasses which define shell features, other than command running:
 * - CommandNode evaluates command
 * - PipeNode evaluates two commands with output of the first one redirected to input of second
 * - DefineVariableNode used for environment variable definition
 * - EmptyNode evaluates empty command
*/