package com.amibar.boggle.data

/**
 * A specialized Trie that uses [PathTrieNode]s to store word paths.
 * Optimized to use [PathTrieNode] only for nodes that represent the end of a word,
 * saving memory for intermediate prefix nodes.
 */
class PathTrie : Trie() {
    override val root: TrieNode = createRootNode()

    override fun createRootNode(): TrieNode = PathTrieNode()

    /**
     * A specialized TrieNode that can store a path string.
     */
    private inner class PathTrieNode : TrieNode() {
        var path: String? = null
        // Note: We do NOT override createChild() here.
        // This ensures that new children created by putChildIfAbsent() are base TrieNodes.
    }

    override fun put(str: String) {
        put(str, null)
    }

    /**
     * Inserts a word and its associated path into the Trie.
     * Ensures that the terminal node is a [PathTrieNode].
     */
    @Synchronized
    fun put(str: String, path: String?) {
        var node = root
        var parent: TrieNode? = null
        var lastCh = ' '
        for (ch in str) {
            if (ch !in 'a'..'z') continue
            parent = node
            lastCh = ch
            node = node.putChildIfAbsent(ch) ?: continue
        }
        node.isEndOfWord = true
        if (node !is PathTrieNode) {
            // Replace the base TrieNode with a PathTrieNode to store the path.
            val newNode = PathTrieNode()
            newNode.isEndOfWord = true
            newNode.isLeaf = node.isLeaf
            newNode.path = path
            // Copy existing children to the new node.
            for (i in 0 until ALPHABET_SIZE) {
                val child = node.children.get(i)
                if (child != null) {
                    newNode.children.set(i, child)
                }
            }
            // Update the parent's reference to this node.
            parent?.children?.set(lastCh - 'a', newNode)
        } else if (path != null) {
            node.path = path
        }
    }

    /**
     * Retrieves the path associated with a word.
     */
    fun getPath(str: String): String? {
        var node = root
        for (ch in str) {
            node = node.getChild(ch) ?: return null
        }
        return (node as? PathTrieNode)?.path
    }

    /**
     * Converts the Trie into a Map where keys are words and values are their associated paths.
     */
    fun toMap(): HashMap<String, String> {
        val map = HashMap<String, String>()
        for (s in words) {
            map[s] = getPath(s) ?: ""
        }
        return map
    }
}
