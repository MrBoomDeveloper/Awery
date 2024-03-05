package ani.awery.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.mrboomdev.awery.databinding.FragmentOfflineBinding
import ani.awery.isOnline
import ani.awery.navBarHeight
import ani.awery.startMainActivity
import ani.awery.statusBarHeight

class OfflineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentOfflineBinding.inflate(inflater, container, false)
        binding.refreshContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = statusBarHeight
            bottomMargin = navBarHeight
        }
        binding.refreshButton.setOnClickListener {
            if (isOnline(requireContext())) {
                startMainActivity(requireActivity())
            }
        }
        return binding.root
    }
}