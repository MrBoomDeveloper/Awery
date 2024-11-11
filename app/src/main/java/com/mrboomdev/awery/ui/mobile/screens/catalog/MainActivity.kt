package com.mrboomdev.awery.ui.mobile.screens.catalog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigationrail.NavigationRailView
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.database
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.app.App.Companion.isLandscape
import com.mrboomdev.awery.app.App.Companion.navigationStyle
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runDelayed
import com.mrboomdev.awery.app.update.UpdatesManager
import com.mrboomdev.awery.app.data.Constants
import com.mrboomdev.awery.app.data.db.item.DBTab
import com.mrboomdev.awery.app.data.settings.SettingsList
import com.mrboomdev.awery.databinding.LayoutHeaderHomeBinding
import com.mrboomdev.awery.databinding.ScreenMainBinding
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.generated.AwerySettings.NavigationStyle_Values
import com.mrboomdev.awery.ui.mobile.screens.search.MultiSearchActivity
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity
import com.mrboomdev.awery.ui.mobile.screens.catalog.feeds.FeedsFragment
import com.mrboomdev.awery.util.IconStateful
import com.mrboomdev.awery.util.TabsTemplate
import com.mrboomdev.awery.util.extensions.UI_INSETS
import com.mrboomdev.awery.util.extensions.addOnBackPressedListener
import com.mrboomdev.awery.util.extensions.applyInsets
import com.mrboomdev.awery.util.extensions.applyTheme
import com.mrboomdev.awery.util.extensions.bottomMargin
import com.mrboomdev.awery.util.extensions.bottomPadding
import com.mrboomdev.awery.util.extensions.dpPx
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.util.extensions.inflater
import com.mrboomdev.awery.util.extensions.leftPadding
import com.mrboomdev.awery.util.extensions.removeOnBackPressedListener
import com.mrboomdev.awery.util.extensions.resolveAttrColor
import com.mrboomdev.awery.util.extensions.rightPadding
import com.mrboomdev.awery.util.extensions.setContentViewCompat
import com.mrboomdev.awery.util.extensions.setHorizontalPadding
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.topPadding
import com.mrboomdev.awery.util.io.FileUtil.readAssets
import com.mrboomdev.awery.ui.mobile.components.EmptyStateView
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity2
import com.mrboomdev.awery.util.ui.FadeTransformer
import com.squareup.moshi.adapter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.lang.ref.WeakReference

@OptIn(ExperimentalStdlibApi::class)
class MainActivity : AppCompatActivity() {
    private var binding: ScreenMainBinding? = null
    private var tabs: List<DBTab>? = null
    private var tabIndex = -1

    private val backListener = object : () -> Unit {
        private var doubleBackToExitPressedOnce = false

        override fun invoke() {
            if(doubleBackToExitPressedOnce) {
                finish()
            }

            doubleBackToExitPressedOnce = true
            toast(R.string.back_to_exit)
            runDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if(savedInstanceState != null) {
            tabIndex = savedInstanceState.getInt(SAVED_TAB_INDEX, -1)
        }

        val template = AwerySettings.TABS_TEMPLATE.value
        if(template == "custom") loadCustomTabs() else loadTemplateTabs(template)

        if(AwerySettings.AUTO_CHECK_APP_UPDATE.value) {
            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, t ->
                Log.e(TAG, "Failed to check for updates!", t)
            }) {
                val update = UpdatesManager.fetchLatestAppUpdate()
                UpdatesManager.showUpdateDialog(this@MainActivity, update)
            }
        }
    }

    private fun loadCustomTabs() {
        lifecycleScope.launch(Dispatchers.IO) {
            setupTabs(database.tabsDao.allTabs)
        }
    }

    private fun loadTemplateTabs(templateName: String) {
        try {
            val selected = getMoshi(SettingsList.ADAPTER).adapter<List<TabsTemplate>>()
                .fromJson(readAssets("tabs_templates.json"))!!
                .find { it.id == templateName }

            setupTabs(selected?.tabs ?: emptyList())
        } catch(e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun setupTabs(theTabs: List<DBTab>) {
        this.tabs = theTabs

        if(theTabs.isEmpty()) {
            runOnUiThread { this.setupEmpty() }
            return
        }

        val tabs = theTabs.sorted()
        val savedDefaultTab = AwerySettings.DEFAULT_HOME_TAB.value

        val icons = getMoshi().adapter<Map<String, IconStateful>>()
            .fromJson(readAssets("icons.json"))!!

        runOnUiThread {
            setupNavigation()

            when(navigationStyle) {
                NavigationStyle_Values.BUBBLE -> {
                    for(i in tabs.indices) {
                        val tab = tabs[i]

                        val drawable = icons[tab.icon]?.getDrawable(this)
                            ?: ContextCompat.getDrawable(this, R.drawable.ic_view_cozy)!!

                        if (tabIndex == -1 && tab.id == savedDefaultTab) {
                            tabIndex = i
                        }

                        binding!!.navbarBubble.addTab(binding!!.navbarBubble.createTab(drawable, tab.title))
                    }

                    binding!!.navbarBubble.selectTabAt(if (tabIndex != -1) tabIndex else 0, false)
                    binding!!.navbarBubble.visibility = View.VISIBLE
                }

                NavigationStyle_Values.MATERIAL -> {
                    val nav = binding!!.navbarMaterial

                    for (i in tabs.indices) {
                        val tab = tabs[i]
                        val icon = icons[tab.icon]

                        val drawable = if (icon != null) icon.getDrawable(this) else ContextCompat.getDrawable(this, R.drawable.ic_view_cozy)!!

                        if (tabIndex == -1 && tab.id == savedDefaultTab) {
                            tabIndex = i
                        }

                        nav.menu.add(0, i, 0, tab.title)
                        nav.menu.getItem(i).setIcon(drawable)
                    }

                    nav.selectedItemId = if (tabIndex != -1) tabIndex else 0
                    nav.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Called after setupTabs() if no tabs was found
     */
    private fun setupEmpty() {
        val binding = EmptyStateView(this)

        binding.setInfo(
            "No tabs found",
            "Please selecting an template or either create your own tabs to see anything here.",
            "Go to settings"
        ) { startActivity(clazz = SettingsActivity::class) }

        setContentViewCompat(binding.root)
    }

    /**
     * Called in middle of setupTabs()
     */
    private fun setupNavigation() {
        binding = ScreenMainBinding.inflate(layoutInflater)
        binding!!.root.setBackgroundColor(resolveAttrColor(android.R.attr.colorBackground))
        setContentView(binding!!.root)

        val pagesAdapter = FeedsAdapter(tabs!!, supportFragmentManager, lifecycle)
        binding!!.pages.adapter = pagesAdapter
        binding!!.pages.isUserInputEnabled = false
        binding!!.pages.setPageTransformer(FadeTransformer())

        if(navigationStyle == NavigationStyle_Values.MATERIAL) {
            if(AwerySettings.USE_AMOLED_THEME.value) {
                binding!!.navbarMaterial.setBackgroundColor(-0x1000000)
                @Suppress("DEPRECATION")
                window.navigationBarColor = if(isLandscape) 0 else -0x1000000
            } else {
                binding!!.navbarMaterial.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
                @Suppress("DEPRECATION")
                window.navigationBarColor = if(isLandscape) 0 else SurfaceColors.SURFACE_2.getColor(this)
            }

            binding!!.navbarMaterial.applyInsets(UI_INSETS, { view, insets ->
                view.topPadding = if((binding!!.navbarMaterial is NavigationRailView)) {
                    insets.top + dpPx(8f)
                } else 0

                view.leftPadding = insets.left
                view.bottomPadding = insets.bottom
                true
            })
        }

        binding!!.bottomSideBarrier.applyInsets(UI_INSETS, { view, insets ->
            view.bottomMargin = insets.bottom
            true
        })

        (fun(tab: Int) {
            binding!!.pages.setCurrentItem(tab, false)
            (binding!!.pages.adapter as? FeedsAdapter)?.fragments?.get(tab)?.get()?.onFocus()
        }).let {
            binding!!.navbarBubble.onTabSelected = { tab -> it(tab.id) }

            binding!!.navbarMaterial.setOnItemSelectedListener { tab ->
                it(tab.itemId)
                true
            }
        }

        binding!!.pages.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) return@setOnFocusChangeListener
            (binding!!.pages.adapter as? FeedsAdapter)?.fragments?.get(binding!!.pages.currentItem)?.get()?.onFocus()
        }

        (fun(tab: Int) {
            (binding!!.pages.adapter as? FeedsAdapter)?.fragments?.get(tab)?.get()?.scrollToTop()
        }).let {
            binding!!.navbarMaterial.setOnItemReselectedListener { it(it.itemId) }
            binding!!.navbarBubble.onTabReselected = { it(it.id) }
        }
    }

    override fun onResume() {
        super.onResume()
        addOnBackPressedListener(backListener)
    }

    override fun onPause() {
        super.onPause()
        removeOnBackPressedListener(backListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if(binding != null) {
            outState.putInt(SAVED_TAB_INDEX, binding!!.pages.currentItem)
        }

        super.onSaveInstanceState(outState)
    }

    private class FeedsAdapter(
		private val tabs: List<DBTab>,
		fragmentManager: FragmentManager,
		lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        val fragments = arrayOfNulls<WeakReference<FeedsFragment>?>(tabs.size)

        override fun createFragment(position: Int): Fragment {
            val tab = tabs[position]
            val arguments = Bundle()

            arguments.putSerializable(FeedsFragment.ARGUMENT_TAB, tab)
            arguments.putSerializable(FeedsFragment.ARGUMENT_FEEDS, tab.feeds as Serializable)

            val fragment = HomeFeedsFragment()
            fragment.arguments = arguments

            fragments[position] = WeakReference(fragment)
            return fragment
        }

        override fun getItemCount(): Int {
            return tabs.size
        }
    }

    class HomeFeedsFragment : FeedsFragment() {
        private var header: LayoutHeaderHomeBinding? = null
        private var isHeaderTransparent = false

        override fun getHeader(parent: ViewGroup): View {
            if(header != null) {
                return header!!.root
            }

            val binding = LayoutHeaderHomeBinding.inflate(parent.context.inflater, parent, false)

            // Note: We do this because the string in resources doesn't tell whatever the app is beta or stable
            binding.title.text = requireContext().applicationInfo.loadLabel(requireContext().packageManager)

            // TODO: Make visible once notifications activity will be done
            binding.notifications.visibility = View.GONE

            binding.search.setOnClickListener {
                val intent = Intent(requireActivity(), MultiSearchActivity::class.java)
                startActivity(intent)
            }

            if(binding.searchBar != null) {
                binding.searchBar.setOnClickListener { binding.search.performClick() }
            }

            binding.settingsWrapper.setOnClickListener {
                if(AwerySettings.EXPERIMENT_SETTINGS2.value) {
                    startActivity(SettingsActivity2::class)
                } else {
                    startActivity(SettingsActivity::class)
                }
            }

            binding.root.applyInsets(UI_INSETS, { view, insets ->
                view.topPadding = insets.top + dpPx(16f)

                if(isLandscape) {
                    view.rightPadding = dpPx(32f) + insets.right
                    view.rightPadding = dpPx(32f) + insets.right
                    view.leftPadding = dpPx(32f) +
                            (if(navigationStyle == NavigationStyle_Values.MATERIAL) 0 else insets.left)
                } else {
                    view.setHorizontalPadding(dpPx(16f))
                }

                false
            })

            header = binding

            if (isHeaderTransparent) {
                updateHeader(false)
            }

            return binding.root
        }

        override fun setContentBehindToolbarEnabled(isEnabled: Boolean) {
            super.setContentBehindToolbarEnabled(isEnabled)
            isHeaderTransparent = isEnabled

            if (header != null) {
                updateHeader(isEnabled)
            }
        }

        private fun updateHeader(isTransparent: Boolean) {
            if (isTransparent) {
                if (header!!.searchBar != null) {
                    header!!.logo.visibility = View.GONE
                    header!!.searchBar!!.visibility = View.GONE
                }

                header!!.title.visibility = View.GONE
                header!!.search.visibility = View.VISIBLE
            } else {
                header!!.title.visibility = View.VISIBLE
                header!!.logo.visibility = View.VISIBLE

                if (header!!.searchBar != null) {
                    header!!.search.visibility = View.GONE
                    header!!.searchBar!!.visibility = View.VISIBLE
                }
            }
        }

        override fun getFilters(): SettingsList {
            return SettingsList()
        }

        override fun getMaxLoadsAtSameTime(): Int {
            return 1
        }

        override fun loadOnStartup(): Boolean {
            return true
        }

        override fun getCacheFile(): File {
            return File(requireContext().cacheDir, Constants.DIRECTORY_NET_CACHE + "/" + Constants.FILE_FEEDS_NET_CACHE)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val SAVED_TAB_INDEX = "was_tab"
    }
}