package ani.awery.settings

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import ani.awery.BottomSheetDialogFragment
import com.mrboomdev.awery.R
import ani.awery.connections.anilist.Anilist
import com.mrboomdev.awery.databinding.BottomSheetSettingsBinding
import ani.awery.download.DownloadContainerActivity
import ani.awery.download.manga.OfflineMangaFragment
import ani.awery.loadData
import ani.awery.loadImage
import ani.awery.openLinkInBrowser
import ani.awery.others.imagesearch.ImageSearchActivity
import ani.awery.setSafeOnClickListener
import ani.awery.startMainActivity
import ani.awery.toast


class SettingsDialogFragment() : BottomSheetDialogFragment() {
    private var _binding: BottomSheetSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var pageType: PageType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageType = arguments?.getSerializable("pageType") as? PageType ?: PageType.HOME
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog?.window
        window?.statusBarColor = Color.CYAN
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        window?.navigationBarColor = typedValue.data

        if (Anilist.token != null) {
            binding.settingsLogin.setText(R.string.logout)
            binding.settingsLogin.setOnClickListener {
                Anilist.removeSavedToken(it.context)
                dismiss()
                startMainActivity(requireActivity())
            }
            binding.settingsUsername.text = Anilist.username
            binding.settingsUserAvatar.loadImage(Anilist.avatar)
        } else {
            binding.settingsUsername.visibility = View.GONE
            binding.settingsLogin.setText(R.string.login)
            binding.settingsLogin.setOnClickListener {
                dismiss()
                Anilist.loginIntent(requireActivity())
            }
        }

        binding.settingsExtensionSettings.setSafeOnClickListener {
            startActivity(Intent(activity, ExtensionsActivity::class.java))
            dismiss()
        }

        binding.settingsAnilistSettings.setOnClickListener {
            openLinkInBrowser("https://anilist.co/settings/lists")
            dismiss()
        }

        binding.imageSearch.setOnClickListener {
            startActivity(Intent(activity, ImageSearchActivity::class.java))
            dismiss()
        }

        binding.settingsDownloads.setSafeOnClickListener {
            when (pageType) {
                PageType.MANGA -> {
                    val intent = Intent(activity, DownloadContainerActivity::class.java)
                    intent.putExtra("FRAGMENT_CLASS_NAME", OfflineMangaFragment::class.java.name)
                    startActivity(intent)
                }

                PageType.ANIME -> {
                    try {
                        val arrayOfFiles =
                            ContextCompat.getExternalFilesDirs(requireContext(), null)
                        startActivity(
                            if (loadData<Boolean>("sd_dl") == true && arrayOfFiles.size > 1 && arrayOfFiles[0] != null && arrayOfFiles[1] != null) {
                                val parentDirectory = arrayOfFiles[1].toString()
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(Uri.parse(parentDirectory), "resource/folder")
                            } else Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                        )
                    } catch (e: ActivityNotFoundException) {
                        toast(getString(R.string.file_manager_not_found))
                    }
                }

                PageType.HOME -> {
                    try {
                        val arrayOfFiles =
                            ContextCompat.getExternalFilesDirs(requireContext(), null)
                        startActivity(
                            if (loadData<Boolean>("sd_dl") == true && arrayOfFiles.size > 1 && arrayOfFiles[0] != null && arrayOfFiles[1] != null) {
                                val parentDirectory = arrayOfFiles[1].toString()
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(Uri.parse(parentDirectory), "resource/folder")
                            } else Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                        )
                    } catch (e: ActivityNotFoundException) {
                        toast(getString(R.string.file_manager_not_found))
                    }
                }
            }

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        enum class PageType {
            MANGA, ANIME, HOME
        }

        fun newInstance(pageType: PageType): SettingsDialogFragment {
            val fragment = SettingsDialogFragment()
            val args = Bundle()
            args.putSerializable("pageType", pageType)
            fragment.arguments = args
            return fragment
        }
    }
}