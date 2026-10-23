import Combine
import Foundation

/// Active interface mode in Voterify.
enum AppInterfaceMode: String, CaseIterable, Identifiable {
    case voter = "Voter Portal"
    case blockchain = "Public Blockchain"
    case admin = "Admin Console"

    var id: String { rawValue }
}

/// Primary state store managing Firestore models, OAuth sessions, RBAC permissions, and the public vote blockchain.
@MainActor
final class AppStore: ObservableObject {
    // MARK: - Authenticated State
    @Published var currentUser: UserModel?
    @Published var currentVoter: VoterModel?
    @Published var activeInterfaceMode: AppInterfaceMode = .voter
    @Published var lastFirestoreSyncStatus: String = "Connected (Local + Firestore Config)"

    // MARK: - Core Data Models
    @Published var users: [UserModel] = []
    @Published var voters: [VoterModel] = []
    @Published var regions: [RegionModel] = []
    @Published var parties: [PartyModel] = []
    @Published var candidates: [CandidateModel] = []
    @Published var elections: [ElectionModel] = []
    @Published var permissions: [PermissionModel] = []
    @Published var votes: [VoteModel] = []
    @Published var blockchainLedger: [BlockchainBlockModel] = []

    // MARK: - Services
    let firestoreService: FirestoreService
    let blockchainService: BlockchainService
    let oauthService: OAuthService

    init(
        firestoreService: FirestoreService = FirestoreService(),
        blockchainService: BlockchainService = BlockchainService(),
        oauthService: OAuthService = OAuthService()
    ) {
        self.firestoreService = firestoreService
        self.blockchainService = blockchainService
        self.oauthService = oauthService
        loadOrSeedInitialData()
    }

    // MARK: - Initial Load & Seed

    func loadOrSeedInitialData() {
        let cols = firestoreService.config.collections

        if let savedRegions = firestoreService.loadCollection(cols.regions, as: [RegionModel].self),
           !savedRegions.isEmpty {
            self.regions = savedRegions
            self.parties = firestoreService.loadCollection(cols.parties, as: [PartyModel].self) ?? []
            self.candidates = firestoreService.loadCollection(cols.candidates, as: [CandidateModel].self) ?? []
            self.elections = firestoreService.loadCollection(cols.elections, as: [ElectionModel].self) ?? []
            self.users = firestoreService.loadCollection(cols.users, as: [UserModel].self) ?? []
            self.voters = firestoreService.loadCollection(cols.voters, as: [VoterModel].self) ?? []
            self.permissions = firestoreService.loadCollection(cols.permissions, as: [PermissionModel].self) ?? []
            self.votes = firestoreService.loadCollection(cols.votes, as: [VoteModel].self) ?? []
            self.blockchainLedger = firestoreService.loadCollection(cols.blockchain, as: [BlockchainBlockModel].self) ?? [blockchainService.makeGenesisBlock()]
            return
        }

        // Seed initial data matching Database/FirestoreSchema/seed_data.json
        let seededRegions = [
            RegionModel(id: "region-national", name: "National District"),
            RegionModel(id: "region-north", name: "Northern Metro Region"),
            RegionModel(id: "region-pacific", name: "Pacific Coastal Region")
        ]

        let seededParties = [
            PartyModel(id: "party-civic", name: "Civic Alliance Party"),
            PartyModel(id: "party-future", name: "Future Horizon Party"),
            PartyModel(id: "party-ind", name: "Independent Coalition")
        ]

        let generalCandidates = [
            CandidateModel(id: "cand-elena-rostova", name: "Elena Rostova", electionId: "election-2026-general", partyId: "party-civic", isActive: true),
            CandidateModel(id: "cand-marcus-vance", name: "Marcus Vance", electionId: "election-2026-general", partyId: "party-future", isActive: true),
            CandidateModel(id: "cand-priya-nair", name: "Priya Nair", electionId: "election-2026-general", partyId: "party-ind", isActive: true)
        ]

        let transitCandidates = [
            CandidateModel(id: "cand-david-chen", name: "David Chen", electionId: "election-2026-transit", partyId: "party-civic", isActive: true),
            CandidateModel(id: "cand-sarah-jenkins", name: "Sarah Jenkins", electionId: "election-2026-transit", partyId: "party-future", isActive: true)
        ]

        let now = Date()
        let start = Calendar.current.date(byAdding: .day, value: -5, to: now) ?? now
        let end = Calendar.current.date(byAdding: .day, value: 30, to: now) ?? now

        let seededElections = [
            ElectionModel(
                id: "election-2026-general",
                name: "2026 National Representative Poll",
                dateStart: start,
                dateEnd: end,
                regionId: "region-national",
                isActive: true,
                candidates: generalCandidates
            ),
            ElectionModel(
                id: "election-2026-transit",
                name: "Northern Metro Clean Transit Commissioner",
                dateStart: start,
                dateEnd: end,
                regionId: "region-north",
                isActive: true,
                candidates: transitCandidates
            )
        ]

        let adminUser = UserModel(
            id: "user-admin-001",
            name: "Alex Morgan (Admin)",
            email: "admin@voterify.org",
            oauthProvider: .apple
        )
        let sampleVoterUser = UserModel(
            id: "user-voter-001",
            name: "Jordan Taylor",
            email: "jordan@example.com",
            oauthProvider: .google
        )

        let adminVoter = VoterModel(
            id: "550E8400-E29B-41D4-A716-446655440000",
            publicUniqueName: "VOTER-550E8400-E29B-41D4-A716-446655440000",
            privateUserId: adminUser.id
        )
        let sampleVoter = VoterModel(
            id: "7C9E6679-7425-40DE-944B-E07FC1F90AE7",
            publicUniqueName: "VOTER-7C9E6679-7425-40DE-944B-E07FC1F90AE7",
            privateUserId: sampleVoterUser.id
        )

        let seededPermissions = [
            PermissionModel(
                userId: adminUser.id,
                resourceType: .global,
                resourceId: "all",
                permissionType: .admin
            ),
            PermissionModel(
                userId: sampleVoterUser.id,
                resourceType: .election,
                resourceId: "election-2026-general",
                permissionType: .read
            )
        ]

        let genesisBlock = blockchainService.makeGenesisBlock()

        self.regions = seededRegions
        self.parties = seededParties
        self.candidates = generalCandidates + transitCandidates
        self.elections = seededElections
        self.users = [adminUser, sampleVoterUser]
        self.voters = [adminVoter, sampleVoter]
        self.permissions = seededPermissions
        self.votes = []
        self.blockchainLedger = [genesisBlock]

        persistAllCollections()
    }

    private func persistAllCollections() {
        let cols = firestoreService.config.collections
        firestoreService.saveCollection(regions, to: cols.regions)
        firestoreService.saveCollection(parties, to: cols.parties)
        firestoreService.saveCollection(candidates, to: cols.candidates)
        firestoreService.saveCollection(elections, to: cols.elections)
        firestoreService.saveCollection(users, to: cols.users)
        firestoreService.saveCollection(voters, to: cols.voters)
        firestoreService.saveCollection(permissions, to: cols.permissions)
        firestoreService.saveCollection(votes, to: cols.votes)
        firestoreService.saveCollection(blockchainLedger, to: cols.blockchain)
    }

    // MARK: - OAuth Login & Session Management

    func loginWithOAuth(
        provider: OAuthProvider,
        customDisplayName: String? = nil,
        asAdminPreset: Bool = false
    ) {
        let session = oauthService.signIn(
            provider: provider,
            customDisplayName: customDisplayName,
            existingUsers: users,
            existingVoters: voters,
            asAdminPreset: asAdminPreset
        )

        if let idx = users.firstIndex(where: { $0.id == session.user.id }) {
            users[idx] = session.user
        } else {
            users.append(session.user)
        }

        if !voters.contains(where: { $0.id == session.voter.id }) {
            voters.append(session.voter)
        }

        if asAdminPreset {
            grantPermission(
                userId: session.user.id,
                resourceType: .global,
                resourceId: "all",
                permissionType: .admin
            )
            activeInterfaceMode = .admin
        } else {
            activeInterfaceMode = .voter
        }

        currentUser = session.user
        currentVoter = session.voter
        persistAllCollections()

        Task {
            _ = await firestoreService.syncDocumentToFirestore(session.user, collection: firestoreService.config.collections.users)
            _ = await firestoreService.syncDocumentToFirestore(session.voter, collection: firestoreService.config.collections.voters)
        }
    }

    func signOut() {
        currentUser = nil
        currentVoter = nil
        activeInterfaceMode = .voter
    }

    // MARK: - Permission Checks (Read, Edit, Admin)

    func effectivePermission(for userId: String, resourceType: PermissionResourceType, resourceId: String = "all") -> PermissionType {
        let userPerms = permissions.filter {
            $0.userId == userId && (
                ($0.resourceType == .global && $0.resourceId == "all") ||
                ($0.resourceType == resourceType && ($0.resourceId == resourceId || $0.resourceId == "all"))
            )
        }
        if userPerms.contains(where: { $0.permissionType == .admin }) {
            return .admin
        }
        if userPerms.contains(where: { $0.permissionType == .edit }) {
            return .edit
        }
        return .read
    }

    var currentUserCanAccessAdminConsole: Bool {
        guard let user = currentUser else { return false }
        return permissions.contains {
            $0.userId == user.id && ($0.permissionType == .admin || $0.permissionType == .edit)
        }
    }

    func grantPermission(
        userId: String,
        resourceType: PermissionResourceType,
        resourceId: String,
        permissionType: PermissionType
    ) {
        let perm = PermissionModel(
            userId: userId,
            resourceType: resourceType,
            resourceId: resourceId,
            permissionType: permissionType
        )
        if let idx = permissions.firstIndex(where: { $0.id == perm.id }) {
            permissions[idx] = perm
        } else {
            permissions.append(perm)
        }
        persistAllCollections()
        Task {
            _ = await firestoreService.syncDocumentToFirestore(perm, collection: firestoreService.config.collections.permissions)
        }
    }

    func deletePermission(_ permission: PermissionModel) {
        permissions.removeAll { $0.id == permission.id }
        persistAllCollections()
    }

    // MARK: - Admin CRUD: Regions

    func saveRegion(_ region: RegionModel) {
        if let idx = regions.firstIndex(where: { $0.id == region.id }) {
            regions[idx] = region
        } else {
            regions.append(region)
        }
        persistAllCollections()
        Task {
            _ = await firestoreService.syncDocumentToFirestore(region, collection: firestoreService.config.collections.regions)
        }
    }

    func deleteRegion(_ region: RegionModel) {
        regions.removeAll { $0.id == region.id }
        persistAllCollections()
    }

    // MARK: - Admin CRUD: Parties

    func saveParty(_ party: PartyModel) {
        if let idx = parties.firstIndex(where: { $0.id == party.id }) {
            parties[idx] = party
        } else {
            parties.append(party)
        }
        persistAllCollections()
        Task {
            _ = await firestoreService.syncDocumentToFirestore(party, collection: firestoreService.config.collections.parties)
        }
    }

    func deleteParty(_ party: PartyModel) {
        parties.removeAll { $0.id == party.id }
        persistAllCollections()
    }

    // MARK: - Admin CRUD: Candidates

    func saveCandidate(_ candidate: CandidateModel) {
        if let idx = candidates.firstIndex(where: { $0.id == candidate.id }) {
            candidates[idx] = candidate
        } else {
            candidates.append(candidate)
        }

        // Synchronize election.candidates list
        for eIdx in elections.indices {
            if elections[eIdx].id == candidate.electionId {
                if let cIdx = elections[eIdx].candidates.firstIndex(where: { $0.id == candidate.id }) {
                    elections[eIdx].candidates[cIdx] = candidate
                } else {
                    elections[eIdx].candidates.append(candidate)
                }
            } else {
                elections[eIdx].candidates.removeAll { $0.id == candidate.id }
            }
        }

        persistAllCollections()
        Task {
            _ = await firestoreService.syncDocumentToFirestore(candidate, collection: firestoreService.config.collections.candidates)
        }
    }

    func deleteCandidate(_ candidate: CandidateModel) {
        candidates.removeAll { $0.id == candidate.id }
        for eIdx in elections.indices {
            elections[eIdx].candidates.removeAll { $0.id == candidate.id }
        }
        persistAllCollections()
    }

    // MARK: - Admin CRUD: Elections

    func saveElection(_ election: ElectionModel) {
        var updatedElection = election
        updatedElection.candidates = candidates.filter { $0.electionId == election.id }

        if let idx = elections.firstIndex(where: { $0.id == updatedElection.id }) {
            elections[idx] = updatedElection
        } else {
            elections.append(updatedElection)
        }

        persistAllCollections()
        Task {
            _ = await firestoreService.syncDocumentToFirestore(updatedElection, collection: firestoreService.config.collections.elections)
        }
    }

    func deleteElection(_ election: ElectionModel) {
        elections.removeAll { $0.id == election.id }
        candidates.removeAll { $0.electionId == election.id }
        persistAllCollections()
    }

    // MARK: - User Voting & Public Blockchain Recording

    /// Checks if the current anonymous voter has already cast a vote in the specified election.
    func existingVote(for electionId: String) -> VoteModel? {
        guard let voter = currentVoter else { return nil }
        return votes.first { $0.electionId == electionId && $0.voterId == voter.publicUniqueName }
    }

    /// Finds the blockchain block corresponding to a given vote.
    func blockchainBlock(for vote: VoteModel) -> BlockchainBlockModel? {
        blockchainLedger.first { $0.vote.id == vote.id || $0.blockKey == vote.blockchainKey }
    }

    /// Casts an anonymous vote for a candidate in an election and records it onto the public blockchain database.
    @discardableResult
    func castVote(election: ElectionModel, candidate: CandidateModel) -> BlockchainBlockModel? {
        guard let voter = currentVoter else { return nil }
        guard existingVote(for: election.id) == nil else { return nil }

        let voterPublicKey = voter.publicUniqueName
        let blockchainKey = VoteModel.makeBlockchainKey(
            voterId: voterPublicKey,
            electionId: election.id,
            candidateId: candidate.id
        )

        let vote = VoteModel(
            electionId: election.id,
            candidateId: candidate.id,
            candidateName: candidate.name,
            voterId: voterPublicKey,
            date: Date(),
            statusFlag: .verified,
            blockchainKey: blockchainKey
        )

        let minedBlock = blockchainService.appendVoteBlock(
            vote: vote,
            existingChain: blockchainLedger
        )

        votes.append(vote)
        blockchainLedger.append(minedBlock)
        persistAllCollections()

        Task {
            _ = await firestoreService.syncDocumentToFirestore(vote, collection: firestoreService.config.collections.votes)
            _ = await firestoreService.syncDocumentToFirestore(minedBlock, collection: firestoreService.config.collections.blockchain)
        }

        return minedBlock
    }

    // MARK: - Helpers

    func regionName(for regionId: String) -> String {
        regions.first(where: { $0.id == regionId })?.name ?? regionId
    }

    func partyName(for partyId: String) -> String {
        parties.first(where: { $0.id == partyId })?.name ?? partyId
    }

    func voteCount(for candidateId: String, in electionId: String) -> Int {
        blockchainLedger.filter {
            $0.electionId == electionId &&
            $0.vote.candidateId == candidateId &&
            $0.vote.statusFlag == .verified
        }.count
    }

    func totalVotes(in electionId: String) -> Int {
        blockchainLedger.filter {
            $0.electionId == electionId &&
            $0.vote.statusFlag == .verified
        }.count
    }

    func verifyBlockchainIntegrity() -> BlockchainVerificationReport {
        blockchainService.verifyChain(blockchainLedger)
    }
}
