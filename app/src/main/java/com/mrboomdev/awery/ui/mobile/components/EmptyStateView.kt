package com.mrboomdev.awery.ui.mobile.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.viewbinding.ViewBinding
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.mrboomdev.awery.databinding.LayoutLoadingBinding
import com.mrboomdev.awery.util.extensions.inflater

class EmptyStateView(private val binding: LayoutLoadingBinding) : ViewBinding {
	val title: TextView = binding.title
	val message: TextView = binding.message
	val progressBar: CircularProgressIndicator = binding.progressBar
	val info: LinearLayout = binding.info
	val button: Button = binding.button
	val button2: Button = binding.button2

	constructor(context: Context) :
			this(LayoutLoadingBinding.inflate(context.inflater))

	constructor(parent: ViewGroup, attachToParent: Boolean = false) :
			this(LayoutLoadingBinding.inflate(parent.context.inflater, parent, attachToParent))

	@JvmOverloads
	fun setInfo(
		title: String? = null,
		message: String? = null,
		buttonText: String? = null,
		buttonClickListener: Runnable? = null,
		button2Text: String? = null,
		button2OnClick: () -> Unit = {}
	) {
		binding.title.text = title
		binding.message.text = message

		progressBar.visibility = View.GONE
		info.visibility = View.VISIBLE
		button.visibility = View.GONE

		if(buttonText != null && buttonClickListener != null) {
			button.text = buttonText
			button.setOnClickListener { buttonClickListener.run() }
			button.visibility = View.VISIBLE
		} else {
			button.visibility = View.GONE
		}

		if(button2Text != null) {
			button2.text = button2Text
			button2.setOnClickListener { button2OnClick() }
			button2.visibility = View.VISIBLE
		} else {
			button2.visibility = View.GONE
		}
	}

	fun hideAll() {
		progressBar.visibility = View.GONE
		info.visibility = View.GONE
	}

	fun setInfo(@StringRes title: Int, @StringRes message: Int) {
		setInfo(context.getString(title), context.getString(message))
	}

	val context: Context
		get() = binding.root.context

	fun startLoading() {
		progressBar.visibility = View.VISIBLE
		info.visibility = View.GONE
	}

	override fun getRoot(): View {
		return binding.root
	}
}