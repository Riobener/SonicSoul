package com.riobener.sonicsoul

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.riobener.sonicsoul.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.drawerButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}