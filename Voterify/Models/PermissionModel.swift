import Foundation

/// Permission type model: Read, Edit, Admin.
enum PermissionType: String, Codable, CaseIterable, Identifiable {
    case read = "Read"
    case edit = "Edit"
    case admin = "Admin"

    var id: String { rawValue }
}

/// Target model type combined with UserModel in PermissionModel.
enum PermissionResourceType: String, Codable, CaseIterable, Identifiable {
    case election = "election"
    case candidate = "candidate"
    case region = "region"
    case party = "party"
    case global = "global"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .election: return "Election"
        case .candidate: return "Candidate"
        case .region: return "Region"
        case .party: return "Party"
        case .global: return "All Models (Global)"
        }
    }
}

/// Permission model: using a combination of UserModel with the other models,
/// grants a PermissionType (Read, Edit, Admin) to each.
struct PermissionModel: Identifiable, Codable, Hashable {
    var id: String
    var userId: String
    var resourceType: PermissionResourceType
    var resourceId: String
    var permissionType: PermissionType

    init(
        id: String? = nil,
        userId: String,
        resourceType: PermissionResourceType,
        resourceId: String,
        permissionType: PermissionType
    ) {
        self.id = id ?? "\(userId)_\(resourceType.rawValue)_\(resourceId)"
        self.userId = userId
        self.resourceType = resourceType
        self.resourceId = resourceId
        self.permissionType = permissionType
    }
}
