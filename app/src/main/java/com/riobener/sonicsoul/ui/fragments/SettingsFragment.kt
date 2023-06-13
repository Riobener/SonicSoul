package com.riobener.sonicsoul.ui.fragments

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.riobener.sonicsoul.R
import com.riobener.sonicsoul.data.settings.Settings
import com.riobener.sonicsoul.data.settings.SettingsName
import com.riobener.sonicsoul.ui.viewmodels.MusicViewModel
import com.riobener.sonicsoul.ui.viewmodels.SettingsViewModel
import com.riobener.sonicsoul.ui.viewmodels.OnlineServiceViewModel
import com.riobener.sonicsoul.utils.FileUtil
import com.riobener.sonicsoul.utils.launchAndCollectIn
import android.media.audiofx.AudioEffect
import com.riobener.sonicsoul.ui.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel by activityViewModels<SettingsViewModel>()
    private val credentialsViewModel by activityViewModels<OnlineServiceViewModel>()
    private val musicViewModel by activityViewModels<MusicViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFragmentScreen()
        setupSettings()
    }

    fun setupFragmentScreen() {
        activity?.let {
            (it as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.settings_fragment)
            if (it.toolbar?.menu?.findItem(R.id.search) != null)
                it.toolbar.menu.removeItem(R.id.search)
            if (it.toolbar?.menu?.findItem(R.id.sort) != null)
                it.toolbar.menu.removeItem(R.id.sort)
        }
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
                            (activity as MainActivity).setNightMode(setting.value.toBooleanStrict())
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
                    servicePref?.title = resources.getString(R.string.settings_service_logout)
                    servicePref?.setOnPreferenceClickListener {
                        credentialsViewModel.cleanToken()
                        musicViewModel.needToReload = true
                        true
                    }
                } else {
                    servicePref?.title = resources.getString(R.string.settings_service_summary)
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
                musicViewModel.needToReload = true
            }
        }
    }

    fun callDirectorySetup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        directorySetup.launch(intent)
    }
}