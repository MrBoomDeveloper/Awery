package com.mrboomdev.awery.ui.dialogs

import android.content.Context
import android.util.Log
import android.view.Gravity.CENTER_VERTICAL
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.checkbox.MaterialCheckBox
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.App.Companion.showLoadingWindow
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.data.Constants.HIDDEN_LISTS
import com.mrboomdev.awery.app.data.db.item.DBCatalogList
import com.mrboomdev.awery.app.data.db.item.DBCatalogMedia
import com.mrboomdev.awery.databinding.PopupMediaBookmarkBinding
import com.mrboomdev.awery.extensions.data.CatalogList
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.extensions.data.CatalogMediaProgress
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.inflater
import com.mrboomdev.awery.util.extensions.setImageTintAttr
import com.mrboomdev.awery.util.extensions.setPadding
import com.mrboomdev.awery.util.ui.dialog.DialogBuilder
import com.mrboomdev.awery.util.ui.fields.EditTextField
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaBookmarkDialog(val media: CatalogMedia): BasePanelDialog() {

    override fun getView(context: Context): View {
        val binding = PopupMediaBookmarkBinding.inflate(context.inflater)
        val checked = HashMap<String, Boolean>()

        CoroutineScope(Dispatchers.IO).launch {
            val lists = database.listDao.all
                .filter { !HIDDEN_LISTS.contains(it.id) }

            val progress = database.mediaProgressDao[media.globalId]
                ?: CatalogMediaProgress(media.globalId)

            fun createListView(item: CatalogList) {
                val linear = LinearLayoutCompat(context).apply {
                    gravity = CENTER_VERTICAL
                    orientation = LinearLayoutCompat.HORIZONTAL
                }

                val checkbox = MaterialCheckBox(context).apply {
                    text = item.title
                    linear.addView(this, LinearLayoutCompat.LayoutParams(0, WRAP_CONTENT, 1f))
                }

                AppCompatImageView(context).apply {
                    setImageResource(R.drawable.ic_round_dots_vertical_24)
                    setImageTintAttr(com.google.android.material.R.attr.colorOnSurface)
                    setBackgroundResource(R.drawable.ripple_circle_white)
                    setPadding(context.dpPx(8f))

                    setOnClickListener {
                        requestDeleteList(context, item) {
                            binding.lists.removeView(linear)
                        }
                    }

                    linear.addView(this, dpPx(38f), dpPx(38f))
                }

                if(progress.isInList(item.id)) {
                    checked[item.id] = true
                    checkbox.isChecked = true
                }

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    checked[item.id] = isChecked
                }

                binding.lists.addView(linear)
            }

            launch(Dispatchers.Main) {
                for(list in lists) {
                    createListView(list.toCatalogList())
                }

                binding.create.setOnClickListener {
                    requestCreateNewList(context) {
                        createListView(it)
                    }
                }

                dismissListener = dismissListener@{
                    if(checked.isEmpty()) {
                        dismiss()
                        return@dismissListener
                    }

                    CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
                        toast("Failed to save!")
                        Log.e(TAG, "Failed to save bookmark", t)
                    }).launch {
                        progress.clearLists()

                        for(entry in checked.entries) {
                            if(!entry.value) continue
                            progress.addToList(entry.key)
                        }

                        // Update poster, tags and so on...
                        database.mediaDao.insert(DBCatalogMedia.fromCatalogMedia(media))
                        database.mediaProgressDao.insert(progress)

                        // TODO: Need to replace this with something new
                        //LibraryFragment.notifyDataChanged()
                    }
                }
            }
        }

        return binding.root
    }

    companion object {
        private const val TAG = "MediaBookmarkDialog"

        fun requestCreateNewList(context: Context, callback: (list: CatalogList) -> Unit) {
            val input = EditTextField(context, "List name")
            input.setLinesCount(1)

            val dialog = DialogBuilder(context)
                .setTitle("Create new list")
                .addView(input.view)
                .setNegativeButton(R.string.cancel) { it.dismiss() }
                .setPositiveButton("Create") { dialog ->
                    val text = input.text.trim { it <= ' ' }

                    if(text.isBlank()) {
                        input.setError("List name cannot be empty!")
                        return@setPositiveButton
                    }

                    val window = showLoadingWindow()

                    CoroutineScope(Dispatchers.IO).launch {
                        val list = CatalogList(text)
                        val dbList = DBCatalogList.fromCatalogList(list)
                        database.listDao.insert(dbList)

                        launch(Dispatchers.Main) {
                            callback(list)
                            dialog.dismiss()
                            window.dismiss()
                        }
                    }
                }.show()

            input.setCompletionListener { dialog.performPositiveClick() }
        }

        fun requestDeleteList(context: Context, list: CatalogList, callback: () -> Unit) {
            DialogBuilder(context)
                .setTitle("Delete \"" + list.title + "\"?")
                .setMessage(R.string.sure_delete_list_description)
                .setNegativeButton(R.string.cancel) { it.dismiss() }
                .setPositiveButton(R.string.delete) { dialog ->
                    val window = showLoadingWindow()

                    CoroutineScope(Dispatchers.IO).launch {
                        val dbList = DBCatalogList.fromCatalogList(list)
                        database.listDao.delete(dbList)

                        launch(Dispatchers.Main) {
                            callback()
                            dialog.dismiss()
                            window.dismiss()
                        }
                    }
                }.show()
        }
    }
}