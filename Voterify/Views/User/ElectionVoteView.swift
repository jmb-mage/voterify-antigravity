import SwiftUI

struct ElectionVoteView: View {
    @EnvironmentObject var store: AppStore
    let election: ElectionModel

    @State private var selectedCandidateId: String?
    @State private var showingConfirmation = false

    private var activeCandidates: [CandidateModel] {
        store.candidates.filter { $0.electionId == election.id && $0.isActive }
    }

    private var userVote: VoteModel? {
        store.existingVote(for: election.id)
    }

    var body: some View {
        List {
            // Election Info Header
            Section("Election Details") {
                LabeledContent("Poll Name", value: election.name)
                LabeledContent("Region", value: store.regionName(for: election.regionId))
                LabeledContent("Starts", value: election.dateStart.formatted(date: .abbreviated, time: .shortened))
                LabeledContent("Ends", value: election.dateEnd.formatted(date: .abbreviated, time: .shortened))
                LabeledContent("Status", value: election.isActive ? "Active" : "Inactive")
            }

            // Ballot / Candidate Selection
            Section("Ballot Candidates") {
                if activeCandidates.isEmpty {
                    Text("No active candidates registered for this election.")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(activeCandidates) { candidate in
                        let votesForCandidate = store.voteCount(for: candidate.id, in: election.id)
                        let isSelected = (selectedCandidateId == candidate.id) || (userVote?.candidateId == candidate.id)

                        Button {
                            if userVote == nil {
                                selectedCandidateId = candidate.id
                            }
                        } label: {
                            HStack {
                                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                    .foregroundStyle(isSelected ? .blue : .secondary)
                                    .font(.title3)

                                VStack(alignment: .leading, spacing: 3) {
                                    Text(candidate.name)
                                        .font(.headline)
                                        .foregroundStyle(.primary)
                                    Text(store.partyName(for: candidate.partyId))
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }

                                Spacer()

                                VStack(alignment: .trailing, spacing: 2) {
                                    Text("\(votesForCandidate)")
                                        .font(.subheadline.monospacedDigit().weight(.bold))
                                        .foregroundStyle(.primary)
                                    Text("blockchain votes")
                                        .font(.caption2)
                                        .foregroundStyle(.secondary)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                        .disabled(userVote != nil)
                    }
                }
            }

            // Submit Vote Button or Blockchain Receipt
            if let recordedVote = userVote {
                Section("Public Blockchain Vote Receipt") {
                    VStack(alignment: .leading, spacing: 8) {
                        Label("Vote Recorded on Public Blockchain", systemImage: "checkmark.seal.fill")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(.green)

                        LabeledContent("Candidate", value: recordedVote.candidateName)
                        LabeledContent("Status Flag", value: recordedVote.statusFlag.rawValue)
                        LabeledContent("Timestamp", value: recordedVote.date.formatted(date: .abbreviated, time: .standard))

                        Divider()

                        Text("Voter Anonymous GUID")
                            .font(.caption.weight(.semibold))
                        Text(recordedVote.voterId)
                            .font(.system(.caption2, design: .monospaced))
                            .textSelection(.enabled)

                        Text("Composite Blockchain Key (Voter ID + Vote)")
                            .font(.caption.weight(.semibold))
                        Text(recordedVote.blockchainKey)
                            .font(.system(.caption2, design: .monospaced))
                            .foregroundStyle(.blue)
                            .textSelection(.enabled)

                        if let block = store.blockchainBlock(for: recordedVote) {
                            Divider()
                            LabeledContent("Block Index", value: "#\(block.index)")
                            LabeledContent("Nonce", value: "\(block.nonce)")

                            Text("Block SHA-256 Hash")
                                .font(.caption.weight(.semibold))
                            Text(block.blockHash)
                                .font(.system(.caption2, design: .monospaced))
                                .textSelection(.enabled)

                            Text("Previous Block Hash")
                                .font(.caption.weight(.semibold))
                            Text(block.previousHash)
                                .font(.system(.caption2, design: .monospaced))
                                .foregroundStyle(.secondary)
                                .textSelection(.enabled)
                        }
                    }
                    .padding(.vertical, 4)
                }
            } else {
                Section {
                    Button {
                        showingConfirmation = true
                    } label: {
                        HStack {
                            Spacer()
                            Label("Cast Vote to Public Blockchain", systemImage: "cube.transparent.fill")
                                .font(.headline)
                            Spacer()
                        }
                    }
                    .disabled(selectedCandidateId == nil || !election.isActive)
                } footer: {
                    if let voter = store.currentVoter, let candId = selectedCandidateId {
                        Text("Preview Blockchain Key: \(VoteModel.makeBlockchainKey(voterId: voter.publicUniqueName, electionId: election.id, candidateId: candId))")
                            .font(.system(.caption2, design: .monospaced))
                    }
                }
            }
        }
        .navigationTitle(election.name)
        .navigationBarTitleDisplayMode(.inline)
        .confirmationDialog(
            "Confirm Anonymous Blockchain Vote",
            isPresented: $showingConfirmation,
            titleVisibility: .visible
        ) {
            if let candId = selectedCandidateId,
               let candidate = activeCandidates.first(where: { $0.id == candId }) {
                Button("Cast Vote for \(candidate.name)") {
                    _ = store.castVote(election: election, candidate: candidate)
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Once recorded on the public blockchain ledger, your vote is permanent and publicly auditable using your anonymous Voter GUID.")
        }
    }
}
