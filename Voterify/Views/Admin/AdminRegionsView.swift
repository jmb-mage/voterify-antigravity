import SwiftUI

struct AdminRegionsView: View {
    @EnvironmentObject var store: AppStore
    @State private var editingRegion: RegionModel?
    @State private var showingCreateSheet = false

    var body: some View {
        List {
            Section {
                Button {
                    showingCreateSheet = true
                } label: {
                    Label("Create New Region", systemImage: "mappin.and.ellipse")
                        .font(.headline)
                }
            }

            Section("Regions (\(store.regions.count))") {
                ForEach(store.regions) { region in
                    Button {
                        editingRegion = region
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 3) {
                                Text(region.name)
                                    .font(.headline)
                                    .foregroundStyle(.primary)
                                Text("ID: \(region.id)")
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
                        store.deleteRegion(store.regions[idx])
                    }
                }
            }
        }
        .sheet(isPresented: $showingCreateSheet) {
            RegionEditorSheet(region: RegionModel(name: ""), isNew: true)
        }
        .sheet(item: $editingRegion) { region in
            RegionEditorSheet(region: region, isNew: false)
        }
    }
}

struct RegionEditorSheet: View {
    @EnvironmentObject var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State var region: RegionModel
    let isNew: Bool

    var body: some View {
        NavigationStack {
            Form {
                Section("Region Model Fields") {
                    TextField("Region Name", text: $region.name)
                    TextField("Region ID", text: $region.id)
                        .font(.system(.body, design: .monospaced))
                        .disabled(!isNew)
                }
            }
            .navigationTitle(isNew ? "New Region" : "Edit Region")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        store.saveRegion(region)
                        dismiss()
                    }
                    .disabled(region.name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }
}
