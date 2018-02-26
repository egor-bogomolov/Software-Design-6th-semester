package ru.spbau.mit.aush

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.parser.Parser

object AUshGrammar : Grammar<ASTNode>() {
    private val ASSIGN by token("=")
    private val PIPE by token("\\|")

    private val WS by token("[\\s\\r\\n]+", ignore = true)
    private val RAW_STRING by token("'.*?\''")
    private val STRING by token("\".*?\"")
    private val IDENTIFIER by token("\\w+")

    private val simple_token: Parser<TokenString> =
            (RAW_STRING or STRING or token("[^\"\'\\s\\r\\n]+")).map {
                when (it.type) {
                    RAW_STRING -> TokenString(
                            it.text.removeSurrounding("'"),
                            TokenString.TokenType.RAW
                    )
                    STRING -> TokenString(
                            it.text.removeSurrounding("\""),
                            TokenString.TokenType.RAW
                    )
                    else -> TokenString(it.text, TokenString.TokenType.PLAIN)
                }
            }

    private val variableDefinition: Parser<Pair<String, TokenString>> =
            (skip(WS) and IDENTIFIER and ASSIGN and simple_token).map {
                (identifier, _, value) -> identifier.text to value
            }

    private val singleCommand: Parser<ASTNode> =
            (zeroOrMore(variableDefinition) and skip(WS)
                    and simple_token and zeroOrMore(skip(WS) and simple_token)).map {
                (environment, commandName, arguments) ->
                environment.foldRight(
                        CommandNode(
                                commandName,
                                arguments
                        ) as ASTNode,
                        { nameValuePair, node -> DefineLocalVariableNode(nameValuePair, node) }
                )
            }

    private val pipedCommands: Parser<ASTNode> =
            (zeroOrMore(singleCommand and skip(WS) and skip(PIPE) and skip(WS)) and singleCommand).map {
                (commands, lastCommand) ->
                commands.foldRight(
                        lastCommand,
                        { command, tail -> PipeNode(command, tail) }
                )
            }

    private val defineGlobalVariablesCommand: Parser<ASTNode> =
            (zeroOrMore(variableDefinition)).map {
                it.foldRight(
                        EmptyNode as ASTNode,
                        { nameValuePair, node -> DefineGlobalVariableNode(nameValuePair, node) }
                )
            }

    override val rootParser: Parser<ASTNode>
        get() = pipedCommands or defineGlobalVariablesCommand
}