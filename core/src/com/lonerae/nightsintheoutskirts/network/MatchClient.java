package com.lonerae.nightsintheoutskirts.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.lonerae.nightsintheoutskirts.game.Player;
import com.lonerae.nightsintheoutskirts.game.roles.AllianceName;
import com.lonerae.nightsintheoutskirts.game.roles.Role;
import com.lonerae.nightsintheoutskirts.game.roles.RoleName;
import com.lonerae.nightsintheoutskirts.network.responses.AssignRoleResponse;
import com.lonerae.nightsintheoutskirts.network.responses.ConnectionResponse;
import com.lonerae.nightsintheoutskirts.network.responses.GreetingResponse;
import com.lonerae.nightsintheoutskirts.network.responses.LobbyResponse;
import com.lonerae.nightsintheoutskirts.network.responses.ProceedResponse;
import com.lonerae.nightsintheoutskirts.network.responses.VoteResponse;
import com.lonerae.nightsintheoutskirts.network.responses.abilities.AssassinInfoResponse;
import com.lonerae.nightsintheoutskirts.network.responses.abilities.MurderResponse;
import com.lonerae.nightsintheoutskirts.screens.visible.gamescreens.DayScreen;
import com.lonerae.nightsintheoutskirts.screens.visible.gamescreens.night.AssassinNightScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchClient {

    private static final Map<String, Integer> availableMatches = new HashMap<>();
    private static Client client;
    private static List<RoleName> matchRoleList;
    private static Boolean connectionAccepted = null;
    private static Role assignedRole;
    private static Boolean permitted = null;
    private static Boolean assassinPermitted = null;

    private static boolean firstFlag = true;
    private static HashMap<String, RoleName> connectedPlayersMap;
    private static HashMap<String, RoleName> alivePlayersMap;
    private static HashMap<String, RoleName> deadPlayersMap;
    private static List<String> hangedList;
    private static List<String> murderedList;

    private static Boolean endGame;
    private static AllianceName winner;

    public static Client getClient() {
        if (client == null) {
            client = new Client();
            client.start();
            NetworkUtil.register(client);
            createListener();
        }
        return client;
    }

    public static void close() {
        client.stop();
        clearClient();
    }

    private static void clearClient() {
        client = null;
        availableMatches.clear();
        matchRoleList = null;
        connectionAccepted = null;
        assignedRole = null;
        permitted = null;
        assassinPermitted = null;
        firstFlag = true;
        connectedPlayersMap = null;
        alivePlayersMap = null;
        deadPlayersMap = null;
        hangedList = null;
        murderedList = null;
        endGame = null;
        winner = null;
    }

    public static Map<String, Integer> getAvailableMatches() {
        return availableMatches;
    }

    public static List<RoleName> getMatchRoleList() {
        return matchRoleList;
    }

    public static Boolean isConnectionAccepted() {
        return connectionAccepted;
    }

    public static Role getAssignedRole() {
        return assignedRole;
    }

    public static Boolean isPermitted() {
        return permitted;
    }

    public static void setPermitted(Boolean permitted) {
        MatchClient.permitted = permitted;
    }

    public static Boolean getAssassinPermitted() {
        return assassinPermitted;
    }

    public static void setAssassinPermitted(Boolean assassinPermitted) {
        MatchClient.assassinPermitted = assassinPermitted;
    }

    public static HashMap<String, RoleName> getConnectedPlayersMap() {
        return connectedPlayersMap;
    }

    public static HashMap<String, RoleName> getAlivePlayersMap() {
        return alivePlayersMap;
    }

    public static HashMap<String, RoleName> getDeadPlayersMap() {
        return deadPlayersMap;
    }

    public static List<String> getHangedList() {
        return hangedList;
    }

    public static List<String> getMurderedList() {
        return murderedList;
    }

    public static void setEndGame(Boolean endGame) {
        MatchClient.endGame = endGame;
    }

    public static Boolean isEndGame() {
        return endGame;
    }

    public static AllianceName getWinner() {
        return winner;
    }

    private static void createListener() {
        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof GreetingResponse) {
                    GreetingResponse response = (GreetingResponse) object;
                    availableMatches.put(response.townName, response.numberOfPlayers);
                } else if (object instanceof ConnectionResponse) {
                    ConnectionResponse response = (ConnectionResponse) object;
                    connectionAccepted = response.connectionAccepted;
                } else if (object instanceof LobbyResponse) {
                    LobbyResponse response = (LobbyResponse) object;
                    matchRoleList = new ArrayList<>(response.matchRoleList);
                } else if (object instanceof AssignRoleResponse) {
                    AssignRoleResponse response = (AssignRoleResponse) object;
                    assignedRole = Role.getRole(response.assignedRole);
                } else if (object instanceof ProceedResponse) {
                    ProceedResponse response = (ProceedResponse) object;
                    permitted = response.permit;
                    if (firstFlag) {
                        connectedPlayersMap = response.alivePlayerMap;
                        firstFlag = false;
                    }
                    alivePlayersMap = response.alivePlayerMap;
                    deadPlayersMap = response.deadPlayerMap;
                    if (response.hangedList != null) {
                        hangedList = response.hangedList;
                    }
                    if (response.murderedList != null) {
                        murderedList = response.murderedList;
                    }
                    if (response.endGame) {
                        endGame = true;
                        winner = response.winner;
                    } else {
                        endGame = false;
                    }
                } else if (object instanceof VoteResponse) {
                    VoteResponse response = (VoteResponse) object;
                    DayScreen.updateVote(response.voterName, response.votedPlayerName, response.vote);
                } else if (object instanceof AssassinInfoResponse) {
                    if (Player.getPlayer().getRole().getName().equals(RoleName.ASSASSIN)) {
                        AssassinInfoResponse response = (AssassinInfoResponse) object;
                        AssassinNightScreen.updateOverview(response.killer, response.target, response.skip);
                    }
                } else if (object instanceof MurderResponse) {
                    if (Player.getPlayer().getRole().getName().equals(RoleName.ASSASSIN)) {
                        MurderResponse response = (MurderResponse) object;
                        assassinPermitted = response.permit;
                    }
                }
            }
        });
    }
}
