package com.mrboomdev.awery.ui.mobile.screens.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mrboomdev.awery.databinding.LayoutLoadingBinding
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.safeargsnext.owner.SafeArgsFragment

class MediaRelationsFragment : Fragment(), SafeArgsFragment<MediaRelationsFragment.Args> {
	class Args(val media: CatalogMedia)

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val binding = LayoutLoadingBinding.inflate(inflater, container, false)

		binding.title.text = "Coming soon"
		binding.message.text = "This feature will be available in future updates"

		binding.info.visibility = View.VISIBLE
		binding.progressBar.visibility = View.GONE

		return binding.root
	}
}