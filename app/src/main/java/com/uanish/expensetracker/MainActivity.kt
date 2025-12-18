package com.uanish.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uanish.expensetracker.data.repository.impl.AuthRepositoryImpl
import com.uanish.expensetracker.data.repository.impl.FinanceRepositoryImpl
import com.uanish.expensetracker.ui.navigation.AppNavGraph
import com.uanish.expensetracker.ui.theme.ExpenseTrackerTheme
import com.uanish.expensetracker.viewmodel.AuthViewModel
import com.uanish.expensetracker.viewmodel.SimpleVmFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepo = AuthRepositoryImpl(FirebaseAuth.getInstance())
        val financeRepo = FinanceRepositoryImpl(FirebaseFirestore.getInstance())

        setContent {
            ExpenseTrackerTheme {
                val nav = rememberNavController()

                val authVm: AuthViewModel = viewModel(
                    factory = SimpleVmFactory { AuthViewModel(authRepo) }
                )

                AppNavGraph(
                    nav = nav,
                    authVm = authVm,
                    financeRepo = financeRepo
                )
            }
        }
    }
}
