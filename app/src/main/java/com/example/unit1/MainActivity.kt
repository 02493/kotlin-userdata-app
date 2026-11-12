package com.example.unit1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.unit1.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userDataStore: UserDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDataStore = UserDataStore(this)

        if (savedInstanceState == null) {
            lifecycleScope.launch {
    val (syncName, syncAge) = userDataStore.readUserDataSync()
    if (syncName == null || syncAge == null) {
        val data = userDataStore.userData.first()
        if (data.name != null && data.age != null) {
            userDataStore.saveUserData(data.name, data.age)
        }
    }
    val hasData = userDataStore.hasUserData.first()
    val fragment = if (hasData) HomeFragment() else LoginFragment()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commitAllowingStateLoss()
}
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}