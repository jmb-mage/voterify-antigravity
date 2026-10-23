package com.voterify.app.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import com.voterify.app.models.ElectionModel
import com.voterify.app.models.VoteModel
import com.voterify.app.services.AppStoreViewModel

@Composable
fun ElectionVoteScreen(
    store: AppStoreViewModel,
    election: ElectionModel,
    onBack: () -> Unit
) {
    var selectedCandidateId by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val activeCandidates = store.candidates.filter { it.electionId == election.id && it.isActive }
    val userVote = store.existingVote(election.id)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedButton(onClick = onBack) {
                Text("← Back to Elections")
            }
        }

        // Election Details Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = election.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Region: ${store.regionName(election.regionId)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Dates: ${election.dateStart} to ${election.dateEnd}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Ballot Candidates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(activeCandidates, key = { it.id }) { candidate ->
            val votesForCandidate = store.voteCount(candidate.id, election.id)
            val isSelected = (selectedCandidateId == candidate.id) || (userVote?.candidateId == candidate.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = userVote == null) {
                        selectedCandidateId = candidate.id
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (userVote == null) selectedCandidateId = candidate.id
                            },
                            enabled = userVote == null
                        )
                        Column {
                            Text(
                                text = candidate.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = store.partyName(candidate.partyId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$votesForCandidate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "blockchain votes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Cast Vote Button or Public Blockchain Receipt
        item {
            if (userVote != null) {
                val block = store.blockchainBlock(userVote)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Vote Recorded on Public Blockchain ✓",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Candidate: ${userVote.candidateName}")
                        Text("Status Flag: ${userVote.statusFlag.rawValue}")
                        Text("Timestamp: ${userVote.date}", style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider()
                        Text(
                            text = "Voter Anonymous GUID",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = userVote.voterId,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Composite Blockchain Key (Voter ID + Vote)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = userVote.blockchainKey,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                        if (block != null) {
                            HorizontalDivider()
                            Text("Block #${block.index} • Nonce: ${block.nonce}", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Block SHA-256 Hash:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = block.blockHash,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Previous Block Hash:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = block.previousHash,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = selectedCandidateId != null && election.isActive,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cast Vote to Public Blockchain")
                    }

                    val voter = store.currentVoter
                    val candId = selectedCandidateId
                    if (voter != null && candId != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Preview Blockchain Key: ${
                                VoteModel.makeBlockchainKey(
                                    voterId = voter.publicUniqueName,
                                    electionId = election.id,
                                    candidateId = candId
                                )
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        val candidate = activeCandidates.firstOrNull { it.id == selectedCandidateId }
        if (candidate != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirm Anonymous Blockchain Vote") },
                text = {
                    Text("Cast your vote for ${candidate.name}? Once recorded on the public blockchain ledger, your vote is permanent and publicly auditable using your anonymous Voter GUID.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            store.castVote(election = election, candidate = candidate)
                            showConfirmDialog = false
                        }
                    ) {
                        Text("Confirm Vote")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
