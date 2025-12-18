package com.uanish.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.uanish.expensetracker.viewmodel.SimpleVmFactory
import com.uanish.expensetracker.data.repository.FinanceRepository
import com.uanish.expensetracker.ui.screens.auth.LoginScreen
import com.uanish.expensetracker.ui.screens.auth.SignUpScreen
import com.uanish.expensetracker.ui.screens.main.*
import com.uanish.expensetracker.viewmodel.*

@Composable
fun AppNavGraph(
    nav: NavHostController,
    authVm: AuthViewModel,
    financeRepo: FinanceRepository
) {
    val authState by authVm.authState.collectAsStateWithLifecycle()

    val start = when (authState) {
        is AuthState.Authenticated -> Routes.DASHBOARD
        else -> Routes.LOGIN
    }

    NavHost(navController = nav, startDestination = start) {

        // ---------------- AUTH ----------------
        composable(Routes.LOGIN) {
            LoginScreen(
                authState = authState,
                onLogin = authVm::signIn,
                onGoSignup = { nav.navigate(Routes.SIGNUP) },
                onClearError = authVm::clearError
            )
        }

        composable(Routes.SIGNUP) {
            SignUpScreen(
                authState = authState,
                onSignup = authVm::signUp,
                onGoLogin = { nav.popBackStack() },
                onClearError = authVm::clearError
            )
        }

        // ---------------- DASHBOARD ----------------
        composable(Routes.DASHBOARD) {
            RequireAuth(authState, nav) { uid ->
                val vm: DashboardViewModel = viewModel(
                    factory = SimpleVmFactory { DashboardViewModel(uid, financeRepo) }
                )
                DashboardScreen(
                    vm = vm,
                    onGoTransactions = { nav.navigate(Routes.TX_LIST) },
                    onGoCategories = { nav.navigate(Routes.CATEGORIES) },
                    onGoProfile = { nav.navigate(Routes.PROFILE) },
                    onOpenTx = { txId -> nav.navigate("tx_detail/$txId") }
                )
            }
        }

        // ---------------- CATEGORIES ----------------
        composable(Routes.CATEGORIES) {
            RequireAuth(authState, nav) { uid ->
                val vm: CategoriesViewModel = viewModel(
                    factory = SimpleVmFactory { CategoriesViewModel(uid, financeRepo) }
                )
                CategoriesScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() }
                )
            }
        }

        // ---------------- TRANSACTIONS LIST ----------------
        composable(Routes.TX_LIST) {
            RequireAuth(authState, nav) { uid ->
                val vm: TransactionsViewModel = viewModel(
                    factory = SimpleVmFactory { TransactionsViewModel(uid, financeRepo) }
                )
                TransactionsListScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    onAdd = { nav.navigate("tx_edit") },
                    onOpen = { txId -> nav.navigate("tx_detail/$txId") },
                    onEdit = { txId -> nav.navigate("tx_edit?txId=$txId") }
                )
            }
        }

        // ---------------- TRANSACTION DETAIL ----------------
        composable(
            route = Routes.TX_DETAIL,
            arguments = listOf(navArgument("txId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequireAuth(authState, nav) { uid ->
                val txId = backStackEntry.arguments?.getString("txId").orEmpty()

                // Use TransactionsViewModel only for categories map (names)
                val listVm: TransactionsViewModel = viewModel(
                    factory = SimpleVmFactory { TransactionsViewModel(uid, financeRepo) }
                )
                val categories by listVm.categories.collectAsStateWithLifecycle()
                val catNameById = remember(categories) { categories.associate { it.id to it.name } }

                val vm: TransactionDetailViewModel = viewModel(
                    factory = SimpleVmFactory { TransactionDetailViewModel(uid, txId, financeRepo) }
                )

                TransactionDetailScreen(
                    vm = vm,
                    categoryName = { id -> catNameById[id] ?: "Unknown" },
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("tx_edit?txId=$txId") }
                )
            }
        }

        // ---------------- TRANSACTION EDIT ----------------
        composable(
            route = Routes.TX_EDIT,
            arguments = listOf(
                navArgument("txId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            RequireAuth(authState, nav) { uid ->
                val txId = backStackEntry.arguments?.getString("txId").orEmpty()

                // Use TransactionsViewModel only to get categories list for dropdown
                val listVm: TransactionsViewModel = viewModel(
                    factory = SimpleVmFactory { TransactionsViewModel(uid, financeRepo) }
                )
                val categories by listVm.categories.collectAsStateWithLifecycle()

                val vm: TransactionEditViewModel = viewModel(
                    factory = SimpleVmFactory { TransactionEditViewModel(uid, txId, financeRepo) }
                )

                TransactionEditScreen(
                    vm = vm,
                    categories = categories,
                    onBack = { nav.popBackStack() },
                    onDone = { nav.popBackStack() }
                )
            }
        }

        // ---------------- PROFILE ----------------
        composable(Routes.PROFILE) {
            RequireAuth(authState, nav) { _ ->
                val email = (authState as AuthState.Authenticated).email
                ProfileScreen(
                    email = email,
                    onBack = { nav.popBackStack() },
                    onSignOut = authVm::signOut
                )
            }
        }
    }
}

@Composable
private fun RequireAuth(
    authState: AuthState,
    nav: NavHostController,
    content: @Composable (uid: String) -> Unit
) {
    if (authState is AuthState.Authenticated) content(authState.uid)
    else nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
}
