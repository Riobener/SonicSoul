package com.riobener.sonicsoul

import android.Manifest
import android.R.attr
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.riobener.sonicsoul.databinding.SplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.DocumentsContract

import android.R.attr.data
import android.net.Uri
import android.os.FileUtils
import com.riobener.sonicsoul.utils.FileUtil


@AndroidEntryPoint
class SplahScreenActivity : AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding

    companion object {
        private const val PERMISSIONS_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.setupLocalDirectoryButton.setOnClickListener {
            checkPermissionsAndCallDirectorySetup(permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }

    }

    private fun checkPermissionsAndCallDirectorySetup(permissions: List<String>) {
        val needRequest = permissions.toMutableList()
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_DENIED) {
                needRequest.remove(it)
            }
        }
        if(needRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(this, needRequest.toTypedArray(), PERMISSIONS_CODE)
        }else{
            callDirectorySetup()
        }
    }
    private val directorySetup = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val dataIntent = it.data ?: return@registerForActivityResult
        dataIntent.data?.let{ uri ->
            val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
            val path = FileUtil.getFullPathFromTreeUri(treeUri = docUri, this)
        }
    }
    fun callDirectorySetup(){
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