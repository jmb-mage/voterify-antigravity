package com.voterify.app.ui.user

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.voterify.app.services.AppStoreViewModel
import com.voterify.app.services.BlockchainVerificationReport

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlockchainExplorerScreen(store: AppStoreViewModel) {
    var searchText by remember { mutableStateOf("") }
    var selectedElectionId by remember { mutableStateOf("ALL") }
    var verificationReport by remember { mutableStateOf<BlockchainVerificationReport?>(null) }

    val report = verificationReport ?: store.verifyBlockchainIntegrity()

    val filteredBlocks = store.blockchainLedger
        .sortedByDescending { it.index }
        .filter { block ->
            val matchesElection = selectedElectionId == "ALL" ||
                block.electionId == selectedElectionId ||
                block.electionId == "GENESIS"
            val q = searchText.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                block.blockKey.lowercase().contains(q) ||
                block.voterId.lowercase().contains(q) ||
                block.blockHash.lowercase().contains(q) ||
                block.vote.candidateName.lowercase().contains(q)
            matchesElection && matchesSearch
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (report.isValid) {
                                "SHA-256 Chain Verified (${report.totalBlocks} Blocks)"
                            } else {
                                "Chain Integrity Alert"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Every block is keyed by [Voter GUID :: Election ID :: Candidate ID] and chained via SHA-256.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = { verificationReport = store.verifyBlockchainIntegrity() }) {
                        Text("Verify")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search by Voter GUID, Block Key, or Hash") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedElectionId == "ALL",
                    onClick = { selectedElectionId = "ALL" },
                    label = { Text("All Elections") }
                )
                store.elections.forEach { election ->
                    FilterChip(
                        selected = selectedElectionId == election.id,
                        onClick = { selectedElectionId = election.id },
                        label = { Text(election.name) }
                    )
                }
            }
        }

        items(filteredBlocks, key = { it.id }) { block ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Block #${block.index}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = block.vote.statusFlag.rawValue,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (block.electionId == "GENESIS") {
                        Text(
                            text = "Genesis Ledger Initialization Block",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text("Candidate Vote: ${block.vote.candidateName}", fontWeight = FontWeight.Medium)
                        Text(
                            text = "Election ID: ${block.electionId}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "Blockchain Key (Voter ID + Vote):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = block.blockKey,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Block SHA-256 Hash (Nonce: ${block.nonce}):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = block.blockHash,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Previous Block Hash:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = block.previousHash,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
