package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentProfileBinding
import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage
import com.alpermelkeli.cryptotrader.repository.cryptoApi.Binance.BinanceAccountOperations
import com.alpermelkeli.cryptotrader.ui.HomeScreen.HomeScreen
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding
    private lateinit var binanceAccountOperations: BinanceAccountOperations
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(requireContext())
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initializeAccountOperations()
        setUpUserUI()
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
    private fun initializeAccountOperations() {
        CoroutineScope(Dispatchers.IO).launch {
            ApiStorage.initialize(requireContext())
            val selectedAPI = ApiStorage.getSelectedApi()
            withContext(Dispatchers.Main) {
                val API_KEY = selectedAPI?.apiKey ?: ""
                val SECRET_KEY = selectedAPI?.secretKey ?: ""
                binanceAccountOperations = BinanceAccountOperations(API_KEY, SECRET_KEY)
                onAccountOperationsInitialized()
            }
        }
    }
    private fun updateAccountBalance() {
        CoroutineScope(Dispatchers.IO).launch {
            val balance = binanceAccountOperations.accountBalance
            withContext(Dispatchers.Main) {
                binding.profileBalanceText.text = "%.2f USDT".format(balance)
            }
        }
    }
    private fun onAccountOperationsInitialized() {
        updateAccountBalance()
    }
    private fun setUpUserUI(){
        userViewModel.userDocument.observe(viewLifecycleOwner, Observer { document ->
            if (document != null) {
                binding.profileIDText.text = document.get("email").toString()
            }
            else {

            }
        })
        userViewModel.fetchUserDocument()
    }
}