package com.worldclass.ai.security.rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default RBAC implementation
 */
public class DefaultRBACService implements RBACService {

    private final Map<String, Set<String>> userRoles = new ConcurrentHashMap<>();
    private final Map<String, Role> roles = new ConcurrentHashMap<>();

    public DefaultRBACService() {
        initializeDefaultRoles();
    }

    @Override
    public boolean hasPermission(String userId, Permission permission) {
        Set<Permission> userPermissions = getUserPermissions(userId);
        return userPermissions.stream()
            .anyMatch(p -> p.getResource().equals(permission.getResource()) &&
                          p.getAction() == permission.getAction());
    }

    @Override
    public boolean hasRole(String userId, String roleName) {
        return userRoles.getOrDefault(userId, Collections.emptySet())
            .contains(roleName);
    }

    @Override
    public void grantRole(String userId, String roleName) {
        if (!roles.containsKey(roleName)) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        userRoles.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
            .add(roleName);
    }

    @Override
    public void revokeRole(String userId, String roleName) {
        Set<String> roles = userRoles.get(userId);
        if (roles != null) {
            roles.remove(roleName);
        }
    }

    @Override
    public void createRole(Role role) {
        roles.put(role.getName(), role);
    }

    @Override
    public Set<Permission> getUserPermissions(String userId) {
        Set<Permission> permissions = new HashSet<>();
        Set<String> userRoleSet = userRoles.getOrDefault(userId, Collections.emptySet());

        for (String roleName : userRoleSet) {
            Role role = roles.get(roleName);
            if (role != null) {
                permissions.addAll(role.getPermissions());

                // Add inherited permissions
                if (role.getInheritsFrom() != null) {
                    for (String inheritedRole : role.getInheritsFrom()) {
                        Role inherited = roles.get(inheritedRole);
                        if (inherited != null) {
                            permissions.addAll(inherited.getPermissions());
                        }
                    }
                }
            }
        }

        return permissions;
    }

    private void initializeDefaultRoles() {
        // Admin role
        createRole(Role.builder()
            .name("ADMIN")
            .description("Full system access")
            .permissions(Set.of(
                Permission.builder().resource("*").action(Permission.Action.ADMIN).build()
            ))
            .build());

        // User role
        createRole(Role.builder()
            .name("USER")
            .description("Standard user access")
            .permissions(Set.of(
                Permission.builder().resource("llm").action(Permission.Action.READ).build(),
                Permission.builder().resource("llm").action(Permission.Action.EXECUTE).build(),
                Permission.builder().resource("prompts").action(Permission.Action.READ).build()
            ))
            .build());

        // Developer role
        createRole(Role.builder()
            .name("DEVELOPER")
            .description("Developer access")
            .permissions(Set.of(
                Permission.builder().resource("llm").action(Permission.Action.READ).build(),
                Permission.builder().resource("llm").action(Permission.Action.WRITE).build(),
                Permission.builder().resource("llm").action(Permission.Action.EXECUTE).build(),
                Permission.builder().resource("prompts").action(Permission.Action.READ).build(),
                Permission.builder().resource("prompts").action(Permission.Action.WRITE).build(),
                Permission.builder().resource("analytics").action(Permission.Action.READ).build()
            ))
            .inheritsFrom(List.of("USER"))
            .build());

        // Read-only role
        createRole(Role.builder()
            .name("VIEWER")
            .description("Read-only access")
            .permissions(Set.of(
                Permission.builder().resource("*").action(Permission.Action.READ).build()
            ))
            .build());
    }
}
