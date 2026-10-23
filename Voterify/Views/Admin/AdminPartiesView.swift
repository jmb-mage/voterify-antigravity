import SwiftUI

struct AdminPartiesView: View {
    @EnvironmentObject var store: AppStore
    @State private var editingParty: PartyModel?
    @State private var showingCreateSheet = false

    var body: some View {
        List {
            Section {
                Button {
                    showingCreateSheet = true
                } label: {
                    Label("Create New Party", systemImage: "flag.2.crossed.fill")
                        .font(.headline)
                }
            }

            Section("Parties (\(store.parties.count))") {
                ForEach(store.parties) { party in
                    Button {
                        editingParty = party
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 3) {
                                Text(party.name)
                                    .font(.headline)
                                    .foregroundStyle(.primary)
                                Text("ID: \(party.id)")
                                    .font(.caption.monospaced())
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Image(systemName: "pencil")
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .onDelete { offsets in
                    for idx in offsets {
                        store.deleteParty(store.parties[idx])
                    }
                }
            }
        }
        .sheet(isPresented: $showingCreateSheet) {
            PartyEditorSheet(party: PartyModel(name: ""), isNew: true)
        }
        .sheet(item: $editingParty) { party in
            PartyEditorSheet(party: party, isNew: false)
        }
    }
}

struct PartyEditorSheet: View {
    @EnvironmentObject var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State var party: PartyModel
    let isNew: Bool

    var body: some View {
        NavigationStack {
            Form {
                Section("Party Model Fields") {
                    TextField("Party Name", text: $party.name)
                    TextField("Party ID", text: $party.id)
                        .font(.system(.body, design: .monospaced))
                        .disabled(!isNew)
                }
            }
            .navigationTitle(isNew ? "New Party" : "Edit Party")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        store.saveParty(party)
                        dismiss()
                    }
                    .disabled(party.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
}
