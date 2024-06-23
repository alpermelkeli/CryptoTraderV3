package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
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

    private fun navigateToSettingsFragment() {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
        findNavController().navigate(R.id.action_profileFragment_to_settingsFragment, null, navOptions)
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
    private fun updateAccountBalanceAnimated(newBalance: Double) {
        val currentBalance = 0.0
        val animator = ValueAnimator.ofFloat(currentBalance.toFloat(), newBalance.toFloat())
        animator.duration = 500
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.profileBalanceText.text = "%.2f USDT".format(animatedValue)
        }
        animator.start()
    }
    private fun updateAccountBalance() {
        CoroutineScope(Dispatchers.IO).launch {
            val balance = binanceAccountOperations.accountBalance
            withContext(Dispatchers.Main) {
                updateAccountBalanceAnimated(balance)
            }
        }
    }
    private fun onAccountOperationsInitialized() {
        updateAccountBalance()
    }
    private fun setUpUserUI(){

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.profileIDText.text = user?.email.toString()
        }
        userViewModel.getCurrentUser()
        /*
        userViewModel.userDocument.observe(viewLifecycleOwner, Observer { document ->
            if (document != null) {
                binding.profileIDText.text = document.get("email").toString()
            }
            else {

            }
        })
        userViewModel.fetchUserDocument()
         */
    }
}