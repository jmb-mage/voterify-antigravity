package com.voterify.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voterify.app.models.ElectionModel
import com.voterify.app.models.PermissionResourceType
import com.voterify.app.models.PermissionType
import com.voterify.app.services.AppInterfaceMode
import com.voterify.app.services.AppStoreViewModel
import com.voterify.app.ui.admin.AdminDashboardScreen
import com.voterify.app.ui.auth.LoginScreen
import com.voterify.app.ui.user.BlockchainExplorerScreen
import com.voterify.app.ui.user.ElectionVoteScreen
import com.voterify.app.ui.user.UserDashboardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(store: AppStoreViewModel) {
    var showConnectionDialog by remember { mutableStateOf(false) }
    var showUserMenu by remember { mutableStateOf(false) }
    var selectedElectionForVoting by remember { mutableStateOf<ElectionModel?>(null) }

    if (store.currentUser == null) {
        LoginScreen(store = store)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (store.activeInterfaceMode) {
                                AppInterfaceMode.VOTER -> selectedElectionForVoting?.name ?: "Public Elections"
                                AppInterfaceMode.BLOCKCHAIN -> "Public Blockchain"
                                AppInterfaceMode.ADMIN -> "Admin Console"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Firestore: ${store.firestoreService.config.environment}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showConnectionDialog = true }) {
                        Icon(Icons.Default.Storage, contentDescription = "Firestore Config")
                    }
                    Box {
                        IconButton(onClick = { showUserMenu = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account Menu")
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            store.currentUser?.let { user ->
                                DropdownMenuItem(
                                    text = { Text("${user.name} (${user.oauthProvider.displayName})") },
                                    onClick = {}
                                )
                            }
                            store.currentVoter?.let { voter ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            voter.publicUniqueName,
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    onClick = {}
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Grant Admin & Open Admin Console") },
                                onClick = {
                                    store.currentUser?.let { user ->
                                        store.grantPermission(
                                            userId = user.id,
                                            resourceType = PermissionResourceType.GLOBAL,
                                            resourceId = "all",
                                            permissionType = PermissionType.ADMIN
                                        )
                                        store.activeInterfaceMode = AppInterfaceMode.ADMIN
                                        selectedElectionForVoting = null
                                    }
                                    showUserMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sign Out") },
                                onClick = {
                                    showUserMenu = false
                                    selectedElectionForVoting = null
                                    store.signOut()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = store.activeInterfaceMode == AppInterfaceMode.VOTER,
                    onClick = {
                        selectedElectionForVoting = null
                        store.activeInterfaceMode = AppInterfaceMode.VOTER
                    },
                    icon = { Icon(Icons.Default.HowToVote, contentDescription = null) },
                    label = { Text("Elections") }
                )
                NavigationBarItem(
                    selected = store.activeInterfaceMode == AppInterfaceMode.BLOCKCHAIN,
                    onClick = {
                        selectedElectionForVoting = null
                        store.activeInterfaceMode = AppInterfaceMode.BLOCKCHAIN
                    },
                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                    label = { Text("Blockchain") }
                )
                NavigationBarItem(
                    selected = store.activeInterfaceMode == AppInterfaceMode.ADMIN,
                    onClick = {
                        selectedElectionForVoting = null
                        store.activeInterfaceMode = AppInterfaceMode.ADMIN
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Admin") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (store.activeInterfaceMode) {
                AppInterfaceMode.VOTER -> {
                    val activeElection = selectedElectionForVoting?.let { selected ->
                        store.elections.firstOrNull { it.id == selected.id } ?: selected
                    }
                    if (activeElection != null) {
                        ElectionVoteScreen(
                            store = store,
                            election = activeElection,
                            onBack = { selectedElectionForVoting = null }
                        )
                    } else {
                        UserDashboardScreen(
                            store = store,
                            onSelectElection = { selectedElectionForVoting = it }
                        )
                    }
                }
                AppInterfaceMode.BLOCKCHAIN -> BlockchainExplorerScreen(store = store)
                AppInterfaceMode.ADMIN -> AdminDashboardScreen(store = store)
            }
        }
    }

    if (showConnectionDialog) {
        val cfg = store.firestoreService.config
        AlertDialog(
            onDismissRequest = { showConnectionDialog = false },
            title = { Text("Firestore & BuildConfig Settings") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ConfigRow("Environment", cfg.environment)
                    ConfigRow("Project ID", cfg.projectId)
                    ConfigRow("Database ID", cfg.databaseId)
                    ConfigRow("Host", cfg.host)
                    ConfigRow("SSL", cfg.useSSL.toString())
                    ConfigRow("Emulator", cfg.useEmulator.toString())
                    ConfigRow("REST URL", cfg.baseDocumentsUrlString)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    store.currentUser?.let { ConfigRow("User ID", it.id) }
                    store.currentVoter?.let {
                        ConfigRow("Voter GUID", it.publicUniqueName)
                        ConfigRow("Private Link", it.privateUserId)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConnectionDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(105.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}
