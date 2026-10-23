import SwiftUI

struct AdminElectionsView: View {
    @EnvironmentObject var store: AppStore
    @State private var editingElection: ElectionModel?
    @State private var showingCreateSheet = false

    var body: some View {
        List {
            Section {
                Button {
                    showingCreateSheet = true
                } label: {
                    Label("Create New Election Poll", systemImage: "plus.circle.fill")
                        .font(.headline)
                }
            }

            Section("Configured Elections (\(store.elections.count))") {
                ForEach(store.elections) { election in
                    Button {
                        editingElection = election
                    } label: {
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text(election.name)
                                    .font(.headline)
                                    .foregroundStyle(.primary)
                                Spacer()
                                Text(election.isActive ? "Active" : "Inactive")
                                    .font(.caption.weight(.semibold))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 2)
                                    .background(election.isActive ? Color.green.opacity(0.15) : Color.secondary.opacity(0.15))
                                    .foregroundStyle(election.isActive ? .green : .secondary)
                                    .clipShape(Capsule())
                            }

                            Text("ID: \(election.id) • Region: \(store.regionName(for: election.regionId))")
                                .font(.caption)
                                .foregroundStyle(.secondary)

                            Text("Dates: \(election.dateStart.formatted(date: .abbreviated, time: .omitted)) – \(election.dateEnd.formatted(date: .abbreviated, time: .omitted))")
                                .font(.caption2)
                                .foregroundStyle(.secondary)

                            Text("Candidates (\(election.candidates.count)): \(election.candidates.map(\.name).joined(separator: ", "))")
                                .font(.caption2)
                                .foregroundStyle(.blue)
                                .lineLimit(2)
                        }
                        .padding(.vertical, 4)
                    }
                }
                .onDelete { offsets in
                    for idx in offsets {
                        store.deleteElection(store.elections[idx])
                    }
                }
            }
        }
        .sheet(isPresented: $showingCreateSheet) {
            ElectionEditorSheet(
                election: ElectionModel(
                    name: "",
                    regionId: store.regions.first?.id ?? "region-national",
                    isActive: true
                ),
                isNew: true
            )
        }
        .sheet(item: $editingElection) { election in
            ElectionEditorSheet(election: election, isNew: false)
        }
    }
}

struct ElectionEditorSheet: View {
    @EnvironmentObject var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State var election: ElectionModel
    let isNew: Bool
    @State private var quickCandidateName: String = ""
    @State private var quickCandidatePartyId: String = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("Election Model Fields") {
                    TextField("Election Name", text: $election.name)
                    TextField("Election ID", text: $election.id)
                        .font(.system(.body, design: .monospaced))
                        .disabled(!isNew)

                    Picker("Region", selection: $election.regionId) {
                        ForEach(store.regions) { region in
                            Text("\(region.name) (\(region.id))").tag(region.id)
                        }
                    }

                    DatePicker("Start Date", selection: $election.dateStart)
                    DatePicker("End Date", selection: $election.dateEnd)
                    Toggle("Active Flag", isOn: $election.isActive)
                }

                Section("Assigned Candidates (\(assignedCandidates.count))") {
                    ForEach(assignedCandidates) { cand in
                        HStack {
                            VStack(alignment: .leading) {
                                Text(cand.name)
                                    .font(.subheadline.weight(.medium))
                                Text("Party: \(store.partyName(for: cand.partyId))")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text(cand.isActive ? "Active" : "Inactive")
                                .font(.caption2)
                                .foregroundStyle(cand.isActive ? .green : .secondary)
                        }
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Add Candidate to Election")
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(.secondary)
                        TextField("Candidate Name", text: $quickCandidateName)
                        Picker("Party", selection: $quickCandidatePartyId) {
                            ForEach(store.parties) { party in
                                Text(party.name).tag(party.id)
                            }
                        }
                        Button("Add Candidate") {
                            let partyId = quickCandidatePartyId.isEmpty ? (store.parties.first?.id ?? "party-ind") : quickCandidatePartyId
                            let newCand = CandidateModel(
                                name: quickCandidateName.trimmingCharacters(in: .whitespacesAndNewlines),
                                electionId: election.id,
                                partyId: partyId,
                                isActive: true
                            )
                            store.saveCandidate(newCand)
                            quickCandidateName = ""
                        }
                        .disabled(quickCandidateName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                }
            }
            .navigationTitle(isNew ? "New Election" : "Edit Election")
            .onAppear {
                if quickCandidatePartyId.isEmpty {
                    quickCandidatePartyId = store.parties.first?.id ?? ""
                }
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        store.saveElection(election)
                        dismiss()
                    }
                    .disabled(election.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }

    private var assignedCandidates: [CandidateModel] {
        store.candidates.filter { $0.electionId == election.id }
    }
}
