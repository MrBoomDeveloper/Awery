package com.mrboomdev.awery.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.sidesheet.SideSheetDialog
import com.mrboomdev.awery.app.App
import com.mrboomdev.awery.util.extensions.fixAndShow

abstract class BasePanelDialog {
    var dismissListener: (() -> Unit)? = null
    private var dialog: Dialog? = null

    fun show(context: Context) {
        val dialog = create(context)
        dialog.setContentView(getView(context))
        dialog.setOnDismissListener { dismissListener?.invoke() }
        dialog.fixAndShow()
    }

    abstract fun getView(context: Context): View

    open fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }

    private fun create(context: Context): Dialog {
        val dialog = if(App.isLandscape()) SideSheetDialog(context)
        else BottomSheetDialog(context)

        this.dialog = dialog
        return dialog
    }
}