package com.riobener.sonicsoul

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.riobener.sonicsoul.databinding.ActivityMainBinding
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
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.MusicList, R.id.MusicPlayer), binding.drawerLayout)
        binding.navigationLayout.setupWithNavController(controller)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(controller,appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.search)?.actionView as? SearchView
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

}