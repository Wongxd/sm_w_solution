package io.wongxd.solution.permission.core

import androidx.compose.runtime.Composable

/**
 * Composable that exercises the permissions flows as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions)
 * when a permission is *required* to be granted for [content].
 *
 * If the permission is not granted or a rationale should be shown, [permissionNotGrantedContent] will
 * be added to Composition. If the user doesn't want to be asked for permissions again,
 * [permissionNotAvailableContent] will be added instead.
 *
 * @param permissionState required permission to be granted.
 * @param permissionNotGrantedContent content to show when the user hasn't granted the permission.
 * Requesting the permission to the user is allowed using [PermissionState.launchPermissionRequest]
 * in a side-effect or non-Composable lambda.
 * @param permissionNotAvailableContent content to show when the permission is not available. This
 * could be because the user doesn't want to be asked again for this permission or the permission
 * is blocked in the device. Attempting to request the permission to the user in this part of
 * Composition has no effect.
 * @param content content to show when the permission is granted.
 */
@Composable
fun PermissionRequired(
    permissionState: PermissionState,
    permissionNotGrantedContent: @Composable (() -> Unit),
    permissionNotAvailableContent: @Composable (() -> Unit),
    content: @Composable (() -> Unit),
) {
    when {
        permissionState.hasPermission -> {
            content()
        }
        permissionState.shouldShowRationale || !permissionState.permissionRequested -> {
            permissionNotGrantedContent()
        }
        else -> {
            permissionNotAvailableContent()
        }
    }
}

/**
 * Composable that exercises the permissions flows as described in the
 * [documentation](https://developer.android.com/training/permissions/requesting#workflow_for_requesting_permissions)
 * when multiple permissions are *required* to be granted for [content].
 *
 * If any permission is not granted and a rationale should be shown, or the user hasn't been
 * presented with the permissions yet, [permissionsNotGrantedContent] will be added to Composition.
 * If the user doesn't want to be asked for permissions again, [permissionsNotAvailableContent]
 * will be added instead.
 *
 * @param multiplePermissionsState required permissions to be granted.
 * @param permissionsNotGrantedContent content to show when the user hasn't granted all permissions.
 * Requesting the permissions to the user is allowed using
 * [MultiplePermissionsState.launchMultiplePermissionRequest] in a side-effect or
 * non-Composable lambda.
 * @param permissionsNotAvailableContent content to show when the permissions are not available.
 * This could be because the user doesn't want to be asked again for these permissions or the
 * permissions are blocked in the device. Attempting to request the permission to the user in
 * this part of Composition has no effect.
 * @param content content to show when all permissions are granted.
 */
@Composable
fun PermissionsRequired(
    multiplePermissionsState: MultiplePermissionsState,
    permissionsNotGrantedContent: @Composable (() -> Unit),
    permissionsNotAvailableContent: @Composable (() -> Unit),
    content: @Composable (() -> Unit),
) {
    when {
        multiplePermissionsState.allPermissionsGranted -> {
            content()
        }
        multiplePermissionsState.shouldShowRationale ||
                !multiplePermissionsState.permissionRequested ->
        {
            permissionsNotGrantedContent()
        }
        else -> {
            permissionsNotAvailableContent()
        }
    }
}
