import SwiftUI

struct AdminCandidatesView: View {
    @EnvironmentObject var store: AppStore
    @State private var editingCandidate: CandidateModel?
    @State private var showingCreateSheet = false

    var body: some View {
        List {
            Section {
                Button {
                    showingCreateSheet = true
                } label: {
                    Label("Create New Candidate", systemImage: "person.badge.plus")
                        .font(.headline)
                }
            }

            Section("Candidates (\(store.candidates.count))") {
                ForEach(store.candidates) { candidate in
                    Button {
                        editingCandidate = candidate
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(candidate.name)
                                    .font(.headline)
                                    .foregroundStyle(.primary)
                                Text("ID: \(candidate.id)")
                                    .font(.caption2.monospaced())
                                    .foregroundStyle(.secondary)
                                Text("Election: \(electionName(for: candidate.electionId)) • Party: \(store.partyName(for: candidate.partyId))")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text(candidate.isActive ? "Active" : "Inactive")
                                .font(.caption.weight(.semibold))
                                .foregroundStyle(candidate.isActive ? .green : .secondary)
                        }
                        .padding(.vertical, 3)
                    }
                }
                .onDelete { offsets in
                    for idx in offsets {
                        store.deleteCandidate(store.candidates[idx])
                    }
                }
            }
        }
        .sheet(isPresented: $showingCreateSheet) {
            CandidateEditorSheet(
                candidate: CandidateModel(
                    name: "",
                    electionId: store.elections.first?.id ?? "election-2026-general",
                    partyId: store.parties.first?.id ?? "party-civic",
                    isActive: true
                ),
                isNew: true
            )
        }
        .sheet(item: $editingCandidate) { candidate in
            CandidateEditorSheet(candidate: candidate, isNew: false)
        }
    }

    private func electionName(for id: String) -> String {
        store.elections.first(where: { $0.id == id })?.name ?? id
    }
}

struct CandidateEditorSheet: View {
    @EnvironmentObject var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State var candidate: CandidateModel
    let isNew: Bool

    var body: some View {
        NavigationStack {
            Form {
                Section("Candidate Model Fields") {
                    TextField("Candidate Name", text: $candidate.name)
                    TextField("Candidate ID", text: $candidate.id)
                        .font(.system(.body, design: .monospaced))
                        .disabled(!isNew)

                    Picker("Election", selection: $candidate.electionId) {
                        ForEach(store.elections) { election in
                            Text("\(election.name) (\(election.id))").tag(election.id)
                        }
                    }

                    Picker("Party", selection: $candidate.partyId) {
                        ForEach(store.parties) { party in
                            Text("\(party.name) (\(party.id))").tag(party.id)
                        }
                    }

                    Toggle("Active Flag", isOn: $candidate.isActive)
                }
            }
            .navigationTitle(isNew ? "New Candidate" : "Edit Candidate")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        store.saveCandidate(candidate)
                        dismiss()
                    }
                    .disabled(candidate.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
}
