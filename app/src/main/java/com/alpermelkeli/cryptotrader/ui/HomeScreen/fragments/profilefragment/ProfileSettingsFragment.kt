package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentProfileSettingsBinding


class ProfileSettingsFragment : Fragment() {
    private lateinit var binding : FragmentProfileSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container ,false)





        return binding.root
    }


}