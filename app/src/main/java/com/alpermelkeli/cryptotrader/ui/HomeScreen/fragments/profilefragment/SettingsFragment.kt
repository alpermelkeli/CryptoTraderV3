package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentSettingsBinding
import com.alpermelkeli.cryptotrader.repository.botRepository.BotService
import com.alpermelkeli.cryptotrader.ui.HomeScreen.HomeScreen
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        binding.btnProfileSettings.setOnClickListener {

        }

        binding.btnApiSettings.setOnClickListener {
            navigateToApiSettings()
        }

        binding.stopServiceButton.setOnClickListener{
            stopAllServices()
        }

        binding.signOutButton.setOnClickListener {
            signOut()
            stopAllServices()
        }
        return binding.root
    }
    private fun signOut(){
        userViewModel.logout()
        activity?.finish()
    }

    private fun navigateToApiSettings() {
        findNavController().navigate(R.id.action_settingsFragment_to_apiSettingsFragment)
        (activity as? HomeScreen)?.hideBottomNavigationView()
    }


    private fun stopAllServices() {
        BotService.stopService()
        Toast.makeText(context, "Servis durduruldu, tüm botları yeniden konfigüre etmelisiniz", Toast.LENGTH_LONG).show()
        activity?.finish()
    }
}
