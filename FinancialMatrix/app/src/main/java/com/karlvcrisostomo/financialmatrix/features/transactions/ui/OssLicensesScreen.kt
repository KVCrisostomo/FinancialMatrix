package com.karlvcrisostomo.financialmatrix.features.transactions.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OssLicensesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val libsState = produceState<Libs?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            try {
                val json = context.assets.open("aboutlibraries.json").bufferedReader().use { it.readText() }
                Libs.Builder().withJson(json).build()
            } catch (e: Exception) {
                null
            }
        }
    }

    val libs = libsState.value
    if (libs != null) {
        LibrariesContainer(
            libraries = libs,
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
