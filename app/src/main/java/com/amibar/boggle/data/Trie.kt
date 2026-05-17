package com.amibar.boggle.data

import java.util.concurrent.atomic.AtomicReferenceArray
import kotlin.concurrent.Volatile

/**
 * A wrapper for a Trie.
 */
open class Trie {
    internal open val root: TrieNode = createRootNode()

    internal open fun createRootNode(): TrieNode = TrieNode()

    /**
     * Base class for a node in the Trie.
     */
    internal open inner class TrieNode {
        /** Atomic array of pointers to child nodes, indexed by character ('a' to 'z'). */
        val children: AtomicReferenceArray<TrieNode?> = AtomicReferenceArray(ALPHABET_SIZE)

        /** Flag indicating if this node represents the end of a complete word. */
        @Volatile
        var isEndOfWord: Boolean = false

        /** Flag indicating if this node has no children. */
        @Volatile
        var isLeaf: Boolean = true
            internal set

        fun getChild(ch: Char): TrieNode? {
            val index = ch.code - 'a'.code
            if (index !in 0 until ALPHABET_SIZE) return null
            return children.get(index)
        }

        internal open fun createChild(): TrieNode = TrieNode()

        fun putChildIfAbsent(ch: Char): TrieNode? {
            val index = ch.code - 'a'.code
            if (index !in 0 until ALPHABET_SIZE) return null
            
            val existing = children.get(index)
            if (existing != null) return existing

            val newNode = createChild()
            if (children.compareAndSet(index, null, newNode)) {
                isLeaf = false
                return newNode
            }
            return children.get(index)
        }
    }

    /**
     * A cursor used for incremental traversal of the Trie.
     * This avoids exposing raw [TrieNode]s while allowing efficient DFS.
     */
    inner class Cursor internal constructor(private val node: TrieNode) {
        val isEndOfWord: Boolean get() = node.isEndOfWord
        val isLeaf: Boolean get() = node.isLeaf

        fun getChildCursor(ch: Char): Cursor? {
            val child = node.getChild(ch) ?: return null
            return Cursor(child)
        }
    }

    fun rootCursor(): Cursor = Cursor(root)

    open fun put(str: String) {
        var node = root
        for (ch in str) {
            if (ch !in 'a'..'z') continue
            node = node.putChildIfAbsent(ch) ?: continue
        }
        node.isEndOfWord = true
    }

    private fun getNode(str: String): TrieNode? {
        var node = root
        for (ch in str) {
            node = node.getChild(ch) ?: return null
        }
        return node
    }

    fun contains(str: String): Boolean {
        return getNode(str)?.isEndOfWord ?: false
    }

    val words: Set<String>
        get() = buildSet {
            collectWords(root, StringBuilder(), this)
        }

    private fun collectWords(node: TrieNode, prefix: StringBuilder, result: MutableSet<String>) {
        if (node.isEndOfWord) {
            result.add(prefix.toString())
        }
        if (node.isLeaf) return

        for (i in 0 until ALPHABET_SIZE) {
            val child = node.children.get(i) ?: continue
            prefix.append(('a'.code + i).toChar())
            collectWords(child, prefix, result)
            prefix.setLength(prefix.length - 1)
        }
    }

    val size: Int
        get() = calculateSize(root)

    private fun calculateSize(node: TrieNode): Int {
        var count = if (node.isEndOfWord) 1 else 0
        if (node.isLeaf) return count
        for (i in 0 until ALPHABET_SIZE) {
            val child = node.children.get(i) ?: continue
            count += calculateSize(child)
        }
        return count
    }

    override fun toString(): String = words.toString()

    companion object {
        const val ALPHABET_SIZE = 26
    }
}
