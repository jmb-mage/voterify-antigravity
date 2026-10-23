import SwiftUI

struct RootView: View {
    @EnvironmentObject var store: AppStore
    @State private var showingConnectionSheet = false

    var body: some View {
        Group {
            if store.currentUser == nil {
                LoginView()
            } else {
                TabView(selection: $store.activeInterfaceMode) {
                    NavigationStack {
                        UserDashboardView()
                            .toolbar {
                                ToolbarItem(placement: .topBarLeading) {
                                    connectionStatusButton
                                }
                                ToolbarItem(placement: .topBarTrailing) {
                                    userMenu
                                }
                            }
                    }
                    .tabItem {
                        Label("Elections", systemImage: "checkmark.seal.fill")
                    }
                    .tag(AppInterfaceMode.voter)

                    NavigationStack {
                        BlockchainExplorerView()
                            .toolbar {
                                ToolbarItem(placement: .topBarLeading) {
                                    connectionStatusButton
                                }
                                ToolbarItem(placement: .topBarTrailing) {
                                    userMenu
                                }
                            }
                    }
                    .tabItem {
                        Label("Blockchain", systemImage: "cube.transparent.fill")
                    }
                    .tag(AppInterfaceMode.blockchain)

                    NavigationStack {
                        AdminDashboardView()
                            .toolbar {
                                ToolbarItem(placement: .topBarLeading) {
                                    connectionStatusButton
                                }
                                ToolbarItem(placement: .topBarTrailing) {
                                    userMenu
                                }
                            }
                    }
                    .tabItem {
                        Label("Admin", systemImage: "slider.horizontal.3")
                    }
                    .tag(AppInterfaceMode.admin)
                }
                .sheet(isPresented: $showingConnectionSheet) {
                    FirestoreConnectionSheet()
                }
            }
        }
    }

    private var connectionStatusButton: some View {
        Button {
            showingConnectionSheet = true
        } label: {
            HStack(spacing: 4) {
                Image(systemName: "externaldrive.connected.to.line.below.fill")
                    .foregroundStyle(.blue)
                Text(store.firestoreService.config.environment.capitalized)
                    .font(.caption.weight(.semibold))
            }
        }
    }

    private var userMenu: some View {
        Menu {
            if let user = store.currentUser {
                Section("Signed In User") {
                    Text(user.name)
                    Text("OAuth: \(user.oauthProvider.displayName)")
                }
            }
            if let voter = store.currentVoter {
                Section("Anonymous Voter GUID") {
                    Text(voter.publicUniqueName)
                }
            }
            Section("Interface Role") {
                Button {
                    if let user = store.currentUser {
                        store.grantPermission(
                            userId: user.id,
                            resourceType: .global,
                            resourceId: "all",
                            permissionType: .admin
                        )
                        store.activeInterfaceMode = .admin
                    }
                } label: {
                    Label("Grant Admin & Open Admin Console", systemImage: "key.fill")
                }

                Button(role: .destructive) {
                    store.signOut()
                } label: {
                    Label("Sign Out", systemImage: "rectangle.portrait.and.arrow.right")
                }
            }
        } label: {
            Image(systemName: "person.crop.circle.badge.checkmark")
                .imageScale(.large)
        }
    }
}

struct FirestoreConnectionSheet: View {
    @EnvironmentObject var store: AppStore
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        let cfg = store.firestoreService.config
        NavigationStack {
            List {
                Section("Xcode .xcconfig Firestore Connection") {
                    LabeledContent("Environment", value: cfg.environment)
                    LabeledContent("Project ID", value: cfg.projectId)
                    LabeledContent("Database ID", value: cfg.databaseId)
                    LabeledContent("Host", value: cfg.host)
                    LabeledContent("SSL Enabled", value: cfg.useSSL ? "YES" : "NO")
                    LabeledContent("Emulator Mode", value: cfg.useEmulator ? "YES" : "NO")
                    LabeledContent("REST Base URL", value: cfg.baseDocumentsURLString)
                        .font(.caption2)
                }

                Section("Configured Firestore Collections") {
                    LabeledContent("Users", value: cfg.collections.users)
                    LabeledContent("Voters (Anon)", value: cfg.collections.voters)
                    LabeledContent("Elections", value: cfg.collections.elections)
                    LabeledContent("Candidates", value: cfg.collections.candidates)
                    LabeledContent("Regions", value: cfg.collections.regions)
                    LabeledContent("Parties", value: cfg.collections.parties)
                    LabeledContent("Permissions", value: cfg.collections.permissions)
                    LabeledContent("Votes", value: cfg.collections.votes)
                    LabeledContent("Blockchain", value: cfg.collections.blockchain)
                }

                if let user = store.currentUser, let voter = store.currentVoter {
                    Section("Anonymous Voter Separation") {
                        LabeledContent("User Model ID", value: user.id)
                        LabeledContent("OAuth Provider", value: user.oauthProvider.displayName)
                        LabeledContent("Voter Public Name (GUID)", value: voter.publicUniqueName)
                        LabeledContent("Voter Private Link", value: voter.privateUserId)
                    }
                }
            }
            .navigationTitle("Database & Config")
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
