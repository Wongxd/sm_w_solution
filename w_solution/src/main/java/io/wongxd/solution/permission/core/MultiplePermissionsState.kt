package io.wongxd.solution.permission.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 *
 * @param permissions the permissions to control and observe.
 */
@Composable
fun rememberMultiplePermissionsState(
    permissions: List<String>
): MultiplePermissionsState {
    return rememberMutableMultiplePermissionsState(permissions)
}

/**
 * A state object that can be hoisted to control and observe multiple [permissions] status changes.
 *
 * In most cases, this will be created via [rememberMultiplePermissionsState].
 *
 * It's recommended that apps exercise the permissions workflow as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions).
 */
@Stable
interface MultiplePermissionsState {

    /**
     * List of all permissions to request.
     */
    val permissions: List<PermissionState>

    /**
     * List of permissions revoked by the user.
     */
    val revokedPermissions: List<PermissionState>

    /**
     * When `true`, the user has granted all [permissions].
     */
    val allPermissionsGranted: Boolean

    /**
     * When `true`, the user should be presented with a rationale.
     */
    val shouldShowRationale: Boolean

    /**
     * When `true`, the [permissions] request has been done previously.
     */
    val permissionRequested: Boolean

    /**
     * Request the [permissions] to the user.
     *
     * This should always be triggered from non-composable scope, for example, from a side-effect
     * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
     *
     * This triggers a system dialog that asks the user to grant or revoke the permission.
     * Note that this dialog might not appear on the screen if the user doesn't want to be asked
     * again or has denied the permission multiple times.
     * This behavior varies depending on the Android level API.
     */
    fun launchMultiplePermissionRequest(): Unit

    fun jump2Setting(): Unit
}