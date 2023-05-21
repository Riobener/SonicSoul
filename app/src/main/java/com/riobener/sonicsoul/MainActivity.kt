package com.riobener.sonicsoul

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.riobener.sonicsoul.databinding.ActivityMainBinding
import com.riobener.sonicsoul.ui.MusicListFragment
import com.riobener.sonicsoul.ui.MusicListFragmentDirections
import com.riobener.sonicsoul.ui.SettingsFragment
import com.riobener.sonicsoul.ui.SettingsFragmentDirections
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
                    //val action = MusicListFragmentDirections.actionMusicListSelf()
                    action?.let{
                        if(it is MusicListFragmentDirections.ActionMusicListSelf){
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
        return true
    }

}