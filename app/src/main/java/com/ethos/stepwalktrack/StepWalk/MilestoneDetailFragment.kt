package com.ethos.stepwalktrack.StepWalk

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ethos.stepwalktrack.R
import com.ethos.stepwalktrack.databinding.FragmentMilestoneDetailBinding
import kotlin.apply

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MilestoneDetailFragment : Fragment() {

    private lateinit var binding: FragmentMilestoneDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMilestoneDetailBinding.inflate(inflater, container, false)

        val title = arguments?.getString("title") ?: ""
        val value = arguments?.getString("value") ?: ""
//        val unit = arguments?.getString("unit")?: ""
        val bgRes = arguments?.getInt("bgRes") ?: R.drawable.background
        val detailTitle = arguments?.getString("detailTitle") ?: ""

        binding.tvTitle.text = detailTitle
//        binding.tvValue.text = value

        binding.bgImage.setImageResource(bgRes)

        return binding.root
    }

    companion object {
//        fun newInstance(title: String, value: String, bgRes: Int) =
        fun newInstance(detailTitle: String, value: String) =
            MilestoneDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("detailTitle", detailTitle)
                    putString("value", value)
//                    putInt("bgRes", bgRes)
//                    putString("unit",unit)
                }
            }
    }
}
