package com.voterify.app.models

/**
 * Permission type model: Read, Edit, Admin.
 */
enum class PermissionType(val rawValue: String) {
    READ("Read"),
    EDIT("Edit"),
    ADMIN("Admin");

    companion object {
        fun fromRaw(raw: String): PermissionType =
            entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: READ
    }
}

/**
 * Target model type combined with UserModel in PermissionModel.
 */
enum class PermissionResourceType(val rawValue: String, val displayName: String) {
    ELECTION("election", "Election"),
    CANDIDATE("candidate", "Candidate"),
    REGION("region", "Region"),
    PARTY("party", "Party"),
    GLOBAL("global", "All Models (Global)");

    companion object {
        fun fromRaw(raw: String): PermissionResourceType =
            entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: ELECTION
    }
}

/**
 * Permission model: using a combination of UserModel with the other models,
 * grants a PermissionType (Read, Edit, Admin) to each.
 */
data class PermissionModel(
    val id: String,
    val userId: String,
    val resourceType: PermissionResourceType,
    val resourceId: String,
    val permissionType: PermissionType
) {
    constructor(
        userId: String,
        resourceType: PermissionResourceType,
        resourceId: String,
        permissionType: PermissionType
    ) : this(
        id = "${userId}_${resourceType.rawValue}_$resourceId",
        userId = userId,
        resourceType = resourceType,
        resourceId = resourceId,
        permissionType = permissionType
    )
}
