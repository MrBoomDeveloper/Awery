package ani.awery.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ani.awery.BottomSheetDialogFragment
import com.mrboomdev.awery.databinding.BottomSheetDevelopersBinding

class DevelopersDialogFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetDevelopersBinding? = null
    private val binding get() = _binding!!

    private val developers = arrayOf(
        Developer(
            "MrBoomDev",
            "https://avatars.githubusercontent.com/u/92123190?v=4",
            "Awery Developer",
            "https://github.com/MrBoomDeveloper"
        ),
        Developer(
            "rebelonion",
            "https://avatars.githubusercontent.com/u/87634197?v=4",
            "Dantotsu Developer",
            "https://github.com/rebelonion"
        ),
        Developer(
            "Wai What",
            "https://avatars.githubusercontent.com/u/149729762?v=4",
            "Icon Designer",
            "https://github.com/WaiWhat"
        ),
        Developer(
            "Aayush262",
            "https://avatars.githubusercontent.com/u/99584765?v=4",
            "Contributor",
            "https://github.com/aayush2622"
        ),
        Developer(
            "MarshMeadow",
            "https://avatars.githubusercontent.com/u/88599122?v=4",
            "Beta Icon Designer",
            "https://github.com/MarshMeadow?tab=repositories"
        ),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDevelopersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.devsRecyclerView.adapter = DevelopersAdapter(developers)
        binding.devsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}