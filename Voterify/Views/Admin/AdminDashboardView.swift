import SwiftUI

enum AdminManagementSection: String, CaseIterable, Identifiable {
    case elections = "Elections"
    case candidates = "Candidates"
    case regions = "Regions"
    case parties = "Parties"
    case permissions = "Permissions"

    var id: String { rawValue }
}

struct AdminDashboardView: View {
    @EnvironmentObject var store: AppStore
    @State private var selectedSection: AdminManagementSection = .elections

    var body: some View {
        VStack(spacing: 0) {
            // Permission Status Banner
            if let user = store.currentUser {
                let globalLevel = store.effectivePermission(for: user.id, resourceType: .global)
                HStack {
                    Image(systemName: store.currentUserCanAccessAdminConsole ? "lock.open.fill" : "eye.fill")
                        .foregroundStyle(store.currentUserCanAccessAdminConsole ? .green : .orange)
                    Text("User: \(user.name) • Permission: \(globalLevel.rawValue)")
                        .font(.caption.weight(.medium))
                    Spacer()
                    if !store.currentUserCanAccessAdminConsole {
                        Button("Elevate to Admin") {
                            store.grantPermission(
                                userId: user.id,
                                resourceType: .global,
                                resourceId: "all",
                                permissionType: .admin
                            )
                        }
                        .font(.caption.weight(.semibold))
                        .buttonStyle(.borderedProminent)
                        .controlSize(.mini)
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
                .background(Color(.secondarySystemBackground))
            }

            // Section Picker
            Picker("Admin Section", selection: $selectedSection) {
                ForEach(AdminManagementSection.allCases) { section in
                    Text(section.rawValue).tag(section)
                }
            }
            .pickerStyle(.segmented)
            .padding()

            // Active Management View
            Group {
                switch selectedSection {
                case .elections:
                    AdminElectionsView()
                case .candidates:
                    AdminCandidatesView()
                case .regions:
                    AdminRegionsView()
                case .parties:
                    AdminPartiesView()
                case .permissions:
                    AdminPermissionsView()
                }
            }
        }
        .navigationTitle("Admin Console")
    }
}
