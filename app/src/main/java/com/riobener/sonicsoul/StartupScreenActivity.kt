package com.riobener.sonicsoul

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.riobener.sonicsoul.databinding.SplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.DocumentsContract

import android.net.Uri
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.riobener.sonicsoul.data.settings.Settings
import com.riobener.sonicsoul.data.settings.SettingsName
import com.riobener.sonicsoul.ui.viewmodels.MusicViewModel
import com.riobener.sonicsoul.ui.viewmodels.SettingsViewModel
import com.riobener.sonicsoul.utils.FileUtil
import com.riobener.sonicsoul.utils.launchAndCollectIn


@AndroidEntryPoint
class StartupScreenActivity : AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding

    private val viewModel by viewModels<SettingsViewModel>()
    private val musicViewModel by viewModels<MusicViewModel>()

    companion object {
        private const val PERMISSIONS_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashScreenBinding.inflate(layoutInflater)
        viewModel.settings.launchAndCollectIn(this) { settings ->
            settings.firstOrNull { it.name == SettingsName.LOCAL_DIRECTORY_PATH }?.let { local ->
                musicViewModel.saveLocalMusicToDatabase(local.value)
                settings.firstOrNull { it.name == SettingsName.THEME_APP }?.let {
                    if (it.value == "true")
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                goToMainActivity()
            } ?: setContentView(binding.root)
        }
        binding.setupLocalDirectoryButton.setOnClickListener {
            checkPermissionsAndCallDirectorySetup(permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun goToMainActivity() {
        musicViewModel.loadingFlow.launchAndCollectIn(this){ loadingState ->
            if(!loadingState) {
                val intent = Intent(this@StartupScreenActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                this@StartupScreenActivity.finish()
            }
        }
    }

    private fun checkPermissionsAndCallDirectorySetup(permissions: List<String>) {
        val needRequest = permissions.toMutableList()
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_DENIED) {
                needRequest.remove(it)
            }
        }
        if (needRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needRequest.toTypedArray(), PERMISSIONS_CODE)
        } else {
            callDirectorySetup()
        }
    }

    private val directorySetup = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val dataIntent = it.data ?: return@registerForActivityResult
        dataIntent.data?.let { uri ->
            val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
            val path = FileUtil.getFullPathFromTreeUri(treeUri = docUri, this)
            path?.let {
                viewModel.setupStartupSettings(path)
            }
        }
    }

    fun callDirectorySetup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        directorySetup.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callDirectorySetup()
            } else {
                Toast.makeText(this, "You should accept permissions", Toast.LENGTH_SHORT).show()
            }
        }
    }
}