package com.mrboomdev.awery.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.sidesheet.SideSheetDialog
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.util.extensions.fix
import java.lang.ref.WeakReference

abstract class BasePanelDialog : DialogInterface {
    private var mContext: WeakReference<Context>? = null
    private var isCreating = false
    var dismissListener: (() -> Unit)? = null
    var dialog: Dialog? = null

    val context: Context?
        get() = mContext?.get()

    val isShowing: Boolean
        get() = isCreating || dialog?.isShowing ?: false

    fun show(context: Context) {
        isCreating = true
        mContext = WeakReference(context)

        create(context).apply {
            setContentView(getView(context))
            setOnDismissListener { dismissListener?.invoke() }
            show()
        }

        isCreating = false
    }

    abstract fun getView(context: Context): View

    override fun cancel() {
        dialog?.cancel()
        dialog = null
        mContext = null
    }

    override fun dismiss() {
        dialog?.dismiss()
        dialog = null
        mContext = null
    }

    private fun create(context: Context): Dialog {
        val dialog = if(App.isLandscape()) object : SideSheetDialog(context) {
            override fun onStart() {
                super.onStart()
                fix()
            }

            override fun onDetachedFromWindow() {
                super.onDetachedFromWindow()
                dialog = null
            }
        } else object : BottomSheetDialog(context) {
            override fun onStart() {
                super.onStart()
                fix()
            }

            override fun onDetachedFromWindow() {
                super.onDetachedFromWindow()
                dialog = null
            }
        }

        this.dialog = dialog
        return dialog
    }
}