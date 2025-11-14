package com.worldclass.ai.security.rbac;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Feature #19: RBAC - Fine-grained permissions
 *
 * Role-Based Access Control for:
 * - API access control
 * - Feature permissions
 * - Resource-level permissions
 * - Custom roles
 */
public interface RBACService {

    /**
     * Check if user has permission
     */
    boolean hasPermission(String userId, Permission permission);

    /**
     * Check if user has role
     */
    boolean hasRole(String userId, String roleName);

    /**
     * Grant role to user
     */
    void grantRole(String userId, String roleName);

    /**
     * Revoke role from user
     */
    void revokeRole(String userId, String roleName);

    /**
     * Create custom role
     */
    void createRole(Role role);

    /**
     * Get user's effective permissions
     */
    Set<Permission> getUserPermissions(String userId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Role {
        private String name;
        private String description;
        private Set<Permission> permissions;
        private List<String> inheritsFrom;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class Permission {
        private String resource;
        private Action action;
        private String condition;

        public enum Action {
            READ,
            WRITE,
            DELETE,
            EXECUTE,
            ADMIN
        }
    }
}
