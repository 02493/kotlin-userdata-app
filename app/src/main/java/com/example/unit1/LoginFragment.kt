package com.example.unit1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.unit1.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val ageString = binding.ageEditText.text.toString().trim()

            binding.nameInputLayout.error = null
            binding.ageInputLayout.error = null

            var valid = true
            if (name.isEmpty()) {
                binding.nameInputLayout.error = "Please enter your name"
                valid = false
            }
            val age = ageString.toIntOrNull()
            if (ageString.isEmpty()) {
                binding.ageInputLayout.error = "Please enter your age"
                valid = false
            } else if (age == null) {
                binding.ageInputLayout.error = "Please enter a valid age"
                valid = false
            } else if (age !in 1..150) {
                binding.ageInputLayout.error = "Age must be between 1 and 150"
                valid = false
            }

            if (!valid || age == null) return@setOnClickListener
            viewModel.saveAndProceed(name, age)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (_binding == null) return@collect
                    when (event) {
                        is LoginEvent.NavigateToHome -> parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}