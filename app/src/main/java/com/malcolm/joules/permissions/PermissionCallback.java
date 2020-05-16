package com.malcolm.joules.permissions;

public interface PermissionCallback {
    void permissionGranted();

    void permissionRefused();
}
