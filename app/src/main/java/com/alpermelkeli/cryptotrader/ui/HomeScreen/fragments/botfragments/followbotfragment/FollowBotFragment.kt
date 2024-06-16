package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.botfragments.followbotfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentFollowBotBinding


class FollowBotFragment : Fragment() {

    private lateinit var binding: FragmentFollowBotBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowBotBinding.inflate(inflater,container,false)




        return binding.root
    }


}