package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentProfileBinding
import com.alpermelkeli.cryptotrader.ui.HomeScreen.HomeScreen

class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.settingsButton.setOnClickListener{
            navigateToSettingsFragment()
        }



        return binding.root
    }
    override fun onResume() {
        super.onResume()
        (activity as? HomeScreen)?.showBottomNavigationView()
    }

    private fun navigateToSettingsFragment(){
        findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        (activity as? HomeScreen)?.hideBottomNavigationView()
    }

}