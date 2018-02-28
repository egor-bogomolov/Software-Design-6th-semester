package ru.spbau.mit.aush

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRepl {
    @Test
    fun simpleTest() {
        val inputString =
                """
                    |echo ${"$"}a
                    |a=2 echo ${"$"}a
                    |echo ${"$"}a
                    |a=3
                    |echo ${"$"}a
                    |echo "multi line
                    |string"
                    |echo "some string" | cat
                    |exit
                    |echo not     exited
                """.trimMargin()

        val expectedOutput =
                """
                    |Welcome to AUsh (Academic University SHell)
                    |>${" "}
                    |> 2
                    |>${" "}
                    |> > 3
                    |> > multi line
                    |string
                    |> some string
                    |>${" "}
                """.trimMargin()
        val expectedErrorOutput = ""

        val environmentIO = EnvironmentIO(
                inputString.byteInputStream(),
                ByteOutputStream(),
                ByteOutputStream()
        )

        Repl(environmentIO).run()

        assertEquals(
                expectedOutput, environmentIO.output.toString()
        )
        assertEquals(
                expectedErrorOutput, environmentIO.error.toString()
        )
    }
}