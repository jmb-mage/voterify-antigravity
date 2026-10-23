import SwiftUI

struct BlockchainExplorerView: View {
    @EnvironmentObject var store: AppStore
    @State private var searchText: String = ""
    @State private var selectedElectionId: String = "ALL"
    @State private var verificationReport: BlockchainVerificationReport?

    private var filteredBlocks: [BlockchainBlockModel] {
        store.blockchainLedger
            .sorted { $0.index > $1.index }
            .filter { block in
                let matchesElection = selectedElectionId == "ALL" || block.electionId == selectedElectionId || block.electionId == "GENESIS"
                let matchesSearch: Bool
                if searchText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    matchesSearch = true
                } else {
                    let q = searchText.lowercased()
                    matchesSearch = block.blockKey.lowercased().contains(q)
                        || block.voterId.lowercased().contains(q)
                        || block.blockHash.lowercased().contains(q)
                        || block.vote.candidateName.lowercased().contains(q)
                }
                return matchesElection && matchesSearch
            }
    }

    var body: some View {
        List {
            // Cryptographic Chain Verification Banner
            Section("Blockchain Integrity & Verification") {
                let report = verificationReport ?? store.verifyBlockchainIntegrity()
                HStack(spacing: 12) {
                    Image(systemName: report.isValid ? "checkmark.shield.fill" : "exclamationmark.triangle.fill")
                        .font(.title2)
                        .foregroundStyle(report.isValid ? .green : .red)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(report.isValid ? "SHA-256 Chain Verified (\(report.totalBlocks) Blocks)" : "Chain Integrity Alert")
                            .font(.subheadline.weight(.semibold))
                        Text("Every block is keyed by [Voter GUID :: Election ID :: Candidate ID] and chained via SHA-256.")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    Button("Verify") {
                        verificationReport = store.verifyBlockchainIntegrity()
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.small)
                }
            }

            // Filter by Election
            Section {
                Picker("Election Filter", selection: $selectedElectionId) {
                    Text("All Elections (Full Chain)").tag("ALL")
                    ForEach(store.elections) { election in
                        Text(election.name).tag(election.id)
                    }
                }
            }

            // Public Ledger Blocks
            Section("Public Blockchain Ledger (\(filteredBlocks.count) Blocks)") {
                ForEach(filteredBlocks) { block in
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text("Block #\(block.index)")
                                .font(.headline.monospacedDigit())
                            Spacer()
                            Text(block.vote.statusFlag.rawValue)
                                .font(.caption2.weight(.semibold))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(Color.green.opacity(0.15))
                                .foregroundStyle(.green)
                                .clipShape(Capsule())
                        }

                        if block.electionId == "GENESIS" {
                            Text("Genesis Ledger Initialization Block")
                                .font(.subheadline.weight(.medium))
                                .foregroundStyle(.secondary)
                        } else {
                            LabeledContent("Candidate Vote", value: block.vote.candidateName)
                                .font(.subheadline)
                            LabeledContent("Election ID", value: block.electionId)
                                .font(.caption)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Blockchain Key (Voter ID + Vote)")
                                .font(.caption2.weight(.semibold))
                                .foregroundStyle(.secondary)
                            Text(block.blockKey)
                                .font(.system(.caption2, design: .monospaced))
                                .foregroundStyle(.blue)
                                .textSelection(.enabled)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Block SHA-256 Hash (Nonce: \(block.nonce))")
                                .font(.caption2.weight(.semibold))
                                .foregroundStyle(.secondary)
                            Text(block.blockHash)
                                .font(.system(.caption2, design: .monospaced))
                                .textSelection(.enabled)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text("Previous Block Hash")
                                .font(.caption2.weight(.semibold))
                                .foregroundStyle(.secondary)
                            Text(block.previousHash)
                                .font(.system(.caption2, design: .monospaced))
                                .foregroundStyle(.secondary)
                                .textSelection(.enabled)
                        }

                        Text(block.timestamp.formatted(date: .abbreviated, time: .standard))
                            .font(.caption2)
                            .foregroundStyle(.tertiary)
                    }
                    .padding(.vertical, 6)
                }
            }
        }
        .navigationTitle("Public Blockchain")
        .searchable(text: $searchText, prompt: "Search by Voter GUID, Block Key, or Hash")
    }
}
