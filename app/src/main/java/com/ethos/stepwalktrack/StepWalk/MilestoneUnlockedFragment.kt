package com.ethos.stepwalktrack.StepWalk

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ethos.stepwalktrack.R
import com.ethos.stepwalktrack.databinding.FragmentMilestoneUnlockedBinding
import kotlin.apply

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MilestoneUnlockedFragment : Fragment() {

    private lateinit var binding: FragmentMilestoneUnlockedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMilestoneUnlockedBinding.inflate(inflater, container, false)

        val title = arguments?.getString("title") ?: ""
        val value = arguments?.getString("value") ?: ""

        val unit = arguments?.getString("unit") ?: ""

        binding.tvUnlocked.text = "$title Unlocked!"

        val paint = binding.tvCenterValue.paint
        val width = paint.measureText(binding.tvCenterValue.text.toString())

        val textShader = LinearGradient(
            0f, 0f, width, binding.tvCenterValue.textSize,
            intArrayOf(
                ContextCompat.getColor(requireContext(), R.color.teal_light),
                ContextCompat.getColor(requireContext(), R.color.teal),
                ContextCompat.getColor(requireContext(), R.color.grey)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        binding.tvCenterValue.paint.shader = textShader

        binding.tvCenterValue.text = value
        binding.tvUnit.text = unit

        return binding.root
    }

    companion object {
        fun newInstance(title: String, value: String,unit: String) =
            MilestoneUnlockedFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("value", value)
                    putString("unit", unit)
                }
            }
    }
}
