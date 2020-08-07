package com.benoolean.KitWar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Set;

public class TeamScoreLogic implements Listener {

    static class KitTeam {
        String name;
        ChatColor teamColor;
        int score;
        int totalKillCount;
        int totalDeathCount;
        Player mvp;

        public KitTeam (String name, ChatColor teamColor) {
            this.teamColor = teamColor;
            this.name = name;
        }
    }

    public static HashMap<String, KitTeam> TeamMap = new HashMap<>();
    public static HashMap<Player, KitTeam> PlayerTeamMap = new HashMap<>();
    public static HashMap<Player, Integer> PlayerScoreMap = new HashMap<>();
    public static HashMap<Player, Integer> PlayerKillCountMap = new HashMap<>();

    public static Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    public static Objective objective = scoreboard.registerNewObjective("Kit War", "dummy", "Player Info");
    public static Team teamRed = scoreboard.registerNewTeam("Red");
    public static Team teamBlue = scoreboard.registerNewTeam("Blue");
    public static Team teamNone = scoreboard.registerNewTeam("No Team");

    public TeamScoreLogic() {
        KitWar plugin = KitWar.getInstance();

        TeamMap.put("Red", new KitTeam("Red", ChatColor.RED));
        TeamMap.put("Blue", new KitTeam("Blue", ChatColor.AQUA));
        TeamMap.put("No Team", new KitTeam("No Team", ChatColor.GRAY));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Game Information");

        // team init
        teamRed.setPrefix(ChatColor.RED + "" + ChatColor.BOLD + "RED " + ChatColor.RESET);
        teamBlue.setPrefix(ChatColor.AQUA + "" + ChatColor.BOLD + "BLUE " + ChatColor.RESET);
        teamNone.setPrefix(ChatColor.GRAY + "" + ChatColor.BOLD + "No Team " + ChatColor.RESET);

        // setting default empty scoreboard
        for (int scoreboardIndex = 1; scoreboardIndex <= 14; scoreboardIndex++) {
            Score score;

            // first bar
            if (scoreboardIndex == 14) {
                score = objective.getScore(new String(new char[15]).replace("\0", "▅"));
            }
            else if (scoreboardIndex == 12) {
                score = objective.getScore("Author: " + ChatColor.GREEN + "benoolean");
            }
            else if (scoreboardIndex == 10) {
                score = objective.getScore("Kills: " + ChatColor.GREEN);
            }
            else if (scoreboardIndex == 9) {
                score = objective.getScore("Deaths: " + ChatColor.GREEN);
            }
            else if (scoreboardIndex == 8) {
                score = objective.getScore("Score: " + ChatColor.GREEN);
            }
            else if (scoreboardIndex == 4) {
                score = objective.getScore(ChatColor.GREEN + "Support me by following my");
            }
            else if (scoreboardIndex == 3) {
                score = objective.getScore(ChatColor.GREEN + "Github or visit my website.");
            }
            else if (scoreboardIndex == 2) {
                score = objective.getScore(ChatColor.GREEN + "More information at:");
            }
            else if (scoreboardIndex == 1) {
                score = objective.getScore(ChatColor.GOLD + "" + ChatColor.BOLD + "www.benoolean.com");
            }
            else {
                score = objective.getScore(new String(new char[scoreboardIndex]).replace("\0", " "));
            }

            score.setScore(scoreboardIndex);
        }
    }


    /////////////////////////////////
    // 							   //
    //       Choosing Team         //
    //							   //
    /////////////////////////////////

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        JoinTeam(player, "No Team");
    }

    public static void JoinTeam(Player player, String teamName) {
        if (TeamScoreLogic.TeamMap.get(teamName) != null) {
            PlayerTeamMap.put(player, TeamMap.get(teamName));
            PlayerScoreMap.put(player, 0);
            PlayerKillCountMap.put(player, 0);
            player.setScoreboard(TeamScoreLogic.scoreboard);

            if (teamName.equalsIgnoreCase("Red")) {
                teamBlue.removeEntry(player.getName());
                teamNone.removeEntry(player.getName());
                teamRed.addEntry(player.getName());
                player.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "RED " + ChatColor.RESET + player.getName());
            }
            else if (teamName.equalsIgnoreCase("Blue")){
                teamRed.removeEntry(player.getName());
                teamNone.removeEntry(player.getName());
                teamBlue.addEntry(player.getName());
                player.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "BLUE " + ChatColor.RESET + player.getName());
            }
            else {
                teamRed.removeEntry(player.getName());
                teamBlue.removeEntry(player.getName());
                teamNone.addEntry(player.getName());
                player.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "No Team " + ChatColor.RESET + player.getName());
            }

            player.sendMessage("You are now on team: " + teamName);

            Team playerKillCount =  PlayerRegisterScore(player, "Kills");
            Team playerDeathCount =  PlayerRegisterScore(player, "Deaths");
            Team playerScore =  PlayerRegisterScore(player, "Score");

            playerKillCount.addEntry("Kills: " + ChatColor.GREEN);
            playerKillCount.setSuffix("0");
            playerKillCount.setPrefix("");

            playerDeathCount.addEntry("Deaths: " + ChatColor.GREEN);
            playerDeathCount.setSuffix("0");
            playerDeathCount.setPrefix("");

            playerScore.addEntry("Score: " + ChatColor.GREEN);
            playerScore.setSuffix("0");
            playerScore.setPrefix("");

            player.setScoreboard(TeamScoreLogic.scoreboard);
        }
    }

    /////////////////////////////////
    // 							   //
    //         Score Board         //
    //							   //
    /////////////////////////////////

    public static void ScoreboardSet(Player player) {
        PlayerSetScore(player, "Kills");
    }

    /////////////////////////////////
    // 							   //
    //            Misc             //
    //							   //
    /////////////////////////////////

    public static void PlayerSetScore(Player player, String score) {
        String hashedPlayerScoreKey = (player.getName() + score).hashCode() + "";
        Team playerKills =  scoreboard.getTeam(hashedPlayerScoreKey);

        int playerKillCount = (PlayerKillCountMap.get(player) != null) ? PlayerKillCountMap.get(player) : 0;
        playerKills.setSuffix(Integer.toString(playerKillCount + 1));

        PlayerKillCountMap.put(player, playerKillCount + 1);
    }

    public static Team PlayerRegisterScore(Player player, String score) {
        String hashedPlayerScoreKey = (player.getName() + score).hashCode() + "";
        if (scoreboard.getTeam(hashedPlayerScoreKey) == null) {
            return scoreboard.registerNewTeam(hashedPlayerScoreKey);
        }

        return scoreboard.getTeam(hashedPlayerScoreKey);
    }
}
