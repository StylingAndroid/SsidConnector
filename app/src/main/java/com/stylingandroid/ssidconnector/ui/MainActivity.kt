package com.stylingandroid.ssidconnector.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.stylingandroid.ssidconnector.R
import com.stylingandroid.ssidconnector.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val requestPermissions =
            registerForActivityResult(RequestMultiplePermissions()) { isGranted ->
                if (isGranted.any { it.value == false }) {
                    navController.popBackStack()
                    navController.navigate(R.id.permissions)
                } else {
                    navController.navigate(R.id.networks)
                }
            }

        requestPermissions.launch(requiredPermissions)
    }
}
