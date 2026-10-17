@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class)

package mandagram.examples

import kotlinx.cinterop.*
import kotlin.native.CName
import mandagram.c.PluginApi
import mandagram.c.PluginMetadata

private fun persistentCString(value: String): CPointer<ByteVar> {
    val bytes = value.encodeToByteArray()
    val buffer = nativeHeap.allocArray<ByteVar>(bytes.size + 1)
    for (index in bytes.indices) buffer[index] = bytes[index]
    buffer[bytes.size] = 0
    return buffer
}

// The C strings and struct must outlive the exported function's stack frame.
// There is no shutdown entry point in this ABI; these allocations are not freed.
private val metadata: CPointer<PluginMetadata> = nativeHeap.alloc<PluginMetadata> {
    name = persistentCString("Plain Text Kotlin")
    version = persistentCString("1.0.0")
    author = persistentCString("MandaGram Community")
    description = persistentCString("Removes the .plain prefix from outgoing text")
}.ptr

@CName(externName = "mandagram_plugin_metadata")
fun pluginMetadata(): CPointer<PluginMetadata> = metadata

@Suppress("UNUSED_PARAMETER")
private fun beforeSend(chatId: Long, text: CPointer<ByteVar>?, userData: ULong): CPointer<ByteVar>? {
    // Short-circuit comparisons stop at the first mismatch, including a NUL.
    // Do not decode/copy the string: the host copies the borrowed substring.
    if (text != null &&
        text[0] == '.'.code.toByte() &&
        text[1] == 'p'.code.toByte() &&
        text[2] == 'l'.code.toByte() &&
        text[3] == 'a'.code.toByte() &&
        text[4] == 'i'.code.toByte() &&
        text[5] == 'n'.code.toByte() &&
        text[6] == ' '.code.toByte()
    ) {
        return text + 7
    }
    return text
}

@CName(externName = "mandagram_plugin_init")
fun pluginInit(api: CPointer<PluginApi>?): Boolean {
    val register = api?.pointed?.register_message_send_hook ?: return false
    return register(staticCFunction(::beforeSend), null, 0uL) == 0
}
