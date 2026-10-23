package com.voterify.app.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.voterify.app.models.CandidateModel
import com.voterify.app.models.ElectionModel
import com.voterify.app.models.PartyModel
import com.voterify.app.models.PermissionResourceType
import com.voterify.app.models.PermissionType
import com.voterify.app.models.RegionModel
import com.voterify.app.services.AppStoreViewModel

enum class AdminSection(val label: String) {
    ELECTIONS("Elections"),
    CANDIDATES("Candidates"),
    REGIONS("Regions"),
    PARTIES("Parties"),
    PERMISSIONS("Permissions")
}

@Composable
fun AdminDashboardScreen(store: AppStoreViewModel) {
    var selectedSection by remember { mutableStateOf(AdminSection.ELECTIONS) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Permission Status Banner
        store.currentUser?.let { user ->
            val globalLevel = store.effectivePermission(user.id, PermissionResourceType.GLOBAL)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "User: ${user.name} • Permission: ${globalLevel.rawValue}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!store.currentUserCanAccessAdminConsole) {
                    TextButton(
                        onClick = {
                            store.grantPermission(
                                userId = user.id,
                                resourceType = PermissionResourceType.GLOBAL,
                                resourceId = "all",
                                permissionType = PermissionType.ADMIN
                            )
                        }
                    ) {
                        Text("Elevate to Admin")
                    }
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = selectedSection.ordinal) {
            AdminSection.entries.forEach { section ->
                Tab(
                    selected = selectedSection == section,
                    onClick = { selectedSection = section },
                    text = { Text(section.label) }
                )
            }
        }

        when (selectedSection) {
            AdminSection.ELECTIONS -> AdminElectionsTab(store = store)
            AdminSection.CANDIDATES -> AdminCandidatesTab(store = store)
            AdminSection.REGIONS -> AdminRegionsTab(store = store)
            AdminSection.PARTIES -> AdminPartiesTab(store = store)
            AdminSection.PERMISSIONS -> AdminPermissionsTab(store = store)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminElectionsTab(store: AppStoreViewModel) {
    var editingElection by remember { mutableStateOf<ElectionModel?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = {
                    editingElection = ElectionModel(
                        name = "",
                        regionId = store.regions.firstOrNull()?.id ?: "region-national",
                        isActive = true
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Create New Election Poll")
            }
        }

        items(store.elections, key = { it.id }) { election ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingElection = election }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(election.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "ID: ${election.id} • Region: ${store.regionName(election.regionId)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${election.dateStart} – ${election.dateEnd} • ${if (election.isActive) "Active" else "Inactive"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "Candidates (${election.candidates.size}): ${election.candidates.joinToString { it.name }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { store.deleteElection(election) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Election")
                    }
                }
            }
        }
    }

    editingElection?.let { initial ->
        var name by remember(initial) { mutableStateOf(initial.name) }
        var dateStart by remember(initial) { mutableStateOf(initial.dateStart) }
        var dateEnd by remember(initial) { mutableStateOf(initial.dateEnd) }
        var regionId by remember(initial) { mutableStateOf(initial.regionId) }
        var isActive by remember(initial) { mutableStateOf(initial.isActive) }

        AlertDialog(
            onDismissRequest = { editingElection = null },
            title = { Text(if (initial.name.isEmpty()) "New Election" else "Edit Election") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Election Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dateStart,
                        onValueChange = { dateStart = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dateEnd,
                        onValueChange = { dateEnd = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        singleLine = true
                    )
                    Text("Region", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        store.regions.forEach { r ->
                            FilterChip(
                                selected = regionId == r.id,
                                onClick = { regionId = r.id },
                                label = { Text(r.name) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Flag")
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.saveElection(
                            initial.copy(
                                name = name.trim(),
                                dateStart = dateStart.trim(),
                                dateEnd = dateEnd.trim(),
                                regionId = regionId,
                                isActive = isActive
                            )
                        )
                        editingElection = null
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingElection = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminCandidatesTab(store: AppStoreViewModel) {
    var editingCandidate by remember { mutableStateOf<CandidateModel?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = {
                    editingCandidate = CandidateModel(
                        name = "",
                        electionId = store.elections.firstOrNull()?.id ?: "election-2026-general",
                        partyId = store.parties.firstOrNull()?.id ?: "party-civic",
                        isActive = true
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Create New Candidate")
            }
        }

        items(store.candidates, key = { it.id }) { candidate ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingCandidate = candidate }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(candidate.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "ID: ${candidate.id}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Election: ${candidate.electionId} • Party: ${store.partyName(candidate.partyId)} • ${if (candidate.isActive) "Active" else "Inactive"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { store.deleteCandidate(candidate) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Candidate")
                    }
                }
            }
        }
    }

    editingCandidate?.let { initial ->
        var name by remember(initial) { mutableStateOf(initial.name) }
        var electionId by remember(initial) { mutableStateOf(initial.electionId) }
        var partyId by remember(initial) { mutableStateOf(initial.partyId) }
        var isActive by remember(initial) { mutableStateOf(initial.isActive) }

        AlertDialog(
            onDismissRequest = { editingCandidate = null },
            title = { Text(if (initial.name.isEmpty()) "New Candidate" else "Edit Candidate") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Candidate Name") },
                        singleLine = true
                    )
                    Text("Election", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        store.elections.forEach { el ->
                            FilterChip(
                                selected = electionId == el.id,
                                onClick = { electionId = el.id },
                                label = { Text(el.name) }
                            )
                        }
                    }
                    Text("Party", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        store.parties.forEach { p ->
                            FilterChip(
                                selected = partyId == p.id,
                                onClick = { partyId = p.id },
                                label = { Text(p.name) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Flag")
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.saveCandidate(
                            initial.copy(
                                name = name.trim(),
                                electionId = electionId,
                                partyId = partyId,
                                isActive = isActive
                            )
                        )
                        editingCandidate = null
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCandidate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminRegionsTab(store: AppStoreViewModel) {
    var editingRegion by remember { mutableStateOf<RegionModel?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { editingRegion = RegionModel(name = "") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Create New Region")
            }
        }

        items(store.regions, key = { it.id }) { region ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingRegion = region }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(region.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(region.id, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    }
                    IconButton(onClick = { store.deleteRegion(region) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Region")
                    }
                }
            }
        }
    }

    editingRegion?.let { initial ->
        var name by remember(initial) { mutableStateOf(initial.name) }
        AlertDialog(
            onDismissRequest = { editingRegion = null },
            title = { Text(if (initial.name.isEmpty()) "New Region" else "Edit Region") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Region Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.saveRegion(initial.copy(name = name.trim()))
                        editingRegion = null
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRegion = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminPartiesTab(store: AppStoreViewModel) {
    var editingParty by remember { mutableStateOf<PartyModel?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { editingParty = PartyModel(name = "") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Create New Party")
            }
        }

        items(store.parties, key = { it.id }) { party ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { editingParty = party }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(party.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(party.id, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    }
                    IconButton(onClick = { store.deleteParty(party) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Party")
                    }
                }
            }
        }
    }

    editingParty?.let { initial ->
        var name by remember(initial) { mutableStateOf(initial.name) }
        AlertDialog(
            onDismissRequest = { editingParty = null },
            title = { Text(if (initial.name.isEmpty()) "New Party" else "Edit Party") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Party Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.saveParty(initial.copy(name = name.trim()))
                        editingParty = null
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingParty = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminPermissionsTab(store: AppStoreViewModel) {
    var showGrantDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showGrantDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Grant Permission to User")
            }
        }

        items(store.permissions, key = { it.id }) { perm ->
            val userName = store.users.firstOrNull { it.id == perm.userId }?.name ?: perm.userId
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Target: ${perm.resourceType.displayName} • ID: ${perm.resourceId}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Permission Type: ${perm.permissionType.rawValue}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { store.deletePermission(perm) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Permission")
                    }
                }
            }
        }
    }

    if (showGrantDialog) {
        var selectedUserId by remember {
            mutableStateOf(store.currentUser?.id ?: store.users.firstOrNull()?.id.orEmpty())
        }
        var selectedResourceType by remember { mutableStateOf(PermissionResourceType.ELECTION) }
        var selectedPermissionType by remember { mutableStateOf(PermissionType.EDIT) }

        AlertDialog(
            onDismissRequest = { showGrantDialog = false },
            title = { Text("Grant Permission") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User Model", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        store.users.forEach { u ->
                            FilterChip(
                                selected = selectedUserId == u.id,
                                onClick = { selectedUserId = u.id },
                                label = { Text(u.name) }
                            )
                        }
                    }

                    Text("Target Model", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PermissionResourceType.entries.forEach { resType ->
                            FilterChip(
                                selected = selectedResourceType == resType,
                                onClick = { selectedResourceType = resType },
                                label = { Text(resType.displayName) }
                            )
                        }
                    }

                    Text("Permission Type (Read, Edit, Admin)", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PermissionType.entries.forEach { permType ->
                            FilterChip(
                                selected = selectedPermissionType == permType,
                                onClick = { selectedPermissionType = permType },
                                label = { Text(permType.rawValue) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        store.grantPermission(
                            userId = selectedUserId,
                            resourceType = selectedResourceType,
                            resourceId = "all",
                            permissionType = selectedPermissionType
                        )
                        showGrantDialog = false
                    },
                    enabled = selectedUserId.isNotBlank()
                ) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGrantDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
