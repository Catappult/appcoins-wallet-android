package com.appcoins.wallet.convention.transforms

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * ASM bytecode transform that restores the null-check in
 * [androidx.core.graphics.PathParser.deepCopyNodes].
 *
 * Background: VK SDK `rich-vector` library's `PathElement` copy constructor calls
 * `PathParser.deepCopyNodes(null)` when `pathDataNodes` has not yet been initialized
 * (null is the intended sentinel for "path not yet converted to nodes"). In
 * `androidx.core 1.9.0` this was safe because `deepCopyNodes` returned null for
 * null input. The null check was removed in `1.13.0`, causing a crash:
 *
 *   NullPointerException: Attempt to get length of null array
 *     at androidx.core.graphics.PathParser.deepCopyNodes(PathParser.java:125)
 *     at com.vk.utils.vectordrawable.internal.element.PathElement.<init>(SourceFile:96)
 *     at com.vk.superapp.vkpay.checkout.bottomsheet.VkPayCheckoutBottomSheet.onViewCreated
 *
 * This transform inserts `if (source == null) return null;` at the start of
 * `deepCopyNodes`, matching the behavior of `androidx.core 1.9.0`.
 */
abstract class PathParserNullFixFactory :
  AsmClassVisitorFactory<InstrumentationParameters.None> {

  override fun createClassVisitor(
    classContext: ClassContext,
    nextClassVisitor: ClassVisitor,
  ): ClassVisitor = PathParserClassVisitor(nextClassVisitor)

  override fun isInstrumentable(classData: ClassData): Boolean =
    classData.className == "androidx.core.graphics.PathParser"
}

private class PathParserClassVisitor(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM9, cv) {

  override fun visitMethod(
    access: Int,
    name: String,
    descriptor: String,
    signature: String?,
    exceptions: Array<out String>?,
  ): MethodVisitor {
    val next = super.visitMethod(access, name, descriptor, signature, exceptions)
    return if (name == DEEP_COPY_NODES && descriptor == DEEP_COPY_NODES_DESC) {
      NullGuardMethodVisitor(next)
    } else {
      next
    }
  }

  private companion object {
    const val DEEP_COPY_NODES = "deepCopyNodes"
    const val DEEP_COPY_NODES_DESC =
      "([Landroidx/core/graphics/PathParser\$PathDataNode;)" +
        "[Landroidx/core/graphics/PathParser\$PathDataNode;"
  }
}

/**
 * Prepends the equivalent of `if (source == null) return null;` to the method body,
 * restoring the null-safe contract that existed in androidx.core 1.9.0.
 */
private class NullGuardMethodVisitor(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM9, mv) {

  override fun visitCode() {
    super.visitCode()
    val nonNullLabel = Label()
    mv.visitVarInsn(Opcodes.ALOAD, 0)              // load 'source' param
    mv.visitJumpInsn(Opcodes.IFNONNULL, nonNullLabel) // if non-null, skip
    mv.visitInsn(Opcodes.ACONST_NULL)              // push null
    mv.visitInsn(Opcodes.ARETURN)                  // return null
    mv.visitLabel(nonNullLabel)                    // normal execution path
  }
}
