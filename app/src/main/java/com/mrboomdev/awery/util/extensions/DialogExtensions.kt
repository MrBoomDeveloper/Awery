package com.mrboomdev.awery.util.extensions

import android.app.Dialog
import android.graphics.Color
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.sidesheet.SideSheetDialog
import com.google.android.material.R as MaterialR
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.getConfiguration
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.ThemeManager

private const val MAX_SIDE_WIDTH = 400f
private const val MAX_WIDTH = 450f

fun Dialog.fixAndShow() {
    show()
    fix()
}

/**
 * A hacky method to fix the height, width of the dialog and it's colors.
 * This method have to be called only after dialog has been shown.
 * @author MrBoomDev
 */
fun Dialog.fix() {
    val window = window ?: throw IllegalStateException("You can't invoke this method before dialog is being shown!")

    if(this is BottomSheetDialog) {
        behavior.peekHeight = 1000
        window.navigationBarColor = SurfaceColors.SURFACE_1.getColor(context)
    }

    if(this is SideSheetDialog) {
        val sheet = window.findViewById<View>(MaterialR.id.m3_side_sheet)
        sheet.useLayoutParams<LayoutParams> { it.width = context.dpPx(MAX_SIDE_WIDTH) }
        window.navigationBarColor = SurfaceColors.SURFACE_1.getColor(context)
    } else {
        /* If we'll try to do this shit with the SideSheetDialog, it will get centered,
			   so we use different approaches for different dialog types.*/

        if(getConfiguration().screenWidthDp > MAX_WIDTH) {
            window.setLayout(context.dpPx(MAX_WIDTH), MATCH_PARENT)
        }
    }
}