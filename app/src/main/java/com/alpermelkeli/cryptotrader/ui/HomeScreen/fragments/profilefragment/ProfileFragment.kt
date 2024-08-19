package com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpermelkeli.cryptotrader.R
import com.alpermelkeli.cryptotrader.databinding.FragmentProfileBinding
import com.alpermelkeli.cryptotrader.model.view.Coin
import com.alpermelkeli.cryptotrader.repository.apiRepository.ApiStorage
import com.alpermelkeli.cryptotrader.ui.HomeScreen.HomeScreen
import com.alpermelkeli.cryptotrader.ui.HomeScreen.fragments.profilefragment.adapter.wallet.WalletAdapter
import com.alpermelkeli.cryptotrader.viewmodel.AccountViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModel
import com.alpermelkeli.cryptotrader.viewmodel.UserViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var binding : FragmentProfileBinding
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var adapter: WalletAdapter
    private var balance: Float = 0.0f
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(requireContext())
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        setProgressBarAnimation()

        balance = userViewModel.getTempBalance()

        binding.profileBalanceText.text = String.format("%.2f USDT",balance)

        initializeAccountOperations()

        binding.settingsButton.setOnClickListener{
            navigateToSettingsFragment()
        }

        return binding.root
    }
    private fun setProgressBarAnimation(){
        val animator = ObjectAnimator.ofFloat(binding.walletRecyclerProgressBar, "rotation", 0f, 360f)
        animator.duration = 630
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator.start()
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
                accountViewModel = AccountViewModel(API_KEY,SECRET_KEY)
                onAccountOperationsInitialized()
            }
        }
    }
    private fun updateAccountBalance() {
        accountViewModel.balance.observe(viewLifecycleOwner) {
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
            binding.profileBalanceText.text = "%.2f USDT".format(animatedValue)
        }
        animator.start()
    }
    private fun onAccountOperationsInitialized() {
        accountViewModel.getAccountWallet()
        accountViewModel.getBalance()
        setUpUserUI()
    }
    private fun setupWalletRecycler() {
        val coinList = mutableListOf<Coin>()
        adapter = WalletAdapter(coinList)
        binding.walletRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.walletRecyclerView.adapter = adapter
        accountViewModel.accountWallet.observe(viewLifecycleOwner){
            for (coin in it){
                coinList.add(coin)
                adapter.notifyItemInserted(adapter.itemCount)
            }
            binding.walletRecyclerProgressBar.visibility = View.GONE
        }
    }
    private fun setUpUserUI(){
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.profileIDText.text = user?.email.toString()
        }
        userViewModel.getCurrentUser()
        updateAccountBalance()
        setupWalletRecycler()
    }
}