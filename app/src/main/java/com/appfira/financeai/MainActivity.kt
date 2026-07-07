package com.appfira.financeai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import androidx.compose.foundation.layout.*
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
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null) {
                viewModel.setGoogleAccount(account.email, account.displayName, account.photoUrl?.toString())
                android.widget.Toast.makeText(this, getString(R.string.login_success, account.email), android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("MainActivity", "Google Sign-In failed", e)
            val statusCode = (e as? com.google.android.gms.common.api.ApiException)?.statusCode ?: -1
            android.widget.Toast.makeText(this, getString(R.string.login_fail, e.message, statusCode), android.widget.Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/spreadsheets"), Scope("https://www.googleapis.com/auth/drive.file"))
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
                                AuthScreen(onLoginClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) })
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
