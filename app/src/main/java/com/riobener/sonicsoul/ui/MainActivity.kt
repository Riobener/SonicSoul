package com.riobener.sonicsoul.ui

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.databinding.ActivityMainBinding
import com.riobener.sonicsoul.ui.fragments.MusicListFragment
import com.riobener.sonicsoul.ui.fragments.MusicListFragmentDirections
import com.riobener.sonicsoul.ui.fragments.SettingsFragment
import com.riobener.sonicsoul.ui.fragments.SettingsFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val controller = Navigation.findNavController(nav_host_fragment_content_main.requireView())
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.MusicList, R.id.SettingsFragment), binding.drawerLayout)
        binding.navigationLayout.setupWithNavController(controller)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(controller,appBarConfiguration)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        this.supportActionBar?.title = "Start Screen"
        binding.navigationLayout.setNavigationItemSelectedListener {
            val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
            when (it.itemId) {
                R.id.LocalMusicList -> {
                    val action = when(currentFragment){
                        is SettingsFragment -> SettingsFragmentDirections.actionSettingsFragmentToMusicList()
                        is MusicListFragment -> MusicListFragmentDirections.actionMusicListSelf()
                        else -> null
                    }
                    action?.let{
                        if(it is MusicListFragmentDirections.ActionMusicListSelf){
                            it.onlineOffline = "offline"
                        }
                        if(it is SettingsFragmentDirections.ActionSettingsFragmentToMusicList){
                            it.onlineOffline = "offline"
                        }
                        Navigation.findNavController(nav_host_fragment_content_main.requireView()).navigate(action)
                    }
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.OnlineMusicList -> {
                    val action = when(currentFragment){
                        is SettingsFragment -> SettingsFragmentDirections.actionSettingsFragmentToMusicList()
                        is MusicListFragment -> MusicListFragmentDirections.actionMusicListSelf()
                        else -> null
                    }
                    action?.let{
                        if(it is MusicListFragmentDirections.ActionMusicListSelf){
                            it.onlineOffline = "online"
                        }
                        if(it is SettingsFragmentDirections.ActionSettingsFragmentToMusicList){
                            it.onlineOffline = "online"
                        }
                        Navigation.findNavController(nav_host_fragment_content_main.requireView()).navigate(action)
                    }
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.SettingsFragment -> {
                    val action = when(currentFragment){
                        is SettingsFragment -> null
                        is MusicListFragment -> MusicListFragmentDirections.actionMusicListToSettingsFragment()
                        else -> null
                    }
                    action?.let{
                        Navigation.findNavController(nav_host_fragment_content_main.requireView()).navigate(action)
                    }
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.search)?.actionView as? SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
        if(currentFragment is SettingsFragment)
        currentFragment.setupFragmentScreen()
        return true
    }

     fun setNightMode(value: Boolean){
        if (value)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}