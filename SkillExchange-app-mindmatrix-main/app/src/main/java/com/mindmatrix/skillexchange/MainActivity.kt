package com.mindmatrix.skillexchange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindmatrix.skillexchange.repository.MockSkillExchangeRepository
import com.mindmatrix.skillexchange.ui.CreatePostScreen
import com.mindmatrix.skillexchange.ui.PostDetailScreen
import com.mindmatrix.skillexchange.ui.ProfileScreen
import com.mindmatrix.skillexchange.ui.SkillBoardScreen
import com.mindmatrix.skillexchange.ui.LoginScreen
import com.mindmatrix.skillexchange.ui.SignupScreen
import com.mindmatrix.skillexchange.viewmodel.SkillExchangeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SkillExchangeApp()
                }
            }
        }
    }
}

@Composable
fun SkillExchangeApp() {
    val navController = rememberNavController()
    
    // Create the mock repository (in a real app, this would be injected via Hilt/Dagger)
    val repository = remember { MockSkillExchangeRepository() }
    
    // Create ViewModel factory
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SkillExchangeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SkillExchangeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    val viewModel: SkillExchangeViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController, viewModel)
        }
        composable("signup") {
            SignupScreen(navController, viewModel)
        }
        composable("board") {
            SkillBoardScreen(navController, viewModel)
        }
        composable("create_post") {
            CreatePostScreen(navController, viewModel)
        }
        composable("profile") {
            ProfileScreen(navController, viewModel)
        }
        composable("post_detail/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                PostDetailScreen(navController, viewModel, postId)
            }
        }
    }
}
