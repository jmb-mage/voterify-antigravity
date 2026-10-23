import SwiftUI

struct UserDashboardView: View {
    @EnvironmentObject var store: AppStore
    @State private var selectedRegionId: String = "ALL"

    private var filteredPublicElections: [ElectionModel] {
        store.elections.filter { election in
            election.isActive && (selectedRegionId == "ALL" || election.regionId == selectedRegionId)
        }
    }

    var body: some View {
        List {
            // Anonymous Voter Identity Card
            if let voter = store.currentVoter, let user = store.currentUser {
                Section {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Label("Anonymous Voter Identity", systemImage: "person.badge.key.fill")
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(.blue)
                            Spacer()
                            Text(user.oauthProvider.displayName)
                                .font(.caption2.weight(.semibold))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color.blue.opacity(0.12))
                                .clipShape(Capsule())
                        }

                        Text(voter.publicUniqueName)
                            .font(.system(.caption, design: .monospaced).weight(.semibold))
                            .textSelection(.enabled)

                        Text("Your OAuth profile (\(user.name)) is privately linked and never written to the public vote blockchain.")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.vertical, 4)
                }
            }

            // Region Filter
            Section {
                Picker("Filter by Region", selection: $selectedRegionId) {
                    Text("All Regions").tag("ALL")
                    ForEach(store.regions) { region in
                        Text(region.name).tag(region.id)
                    }
                }
            }

            // Publicly Available Polls / Elections
            Section("Publicly Available Polls & Elections") {
                if filteredPublicElections.isEmpty {
                    ContentUnavailableView(
                        "No Active Elections",
                        systemImage: "tray",
                        description: Text("No active polls match the selected region.")
                    )
                } else {
                    ForEach(filteredPublicElections) { election in
                        NavigationLink {
                            ElectionVoteView(election: election)
                        } label: {
                            ElectionRowView(election: election)
                        }
                    }
                }
            }
        }
        .navigationTitle("Public Elections")
    }
}

struct ElectionRowView: View {
    @EnvironmentObject var store: AppStore
    let election: ElectionModel

    var body: some View {
        let existingVote = store.existingVote(for: election.id)
        let activeCandidatesCount = election.candidates.filter(\.isActive).count

        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(election.name)
                    .font(.headline)
                Spacer()
                if existingVote != nil {
                    Label("Voted", systemImage: "checkmark.seal.fill")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(.green)
                } else {
                    Text("Open")
                        .font(.caption.weight(.semibold))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(Color.green.opacity(0.15))
                        .foregroundStyle(.green)
                        .clipShape(Capsule())
                }
            }

            HStack(spacing: 12) {
                Label(store.regionName(for: election.regionId), systemImage: "mappin.and.ellipse")
                Label("\(activeCandidatesCount) Candidates", systemImage: "person.3.fill")
                Label("\(store.totalVotes(in: election.id)) Votes", systemImage: "cube.fill")
            }
            .font(.caption)
            .foregroundStyle(.secondary)

            Text("\(election.dateStart.formatted(date: .abbreviated, time: .omitted)) – \(election.dateEnd.formatted(date: .abbreviated, time: .omitted))")
                .font(.caption2)
                .foregroundStyle(.tertiary)
        }
        .padding(.vertical, 4)
    }
}
