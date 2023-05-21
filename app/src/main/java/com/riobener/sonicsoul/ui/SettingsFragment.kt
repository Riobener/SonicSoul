package com.riobener.sonicsoul.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.settings.Settings
import com.riobener.sonicsoul.data.settings.SettingsName
import com.riobener.sonicsoul.databinding.MusicListFragmentBinding
import com.riobener.sonicsoul.ui.viewmodels.MusicViewModel
import com.riobener.sonicsoul.ui.viewmodels.SettingsViewModel
import com.riobener.sonicsoul.ui.viewmodels.SpotifyViewModel
import com.riobener.sonicsoul.utils.FileUtil
import com.riobener.sonicsoul.utils.launchAndCollectIn
import androidx.core.app.ActivityCompat.startActivityForResult

import android.media.audiofx.AudioEffect
import androidx.appcompat.app.AppCompatDelegate


class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel by activityViewModels<SettingsViewModel>()
    private val credentialsViewModel by activityViewModels<SpotifyViewModel>()
    private val musicViewModel by activityViewModels<MusicViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            (it as AppCompatActivity).supportActionBar?.title = "Settings"
        }
        setupSettings()
    }

    fun setupSettings() {
        viewModel.settings.launchAndCollectIn(viewLifecycleOwner) { settings ->
            val gaplessPref = findPreference<SwitchPreferenceCompat>("gapless")
            val equalizerPref = findPreference<Preference>("equalizer")
            val localStoragePref = findPreference<Preference>("local_storage")
            val themeAppPref = findPreference<SwitchPreferenceCompat>("theme_app")
            val servicePref = findPreference<Preference>("online_service")
            settings.forEach { setting ->
                when (setting.name) {
                    SettingsName.LOCAL_DIRECTORY_PATH -> {
                        localStoragePref?.summary = setting.value
                        localStoragePref?.setOnPreferenceClickListener {
                            callDirectorySetup()
                            true
                        }
                    }
                    SettingsName.IS_GAPLESS -> {
                        gaplessPref?.setOnPreferenceChangeListener { preference, newValue ->
                            setting.value = newValue.toString()
                            viewModel.saveSettings(setting)
                            true
                        }
                        gaplessPref?.isChecked = setting.value.toBooleanStrict()
                    }
                    SettingsName.THEME_APP -> {
                        themeAppPref?.setOnPreferenceChangeListener { preference, newValue ->
                            setting.value = newValue.toString()
                            viewModel.saveSettings(setting)
                            if (setting.value == "true")
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            else
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            true
                        }
                        themeAppPref?.isChecked = setting.value.toBooleanStrict()
                    }
                }
            }
            equalizerPref?.setOnPreferenceClickListener {
                openEqualizer()
                true
            }
            credentialsViewModel.serviceCredentialsFlow.launchAndCollectIn(viewLifecycleOwner) { serviceCredentials ->
                if (serviceCredentials != null) {
                    servicePref?.title = "Exit from account"
                    servicePref?.setOnPreferenceClickListener {
                        credentialsViewModel.cleanToken()
                        true
                    }
                } else {
                    servicePref?.title = "For service data, authorize in Online music screen"
                    servicePref?.setOnPreferenceClickListener {
                        true
                    }
                }
            }
        }
    }

    private val eqResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private fun openEqualizer() {
        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        eqResult.launch(intent)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private val directorySetup = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val dataIntent = it.data ?: return@registerForActivityResult
        dataIntent.data?.let { uri ->
            val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
            val path = FileUtil.getFullPathFromTreeUri(treeUri = docUri, activity)
            path?.let {
                viewModel.saveSettings(
                    Settings.create(
                        name = SettingsName.LOCAL_DIRECTORY_PATH,
                        value = path
                    )
                )
                musicViewModel.saveLocalMusicToDatabase(path)
                musicViewModel.alreadyLoaded = false
            }
        }
    }

    fun callDirectorySetup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        directorySetup.launch(intent)
    }
}