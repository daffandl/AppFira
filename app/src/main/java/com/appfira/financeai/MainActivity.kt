package com.appfira.financeai

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.appfira.financeai.ui.theme.FinanceAITheme
import com.appfira.financeai.ui.screens.*
import com.appfira.financeai.model.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModel()
    private lateinit var googleSignInClient: com.google.android.gms.auth.api.signin.GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.login_fail, getString(R.string.login_cancelled), -1), Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                viewModel.setGoogleAccount(account.email, account.displayName, account.photoUrl?.toString())
                Toast.makeText(this, getString(R.string.login_success, account.email), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("MainActivity", "Google Sign-In failed", e)
            val statusCode = (e as? ApiException)?.statusCode ?: -1
            Toast.makeText(this, getString(R.string.login_fail, e.message, statusCode), Toast.LENGTH_LONG).show()
        }
    }

    private fun buildGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }

    private fun ensureGooglePlayServicesAvailable(): Boolean {
        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (availability == ConnectionResult.SUCCESS) {
            return true
        }

        val errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, availability, 9000)
        if (errorDialog != null) {
            errorDialog.show()
        } else {
            Toast.makeText(this, "Google Play Services tidak tersedia untuk login.", Toast.LENGTH_LONG).show()
        }
        return false
    }

    private fun launchGoogleLogin() {
        if (!ensureGooglePlayServicesAvailable()) {
            return
        }

        googleSignInClient = GoogleSignIn.getClient(this, buildGoogleSignInOptions())
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        googleSignInClient = GoogleSignIn.getClient(this, buildGoogleSignInOptions())

        val lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastAccount != null) {
            viewModel.setGoogleAccount(lastAccount.email, lastAccount.displayName, lastAccount.photoUrl?.toString())
        }

        setContent {
            val settingsState by viewModel.settings.collectAsState()
            // Gunakan default settings dulu agar theme langsung aktif, hindari flash putih
            val settings = settingsState ?: AppSettings()

            val currentContext = androidx.compose.ui.platform.LocalContext.current
            val localizedContext = remember(settings.language) {
                com.appfira.financeai.logic.LocalizationUtils.setLocale(currentContext, settings.language)
            }

            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localizedContext,
                androidx.compose.ui.platform.LocalConfiguration provides localizedContext.resources.configuration
            ) {
                FinanceAITheme(theme = settings.theme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (settingsState == null) {
                            // Masih loading dari DB — tampilkan blank dengan warna yang benar
                            // (tidak perlu spinner, transisi akan sangat cepat)
                        } else {
                            val userEmail by viewModel.userEmail.collectAsState()
                            
                            if (userEmail == null) {
                                AuthScreen(onLoginClick = { launchGoogleLogin() })
                            } else {
                                MainScreen(viewModel, settings) {
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        viewModel.logout()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
