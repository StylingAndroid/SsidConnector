package com.stylingandroid.ssidconnector.ui.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.stylingandroid.ssidconnector.databinding.PermissionsFragmentBinding

class PermissionsFragment : Fragment() {

    private lateinit var binding: PermissionsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PermissionsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}
