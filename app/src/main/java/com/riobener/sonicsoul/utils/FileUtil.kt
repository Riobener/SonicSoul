package com.riobener.sonicsoul.utils

import android.provider.DocumentsContract

import android.os.Build

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri

import android.os.storage.StorageVolume

import android.os.storage.StorageManager
import androidx.core.content.res.TypedArrayUtils
import java.io.File
import java.lang.Exception
import java.lang.reflect.Array.getLength
import java.lang.reflect.Method
import java.nio.file.Paths.get


object FileUtil {
    private const val PRIMARY_VOLUME_NAME = "primary"
    fun getFullPathFromTreeUri(treeUri: Uri?, con: Context?): String? {
        if (treeUri == null) return null
        var volumePath: String? = con?.let { getVolumePath(getVolumeIdFromTreeUri(treeUri), it) } ?: File.separator
        if (volumePath is String) volumePath = volumePath.substring(0, volumePath.length - 1)
        var documentPath = getDocumentPathFromTreeUri(treeUri)
        if (documentPath != null) {
            if (documentPath.endsWith(File.separator)) documentPath = documentPath.substring(0, documentPath.length - 1)
        }
        return if (documentPath!!.isNotEmpty()) {
            if (documentPath.startsWith(File.separator)) volumePath + documentPath else volumePath + File.separator.toString() + documentPath
        } else volumePath
    }

    private fun getVolumePath(volumeId: String?, context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getVolumePathForAndroid11AndAbove(
            volumeId,
            context
        ) else getVolumePathBeforeAndroid11(volumeId, context)
    }

    private fun getVolumePathBeforeAndroid11(volumeId: String?, context: Context): String? {
        return try {
            val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList: Method = mStorageManager.javaClass.getMethod("getVolumeList")
            val getUuid: Method = storageVolumeClazz.getMethod("getUuid")
            val getPath: Method = storageVolumeClazz.getMethod("getPath")
            val isPrimary: Method = storageVolumeClazz.getMethod("isPrimary")
            val result: Array<String> = getVolumeList.invoke(mStorageManager) as Array<String>
            val length: Int = result.size
            for (i in 0 until length) {
                val storageVolumeElement: String =  result[i]
                val uuid = getUuid.invoke(storageVolumeElement) as String
                val primary = isPrimary.invoke(storageVolumeElement) as Boolean
                if (primary && PRIMARY_VOLUME_NAME == volumeId) // primary volume?
                    return getPath.invoke(storageVolumeElement).toString()
                if (uuid != null && uuid == volumeId) // other volumes?
                    return getPath.invoke(storageVolumeElement).toString()
            }
            // not found.
            null
        } catch (ex: Exception) {
            null
        }
    }

    @TargetApi(Build.VERSION_CODES.R)
    private fun getVolumePathForAndroid11AndAbove(volumeId: String?, context: Context): String? {
        return try {
            val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumes = mStorageManager.storageVolumes
            for (storageVolume in storageVolumes) {
                // primary volume?
                if (storageVolume.isPrimary && PRIMARY_VOLUME_NAME == volumeId) return storageVolume.directory!!
                    .path

                // other volumes?
                val uuid = storageVolume.uuid
                if (uuid != null && uuid == volumeId) return storageVolume.directory!!.path
            }
            // not found.
            null
        } catch (ex: Exception) {
            null
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getVolumeIdFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split = docId.split(":").toTypedArray()
        return if (split.size > 0) split[0] else null
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getDocumentPathFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val split: Array<String?> = docId.split(":").toTypedArray()
        return if (split.size >= 2 && split[1] != null) split[1] else File.separator
    }
}

