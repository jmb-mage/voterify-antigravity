package com.voterify.app.ui.user

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.voterify.app.services.AppStoreViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserDashboardScreen(
    store: AppStoreViewModel,
    onSelectElection: (ElectionModel) -> Unit
) {
    var selectedRegionId by remember { mutableStateOf("ALL") }

    val filteredElections = store.elections.filter { election ->
        election.isActive && (selectedRegionId == "ALL" || election.regionId == selectedRegionId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Anonymous Voter Identity Card
        item {
            val voter = store.currentVoter
            val user = store.currentUser
            if (voter != null && user != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Anonymous Voter Identity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(user.oauthProvider.displayName) }
                            )
                        }
                        Text(
                            text = voter.publicUniqueName,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Your OAuth profile (${user.name}) is privately linked and never written to the public vote blockchain.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Region Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Filter by Region",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedRegionId == "ALL",
                        onClick = { selectedRegionId = "ALL" },
                        label = { Text("All Regions") }
                    )
                    store.regions.forEach { region ->
                        FilterChip(
                            selected = selectedRegionId == region.id,
                            onClick = { selectedRegionId = region.id },
                            label = { Text(region.name) }
                        )
                    }
                }
            }
        }

        // Publicly Available Polls & Elections
        item {
            Text(
                text = "Publicly Available Polls & Elections (${filteredElections.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(filteredElections, key = { it.id }) { election ->
            val existingVote = store.existingVote(election.id)
            val activeCandidatesCount = election.candidates.count { it.isActive }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectElection(election) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = election.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (existingVote != null) "Voted ✓" else "Open",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (existingVote != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Region: ${store.regionName(election.regionId)} • $activeCandidatesCount Candidates • ${store.totalVotes(election.id)} Votes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${election.dateStart} – ${election.dateEnd}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
