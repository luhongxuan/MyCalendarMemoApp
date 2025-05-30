package com.example.mycalendarmemoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mycalendarmemoapp.ui.MemoViewModel
import com.example.mycalendarmemoapp.ui.MemoViewModelFactory
import com.example.mycalendarmemoapp.ui.screen.AddMemoScreen
import com.example.mycalendarmemoapp.ui.screen.HomeScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext
    val memoViewModel: MemoViewModel = viewModel(
        factory = MemoViewModelFactory(context as android.app.Application)
    )

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, viewModel = memoViewModel)
        }
        composable("add_memo") {
            AddMemoScreen(navController = navController, viewModel = memoViewModel)
        }
    }
}