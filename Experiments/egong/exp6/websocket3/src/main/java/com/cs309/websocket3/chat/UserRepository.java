package com.cs309.websocket3.chat;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final Map<String, Role> users = new HashMap<>();

    static {
        // Predefined accounts with roles
        users.put("eddie", Role.USER);
        users.put("marcel", Role.USER);
        users.put("tony", Role.USER);
        users.put("tyler", Role.USER);
    }

    public static Role findRoleByUsername(String username) {
        return users.get(username);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static void addUser(User user) {
        users.put(user.getUsername(), Role.VIEWER);
    }
}

