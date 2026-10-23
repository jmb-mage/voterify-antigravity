import SwiftUI

struct AdminPermissionsView: View {
    @EnvironmentObject var store: AppStore
    @State private var showingGrantSheet = false

    var body: some View {
        List {
            Section {
                Button {
                    showingGrantSheet = true
                } label: {
                    Label("Grant Permission to User", systemImage: "key.fill")
                        .font(.headline)
                }
            }

            Section("Active Permission Grants (\(store.permissions.count))") {
                ForEach(store.permissions) { perm in
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(userName(for: perm.userId))
                                .font(.headline)
                            Text("Target: \(perm.resourceType.displayName) • ID: \(perm.resourceId)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                        Text(perm.permissionType.rawValue)
                            .font(.caption.weight(.bold))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(badgeColor(for: perm.permissionType).opacity(0.15))
                            .foregroundStyle(badgeColor(for: perm.permissionType))
                            .clipShape(Capsule())
                    }
                    .padding(.vertical, 3)
                }
                .onDelete { offsets in
                    for idx in offsets {
                        store.deletePermission(store.permissions[idx])
                    }
                }
            }
        }
        .sheet(isPresented: $showingGrantSheet) {
            GrantPermissionSheet()
        }
    }

    private func userName(for userId: String) -> String {
        store.users.first(where: { $0.id == userId })?.name ?? userId
    }

    private func badgeColor(for type: PermissionType) -> Color {
        switch type {
        case .read: return .blue
        case .edit: return .orange
        case .admin: return .purple
        }
    }
}

struct GrantPermissionSheet: View {
    @EnvironmentObject var store: AppStore
    @Environment(\.dismiss) private var dismiss

    @State private var selectedUserId: String = ""
    @State private var selectedResourceType: PermissionResourceType = .election
    @State private var selectedResourceId: String = "all"
    @State private var selectedPermissionType: PermissionType = .edit

    var body: some View {
        NavigationStack {
            Form {
                Section("User Model") {
                    Picker("User", selection: $selectedUserId) {
                        ForEach(store.users) { user in
                            Text("\(user.name) (\(user.oauthProvider.displayName))").tag(user.id)
                        }
                    }
                }

                Section("Target Model") {
                    Picker("Model Type", selection: $selectedResourceType) {
                        ForEach(PermissionResourceType.allCases) { type in
                            Text(type.displayName).tag(type)
                        }
                    }
                    .onChange(of: selectedResourceType) { _, _ in
                        selectedResourceId = availableResourceOptions.first?.id ?? "all"
                    }

                    Picker("Model Instance", selection: $selectedResourceId) {
                        ForEach(availableResourceOptions, id: \.id) { item in
                            Text(item.label).tag(item.id)
                        }
                    }
                }

                Section("Permission Type Model") {
                    Picker("Permission Type", selection: $selectedPermissionType) {
                        ForEach(PermissionType.allCases) { permType in
                            Text(permType.rawValue).tag(permType)
                        }
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Grant Permission")
            .onAppear {
                if selectedUserId.isEmpty {
                    selectedUserId = store.currentUser?.id ?? store.users.first?.id ?? ""
                }
                selectedResourceId = availableResourceOptions.first?.id ?? "all"
            }
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Grant") {
                        store.grantPermission(
                            userId: selectedUserId,
                            resourceType: selectedResourceType,
                            resourceId: selectedResourceId,
                            permissionType: selectedPermissionType
                        )
                        dismiss()
                    }
                    .disabled(selectedUserId.isEmpty)
                }
            }
        }
    }

    private var availableResourceOptions: [(id: String, label: String)] {
        var options: [(id: String, label: String)] = [("all", "All \(selectedResourceType.displayName) Records")]
        switch selectedResourceType {
        case .election:
            options += store.elections.map { ($0.id, $0.name) }
        case .candidate:
            options += store.candidates.map { ($0.id, $0.name) }
        case .region:
            options += store.regions.map { ($0.id, $0.name) }
        case .party:
            options += store.parties.map { ($0.id, $0.name) }
        case .global:
            break
        }
        return options
    }
}
