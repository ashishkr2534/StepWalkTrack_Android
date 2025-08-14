package com.ethos.stepwalktrack.StepWalk

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ethos.stepwalktrack.R
import com.ethos.stepwalktrack.databinding.ActivityStepWalkBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StepWalkActivity : AppCompatActivity() {

//  private lateinit var binding: ActivityStepWalkTrackBinding
    private  lateinit var binding: ActivityStepWalkBinding
    private var incrementJob: Job? = null
    private var currentSteps = 0
    private val maxSteps = 300

    // Milestones with labels
    private val milestoneData = listOf(
        100 to "Heart Health",
        200 to "SpO₂",
        290 to "Cadence"
    )

    private var lastSteps = 0
    private val triggeredMilestones = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepWalkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure progress view
        binding.stepsTrack.setMaxSteps(maxSteps)
        //binding.stepsTrack.(milestoneData.map { it.first })
        binding.stepsTrack.setMilestones(milestoneData)

        //Show loader
//        binding.heartLoader.visibility = View.VISIBLE
//        binding.tvHeartVal.visibility = View.GONE
//
//        binding.spo2Loader.visibility = View.VISIBLE
//        binding.tvSpo2Val.visibility = View.GONE
//
//        binding.cadenceLoader.visibility = View.VISIBLE
//        binding.tvCadenceVal.visibility = View.GONE


        incrementSteps()
    }

    private fun incrementSteps() {
        incrementJob = CoroutineScope(Dispatchers.Main).launch {
            while (currentSteps < maxSteps) {
                val randomSteps = (1..10).random()
                delay(1000)
                val targetSteps = (currentSteps + randomSteps).coerceAtMost(maxSteps)
                binding.stepsTrack.setSteps(targetSteps, true)
                currentSteps = targetSteps
                updateUI()
            }
        }
    }

    private fun updateUI() {
        binding.tvStepsCount.text = currentSteps.toString()

        // Description update
//        when {
//            currentSteps < milestoneData[0].first -> binding.tvStepsDescription.text =
//                "Just a few more steps to reveal your ${milestoneData[0].second}"
//
//            currentSteps < milestoneData[1].first -> binding.tvStepsDescription.text =
//                "${milestoneData[0].second} unlocked, keep going to unlock ${milestoneData[1].second}"
//
//            currentSteps < milestoneData[2].first -> binding.tvStepsDescription.text =
//                "${milestoneData[1].second} unlocked, keep going to unlock ${milestoneData[2].second}"
//
//            else -> binding.tvStepsDescription.text =
//                "${milestoneData[2].second} unlocked. Great job!"
//        }

        // Change milestone icon/color when reached
        milestoneData.forEachIndexed { index, (step, _) ->
            if (currentSteps >= step) {
                // Change drawable when milestone is reached
                binding.stepsTrack.setMilestoneDrawable(
                    index,
                    ContextCompat.getDrawable(this, R.drawable.checkcircle)
                )

                // binding.stepsTrack.setMilestoneColor(index, ContextCompat.getColor(this, R.color.green))
                //showMilestoneSequence(label, value, R.drawable.bg_heart_health)
            }
        }

        when {
            currentSteps < milestoneData[0].first -> {
                // Heart loader visible
                binding.heartLoader.visibility = View.VISIBLE
                binding.tvHeartVal.visibility = View.GONE

            }
            currentSteps in milestoneData[0].first until milestoneData[1].first -> {
                // Between first and second  Spo2 loader on
                binding.spo2Loader.visibility = View.VISIBLE
                binding.tvSpo2Val.visibility = View.GONE

            }
            currentSteps in milestoneData[1].first until milestoneData[2].first -> {
                // Between second and third
                binding.cadenceLoader.visibility = View.VISIBLE
                binding.tvCadenceVal.visibility = View.GONE
            }
            else -> {
                // All values shown
                binding.heartLoader.visibility = View.GONE
                binding.tvHeartVal.visibility = View.VISIBLE

                binding.spo2Loader.visibility = View.GONE
                binding.tvSpo2Val.visibility = View.VISIBLE

                binding.cadenceLoader.visibility = View.GONE
                binding.tvCadenceVal.visibility = View.VISIBLE
            }
        }

        //Conditional trigger for milestones
        milestoneData.forEach { (step, label) ->
            if(!triggeredMilestones.contains(step) && lastSteps < step && currentSteps >= step){
                triggeredMilestones.add(step)

                when (label) {
                    "Heart Health" -> {
                        // When milestone reached
                        binding.tvHeartVal.text = "64"
                        binding.heartLoader.visibility = View.GONE
                        binding.tvHeartVal.visibility = View.VISIBLE
                    }
                    "SpO₂" -> {
                        binding.tvSpo2Val.text = 98.toString()
                        binding.spo2Loader.visibility = View.GONE
                        binding.tvSpo2Val.visibility = View.VISIBLE
                    }
                    "Cadence" -> {
                        binding.tvCadenceVal.text = 99.toString()
                        binding.cadenceLoader.visibility = View.GONE
                        binding.tvCadenceVal.visibility = View.VISIBLE
                    }
                }

                Toast.makeText(this, "$label milestone reached!", Toast.LENGTH_SHORT).show()

                val value = "${currentSteps}"

                val unit = when(label){
                    "Heart Health" -> "bpm"
                    "SpO₂" -> "%"
                    "Cadence" -> "spm"
                    else -> ""
                }

                val detailTitle = when (label){
                    "Heart Health" -> "Your Heart Rate is the number of hearbeats per minute"
                    "SpO₂" -> "SpO₂ is a measure of amount of oxygen in your blood"
                    "Cadence" -> "Cadence is the number of steps you take in a minute"
                    else -> ""
                }
                showMilestoneSequence(label,value,unit,detailTitle)
            }
        }

        lastSteps = currentSteps

    }

    private fun showMilestoneSequence(title: String, value: String,unit: String,detailTitle: String) {
        // Stage 1: Detail
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .add(android.R.id.content, MilestoneDetailFragment.newInstance(detailTitle, value))
            .commit()

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            // Stage 2: Unlocked
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(android.R.id.content, MilestoneUnlockedFragment.newInstance(title, value,unit))
                .commit()

            delay(1500)
            // Remove and go back
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .remove(
                    supportFragmentManager.findFragmentById(android.R.id.content) ?: return@launch
                )
                .commit()
        }
    }

}