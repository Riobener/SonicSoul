package com.riobener.sonicsoul

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.riobener.sonicsoul.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

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
        val controller = Navigation.findNavController(nav_host_fragment_content_main.requireView())
        binding.navigationLayout.setupWithNavController(controller)
        controller.addOnDestinationChangedListener(NavController.OnDestinationChangedListener { controller, destination, arguments ->
            binding.pageTitle.text = when (destination.id) {
                R.id.music_list_fragment -> "Online music"
                R.id.MusicDetails -> "Offline music"
                else -> "Default title"
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(binding.root.rootView).navigateUp() || super.onSupportNavigateUp()
    }

}