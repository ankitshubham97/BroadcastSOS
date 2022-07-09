package com.example.broadcastsos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.broadcastsos.databinding.FragmentScreenSlidePageBinding
import com.google.android.material.tabs.TabLayoutMediator

class ScreenSlidePageFragment : Fragment() {

    private lateinit var binding: FragmentScreenSlidePageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentScreenSlidePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        arguments?.takeIf { it.containsKey("INTRO_STRING_OBJECT") }?.apply {
            binding.introTitle.text = getStringArray("INTRO_STRING_OBJECT")!![0]
            binding.introSubTitle.text = getStringArray("INTRO_STRING_OBJECT")!![1]
            changeColor(getStringArray("INTRO_STRING_OBJECT")!![2])
            binding.doneButton.visibility = getInt("DONE_BUTTON_VISIBILITY");
            binding.doneButton.setOnClickListener {
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    fun changeColor(color:String){
        when(color)
        {
            "R" ->
                binding.content.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            "B" ->
                binding.content.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            "G" ->
                binding.content.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
        }
    }

}
