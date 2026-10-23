package com.voterify.app.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voterify.app.models.BlockchainBlockModel
import com.voterify.app.models.CandidateModel
import com.voterify.app.models.ElectionModel
import com.voterify.app.models.OAuthProvider
import com.voterify.app.models.PartyModel
import com.voterify.app.models.PermissionModel
import com.voterify.app.models.PermissionResourceType
import com.voterify.app.models.PermissionType
import com.voterify.app.models.RegionModel
import com.voterify.app.models.UserModel
import com.voterify.app.models.VoteModel
import com.voterify.app.models.VoteStatus
import com.voterify.app.models.VoterModel
import kotlinx.coroutines.launch

enum class AppInterfaceMode(val label: String) {
    VOTER("Elections"),
    BLOCKCHAIN("Blockchain"),
    ADMIN("Admin")
}

/**
 * Primary Jetpack ViewModel coordinating Firestore models, OAuth sessions,
 * RBAC permissions (Read, Edit, Admin), and the public vote blockchain.
 */
class AppStoreViewModel : ViewModel() {
    val firestoreService = FirestoreService()
    val blockchainService = BlockchainService()
    val oauthService = OAuthService()

    var currentUser by mutableStateOf<UserModel?>(null)
        private set
    var currentVoter by mutableStateOf<VoterModel?>(null)
        private set
    var activeInterfaceMode by mutableStateOf(AppInterfaceMode.VOTER)

    var users by mutableStateOf<List<UserModel>>(emptyList())
        private set
    var voters by mutableStateOf<List<VoterModel>>(emptyList())
        private set
    var regions by mutableStateOf<List<RegionModel>>(emptyList())
        private set
    var parties by mutableStateOf<List<PartyModel>>(emptyList())
        private set
    var candidates by mutableStateOf<List<CandidateModel>>(emptyList())
        private set
    var elections by mutableStateOf<List<ElectionModel>>(emptyList())
        private set
    var permissions by mutableStateOf<List<PermissionModel>>(emptyList())
        private set
    var votes by mutableStateOf<List<VoteModel>>(emptyList())
        private set
    var blockchainLedger by mutableStateOf<List<BlockchainBlockModel>>(emptyList())
        private set

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val seededRegions = listOf(
            RegionModel(id = "region-national", name = "National District"),
            RegionModel(id = "region-north", name = "Northern Metro Region"),
            RegionModel(id = "region-pacific", name = "Pacific Coastal Region")
        )

        val seededParties = listOf(
            PartyModel(id = "party-civic", name = "Civic Alliance Party"),
            PartyModel(id = "party-future", name = "Future Horizon Party"),
            PartyModel(id = "party-ind", name = "Independent Coalition")
        )

        val generalCandidates = listOf(
            CandidateModel(id = "cand-elena-rostova", name = "Elena Rostova", electionId = "election-2026-general", partyId = "party-civic", isActive = true),
            CandidateModel(id = "cand-marcus-vance", name = "Marcus Vance", electionId = "election-2026-general", partyId = "party-future", isActive = true),
            CandidateModel(id = "cand-priya-nair", name = "Priya Nair", electionId = "election-2026-general", partyId = "party-ind", isActive = true)
        )

        val transitCandidates = listOf(
            CandidateModel(id = "cand-david-chen", name = "David Chen", electionId = "election-2026-transit", partyId = "party-civic", isActive = true),
            CandidateModel(id = "cand-sarah-jenkins", name = "Sarah Jenkins", electionId = "election-2026-transit", partyId = "party-future", isActive = true)
        )

        val seededElections = listOf(
            ElectionModel(
                id = "election-2026-general",
                name = "2026 National Representative Poll",
                dateStart = "2026-10-01",
                dateEnd = "2026-11-15",
                regionId = "region-national",
                isActive = true,
                candidates = generalCandidates
            ),
            ElectionModel(
                id = "election-2026-transit",
                name = "Northern Metro Clean Transit Commissioner",
                dateStart = "2026-10-15",
                dateEnd = "2026-12-01",
                regionId = "region-north",
                isActive = true,
                candidates = transitCandidates
            )
        )

        val adminUser = UserModel(
            id = "user-admin-001",
            name = "Alex Morgan (Admin)",
            email = "admin@voterify.org",
            oauthProvider = OAuthProvider.APPLE
        )
        val sampleVoterUser = UserModel(
            id = "user-voter-001",
            name = "Jordan Taylor",
            email = "jordan@example.com",
            oauthProvider = OAuthProvider.GOOGLE
        )

        val adminVoter = VoterModel(
            id = "550E8400-E29B-41D4-A716-446655440000",
            publicUniqueName = "VOTER-550E8400-E29B-41D4-A716-446655440000",
            privateUserId = adminUser.id
        )
        val sampleVoter = VoterModel(
            id = "7C9E6679-7425-40DE-944B-E07FC1F90AE7",
            publicUniqueName = "VOTER-7C9E6679-7425-40DE-944B-E07FC1F90AE7",
            privateUserId = sampleVoterUser.id
        )

        val seededPermissions = listOf(
            PermissionModel(
                userId = adminUser.id,
                resourceType = PermissionResourceType.GLOBAL,
                resourceId = "all",
                permissionType = PermissionType.ADMIN
            ),
            PermissionModel(
                userId = sampleVoterUser.id,
                resourceType = PermissionResourceType.ELECTION,
                resourceId = "election-2026-general",
                permissionType = PermissionType.READ
            )
        )

        regions = seededRegions
        parties = seededParties
        candidates = generalCandidates + transitCandidates
        elections = seededElections
        users = listOf(adminUser, sampleVoterUser)
        voters = listOf(adminVoter, sampleVoter)
        permissions = seededPermissions
        votes = emptyList()
        blockchainLedger = listOf(blockchainService.makeGenesisBlock())
    }

    // MARK: - OAuth Login & Session Management

    fun loginWithOAuth(
        provider: OAuthProvider,
        customDisplayName: String? = null,
        asAdminPreset: Boolean = false
    ) {
        val session = oauthService.signIn(
            provider = provider,
            customDisplayName = customDisplayName,
            existingUsers = users,
            existingVoters = voters,
            asAdminPreset = asAdminPreset
        )

        users = users.filterNot { it.id == session.user.id } + session.user
        if (voters.none { it.id == session.voter.id }) {
            voters = voters + session.voter
        }

        if (asAdminPreset) {
            grantPermission(
                userId = session.user.id,
                resourceType = PermissionResourceType.GLOBAL,
                resourceId = "all",
                permissionType = PermissionType.ADMIN
            )
            activeInterfaceMode = AppInterfaceMode.ADMIN
        } else {
            activeInterfaceMode = AppInterfaceMode.VOTER
        }

        currentUser = session.user
        currentVoter = session.voter

        viewModelScope.launch {
            firestoreService.syncDocumentToFirestore(
                documentId = session.user.id,
                jsonPayload = session.user.toString(),
                collection = firestoreService.config.collections.users
            )
            firestoreService.syncDocumentToFirestore(
                documentId = session.voter.id,
                jsonPayload = session.voter.toString(),
                collection = firestoreService.config.collections.voters
            )
        }
    }

    fun signOut() {
        currentUser = null
        currentVoter = null
        activeInterfaceMode = AppInterfaceMode.VOTER
    }

    // MARK: - Permission Checks (Read, Edit, Admin)

    fun effectivePermission(
        userId: String,
        resourceType: PermissionResourceType,
        resourceId: String = "all"
    ): PermissionType {
        val matching = permissions.filter {
            it.userId == userId && (
                (it.resourceType == PermissionResourceType.GLOBAL && it.resourceId == "all") ||
                    (it.resourceType == resourceType && (it.resourceId == resourceId || it.resourceId == "all"))
                )
        }
        return when {
            matching.any { it.permissionType == PermissionType.ADMIN } -> PermissionType.ADMIN
            matching.any { it.permissionType == PermissionType.EDIT } -> PermissionType.EDIT
            else -> PermissionType.READ
        }
    }

    val currentUserCanAccessAdminConsole: Boolean
        get() {
            val user = currentUser ?: return false
            return permissions.any {
                it.userId == user.id && (it.permissionType == PermissionType.ADMIN || it.permissionType == PermissionType.EDIT)
            }
        }

    fun grantPermission(
        userId: String,
        resourceType: PermissionResourceType,
        resourceId: String,
        permissionType: PermissionType
    ) {
        val perm = PermissionModel(
            userId = userId,
            resourceType = resourceType,
            resourceId = resourceId,
            permissionType = permissionType
        )
        permissions = permissions.filterNot { it.id == perm.id } + perm
        viewModelScope.launch {
            firestoreService.syncDocumentToFirestore(
                documentId = perm.id,
                jsonPayload = perm.toString(),
                collection = firestoreService.config.collections.permissions
            )
        }
    }

    fun deletePermission(permission: PermissionModel) {
        permissions = permissions.filterNot { it.id == permission.id }
    }

    // MARK: - Admin CRUD: Regions

    fun saveRegion(region: RegionModel) {
        val idx = regions.indexOfFirst { it.id == region.id }
        regions = if (idx >= 0) {
            regions.toMutableList().also { it[idx] = region }
        } else {
            regions + region
        }
        viewModelScope.launch {
            firestoreService.syncDocumentToFirestore(
                documentId = region.id,
                jsonPayload = region.toString(),
                collection = firestoreService.config.collections.regions
            )
        }
    }

    fun deleteRegion(region: RegionModel) {
        regions = regions.filterNot { it.id == region.id }
    }

    // MARK: - Admin CRUD: Parties

    fun saveParty(party: PartyModel) {
        val idx = parties.indexOfFirst { it.id == party.id }
        parties = if (idx >= 0) {
            parties.toMutableList().also { it[idx] = party }
        } else {
            parties + party
        }
        viewModelScope.launch {
            firestoreService.syncDocumentToFirestore(
                documentId = party.id,
                jsonPayload = party.toString(),
                collection = firestoreService.config.collections.parties
            )
        }
    }

    fun deleteParty(party: PartyModel) {
        parties = parties.filterNot { it.id == party.id }
    }

    // MARK: - Admin CRUD: Candidates

    fun saveCandidate(candidate: CandidateModel) {
        val idx = candidates.indexOfFirst { it.id == candidate.id }
        candidates = if (idx >= 0) {
            candidates.toMutableList().also { it[idx] = candidate }
        } else {
            candidates + candidate
        }
        syncElectionCandidateLists()
        viewModelScope.launch {
            firestoreService.syncDocumentToFirestore(
                documentId = candidate.id,
                jsonPayload = candidate.toString(),
                collection = firestoreService.config.collections.candidates
            )
        }
    }

    fun deleteCandidate(candidate: CandidateModel) {
        candidates = candidates.filterNot { it.id == candidate.id }
        syncElectionCandidateLists()
    }

    // MARK: - Admin CRUD: Elections

    fun saveElection(election: ElectionModel) {
        val assigned = candidates.filter { it.electionId == election.id }
        val updated = election.copy(candidates = assigned)
        val idx = elections.indexOfFirst { it.id == updated.id }
        elections = if (idx >= 0) {
            elections.toMutableList().also { it[idx] = updated }
        } else {
            elections + updated
        }
        viewModelScope.launch {
            firestoreService.syncDocumentToFirestore(
                documentId = updated.id,
                jsonPayload = updated.toString(),
                collection = firestoreService.config.collections.elections
            )
        }
    }

    fun deleteElection(election: ElectionModel) {
        elections = elections.filterNot { it.id == election.id }
        candidates = candidates.filterNot { it.electionId == election.id }
    }

    private fun syncElectionCandidateLists() {
        elections = elections.map { election ->
            election.copy(candidates = candidates.filter { it.electionId == election.id })
        }
    }

    // MARK: - User Voting & Public Blockchain Recording

    fun existingVote(electionId: String): VoteModel? {
        val voter = currentVoter ?: return null
        return votes.firstOrNull { it.electionId == electionId && it.voterId == voter.publicUniqueName }
    }

    fun blockchainBlock(vote: VoteModel): BlockchainBlockModel? =
        blockchainLedger.firstOrNull { it.vote.id == vote.id || it.blockKey == vote.blockchainKey }

    fun castVote(election: ElectionModel, candidate: CandidateModel): BlockchainBlockModel? {
        val voter = currentVoter ?: return null
        if (existingVote(election.id) != null) return null

        val voterPublicKey = voter.publicUniqueName
        val blockchainKey = VoteModel.makeBlockchainKey(
            voterId = voterPublicKey,
            electionId = election.id,
            candidateId = candidate.id
        )

        val vote = VoteModel(
            electionId = election.id,
            candidateId = candidate.id,
            candidateName = candidate.name,
            voterId = voterPublicKey,
            statusFlag = VoteStatus.VERIFIED,
            blockchainKey = blockchainKey
        )

        val minedBlock = blockchainService.appendVoteBlock(
            vote = vote,
            existingChain = blockchainLedger
        )

        votes = votes + vote
        blockchainLedger = blockchainLedger + minedBlock

        viewModelScope.launch {
            firestoreService.syncDocumentToFirestore(
                documentId = vote.id,
                jsonPayload = vote.toString(),
                collection = firestoreService.config.collections.votes
            )
            firestoreService.syncDocumentToFirestore(
                documentId = minedBlock.id,
                jsonPayload = minedBlock.toString(),
                collection = firestoreService.config.collections.blockchain
            )
        }

        return minedBlock
    }

    // MARK: - Helpers

    fun regionName(regionId: String): String =
        regions.firstOrNull { it.id == regionId }?.name ?: regionId

    fun partyName(partyId: String): String =
        parties.firstOrNull { it.id == partyId }?.name ?: partyId

    fun voteCount(candidateId: String, electionId: String): Int =
        blockchainLedger.count {
            it.electionId == electionId &&
                it.vote.candidateId == candidateId &&
                it.vote.statusFlag == VoteStatus.VERIFIED
        }

    fun totalVotes(electionId: String): Int =
        blockchainLedger.count {
            it.electionId == electionId && it.vote.statusFlag == VoteStatus.VERIFIED
        }

    fun verifyBlockchainIntegrity(): BlockchainVerificationReport =
        blockchainService.verifyChain(blockchainLedger)
}
