package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.homefragment

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentHomeBinding
import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage
import com.alpermelkeli.cryptotrader.repository.botRepository.ram.BotManagerStorage
import com.alpermelkeli.cryptotrader.viewmodel.AccountViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private lateinit var accountViewModel: AccountViewModel

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(requireContext())
    }
    private var balance : Float = 0.0f
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)

        balance = userViewModel.getTempBalance()

        binding.accountBalanceUsdtText.text = String.format("%.2f USDT", balance)

        initializeAccountOperations()

        setUpUserUI()


        return binding.root
    }
    private fun updateAccountBalance() {
        accountViewModel.balance.observe(viewLifecycleOwner){
            userViewModel.storeTempBalance(it)
            updateAccountBalanceAnimated(it)
        }
    }
    private fun updateAccountBalanceAnimated(newBalance: Double) {
        val animator = ValueAnimator.ofFloat(balance, newBalance.toFloat())
        animator.duration = 500
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            binding.accountBalanceUsdtText.text = "%.2f USDT".format(animatedValue)
        }
        animator.start()
    }

    private fun initializeAccountOperations() {
        CoroutineScope(Dispatchers.IO).launch {
            val selectedAPI = ApiStorage.getSelectedApi()
            withContext(Dispatchers.Main) {
                val API_KEY = selectedAPI?.apiKey ?: ""
                val SECRET_KEY = selectedAPI?.secretKey ?: ""
                accountViewModel = AccountViewModel(API_KEY,SECRET_KEY)
                onAccountOperationsInitialized()
            }
        }
    }
    private fun onAccountOperationsInitialized() {
        accountViewModel.getBalance()
        updateAccountBalance()
    }
    private fun setUpUserUI(){
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.nameText.text = user?.email.toString().split("@")[0]
        }
        userViewModel.getCurrentUser()
    }



}