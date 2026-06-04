package com.rpgle.plugin.scan

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.rpgle.plugin.data.RpgWords
import com.rpgle.plugin.psi.RpgTokenTypes

/**
 * Extracts declared symbols and /COPY targets from a free-format RPG file by walking
 * the flat token stream, recovering each symbol's detail for hover documentation.
 * Results are cached per file and invalidated on any PSI change.
 */
object RpgSymbolScanner {

    private val KEYWORD_KIND: Map<String, RpgSymbol.Kind> = mapOf(
        "DCL-PROC" to RpgSymbol.Kind.PROCEDURE,
        "DCL-PR" to RpgSymbol.Kind.PROTOTYPE,
        "DCL-F" to RpgSymbol.Kind.FILE,
        "DCL-S" to RpgSymbol.Kind.VARIABLE,
        "DCL-C" to RpgSymbol.Kind.CONSTANT,
        "DCL-DS" to RpgSymbol.Kind.DATA_STRUCTURE,
        "BEGSR" to RpgSymbol.Kind.SUBROUTINE,
    )

    private val BLOCK_STOP: Set<String> = setOf(
        "DCL-PROC", "DCL-PR", "DCL-PI", "DCL-DS", "DCL-S", "DCL-C", "DCL-F", "BEGSR",
        "END-PROC", "END-PR", "END-PI", "END-DS",
    )

    fun scan(file: PsiFile): RpgScanResult =
        CachedValuesManager.getCachedValue(file) {
            CachedValueProvider.Result.create(doScan(file), file)
        }

    private fun doScan(file: PsiFile): RpgScanResult {
        val symbols = ArrayList<RpgSymbol>()
        val includes = ArrayList<String>()
        val text = file.text

        val nodes = file.node.getChildren(null).filter {
            it.elementType != TokenType.WHITE_SPACE && it.elementType != RpgTokenTypes.COMMENT
        }

        for (i in nodes.indices) {
            val node = nodes[i]
            when (node.elementType) {
                RpgTokenTypes.IDENTIFIER -> {
                    val kind = KEYWORD_KIND[node.text.uppercase()] ?: continue
                    val next = nodes.getOrNull(i + 1) ?: continue
                    if (next.elementType != RpgTokenTypes.IDENTIFIER) continue
                    val name = next.text
                    if (name.uppercase() in RpgWords.DECL_KEYWORDS) continue
                    symbols.add(buildSymbol(kind, name, nodes, i, text, file.name))
                }

                RpgTokenTypes.DIRECTIVE -> {
                    val dir = node.text.uppercase()
                    if (dir == "/COPY" || dir == "/INCLUDE") {
                        val start = node.startOffset + node.textLength
                        val nl = text.indexOf('\n', start)
                        val end = if (nl < 0) text.length else nl
                        val target = text.substring(start.coerceAtMost(text.length), end).trim()
                        if (target.isNotEmpty()) includes.add(target)
                    }
                }
            }
        }
        return RpgScanResult(symbols, includes)
    }

    /** Builds a symbol for the declaration whose keyword is at [keywordIndex] and whose name is the following token. */
    private fun buildSymbol(
        kind: RpgSymbol.Kind,
        name: String,
        nodes: List<ASTNode>,
        keywordIndex: Int,
        text: String,
        fileName: String,
    ): RpgSymbol {
        val afterName = keywordIndex + 2
        return when (kind) {
            RpgSymbol.Kind.VARIABLE ->
                RpgSymbol(name, kind, fileName, typeText = extractType(nodes, afterName, text))

            RpgSymbol.Kind.CONSTANT ->
                RpgSymbol(name, kind, fileName, value = extractConstantValue(nodes, afterName, text))

            RpgSymbol.Kind.PROTOTYPE -> {
                val returnType = extractType(nodes, afterName, text)
                val headerEnd = indexOfSemicolon(nodes, afterName)
                val params = parseParameters(nodes, headerEnd + 1, text, "END-PR")
                RpgSymbol(name, kind, fileName, typeText = returnType, parameters = params)
            }

            RpgSymbol.Kind.PROCEDURE -> {
                val pi = findProcInterface(nodes, afterName, text)
                RpgSymbol(name, kind, fileName, typeText = pi?.returnType, parameters = pi?.params)
            }

            else -> RpgSymbol(name, kind, fileName)
        }
    }

    /**
     * Extracts a data type starting at [from] — the type keyword plus its parenthesized
     * arguments, verbatim — or null when [from] is not a recognized data type keyword.
     */
    private fun extractType(nodes: List<ASTNode>, from: Int, text: String): String? {
        val first = nodes.getOrNull(from) ?: return null
        if (first.elementType != RpgTokenTypes.IDENTIFIER) return null
        if (first.text.uppercase() !in RpgWords.DATA_TYPES) return null
        val end = nodes[consumeParens(nodes, from)]
        return text.substring(first.startOffset, end.startOffset + end.textLength)
    }

    /**
     * The value of a `DCL-C`: the inner text of a `CONST(...)` wrapper if present,
     * otherwise everything between the name and the terminating `;`.
     */
    private fun extractConstantValue(nodes: List<ASTNode>, from: Int, text: String): String? {
        val first = nodes.getOrNull(from) ?: return null
        if (first.elementType == RpgTokenTypes.IDENTIFIER && first.text.equals("CONST", ignoreCase = true)) {
            val open = nodes.getOrNull(from + 1)
            if (open?.elementType == RpgTokenTypes.LPAREN) {
                val close = nodes[consumeParens(nodes, from)]
                val inner = text.substring(open.startOffset + open.textLength, close.startOffset).trim()
                return inner.ifEmpty { null }
            }
        }
        val semi = indexOfSemicolon(nodes, from)
        if (semi <= from) return null
        val last = nodes[semi - 1]
        return text.substring(first.startOffset, last.startOffset + last.textLength).trim().ifEmpty { null }
    }

    private data class ProcInterface(val returnType: String?, val params: List<RpgParameter>)

    /**
     * Finds a procedure's `DCL-PI ... END-PI` interface, scanning forward from the
     * procedure name; returns null at `END-PROC` or the next `DCL-PROC`.
     */
    private fun findProcInterface(nodes: List<ASTNode>, from: Int, text: String): ProcInterface? {
        var j = from
        while (j < nodes.size) {
            val tok = nodes[j]
            if (tok.elementType == RpgTokenTypes.IDENTIFIER) {
                when (tok.text.uppercase()) {
                    "DCL-PI" -> return parseProcInterface(nodes, j, text)
                    "END-PROC", "DCL-PROC" -> return null
                }
            }
            j++
        }
        return null
    }

    /** Parses a `DCL-PI <name|*n> [returnType]; <params...> END-PI`. */
    private fun parseProcInterface(nodes: List<ASTNode>, piIndex: Int, text: String): ProcInterface {
        var k = piIndex + 1
        val n0 = nodes.getOrNull(k)
        if (n0?.elementType == RpgTokenTypes.OPERATOR && n0.text == "*") {
            k += 2
        } else if (n0?.elementType == RpgTokenTypes.IDENTIFIER) {
            k += 1
        }
        val returnType = extractType(nodes, k, text)
        val headerEnd = indexOfSemicolon(nodes, k)
        val params = parseParameters(nodes, headerEnd + 1, text, "END-PI")
        return ProcInterface(returnType, params)
    }

    /**
     * Reads parameter declarations starting at [from] until [endKeyword]
     * (`END-PR` / `END-PI`). Each parameter is `[DCL-PARM] name type [keywords];`.
     */
    private fun parseParameters(
        nodes: List<ASTNode>,
        from: Int,
        text: String,
        endKeyword: String,
    ): List<RpgParameter> {
        val params = ArrayList<RpgParameter>()
        var j = from
        while (j < nodes.size) {
            val tok = nodes[j]
            if (tok.elementType != RpgTokenTypes.IDENTIFIER) {
                j++
                continue
            }
            val upper = tok.text.uppercase()
            if (upper == endKeyword) break
            if (upper in BLOCK_STOP) break

            val explicitParm = upper == "DCL-PARM"
            val nameIndex = if (explicitParm) j + 1 else j
            val nameNode = nodes.getOrNull(nameIndex)
            if (nameNode?.elementType != RpgTokenTypes.IDENTIFIER) break
            val type = extractType(nodes, nameIndex + 1, text)
            if (type == null && !explicitParm) break
            params.add(RpgParameter(nameNode.text, type))
            j = indexOfSemicolon(nodes, nameIndex) + 1
        }
        return params
    }

    /**
     * Index of the matching `)` for a type/keyword whose `(` is at [openOwner] + 1,
     * or [openOwner] itself when no parenthesized argument list follows.
     */
    private fun consumeParens(nodes: List<ASTNode>, openOwner: Int): Int {
        val open = nodes.getOrNull(openOwner + 1) ?: return openOwner
        if (open.elementType != RpgTokenTypes.LPAREN) return openOwner
        var depth = 0
        var j = openOwner + 1
        while (j < nodes.size) {
            when (nodes[j].elementType) {
                RpgTokenTypes.LPAREN -> depth++
                RpgTokenTypes.RPAREN -> {
                    depth--
                    if (depth == 0) return j
                }
            }
            j++
        }
        return nodes.size - 1
    }

    /** Index of the next `;` at or after [from], or [nodes].size when none. */
    private fun indexOfSemicolon(nodes: List<ASTNode>, from: Int): Int {
        var j = from
        while (j < nodes.size) {
            if (nodes[j].elementType == RpgTokenTypes.SEMICOLON) return j
            j++
        }
        return nodes.size
    }
}