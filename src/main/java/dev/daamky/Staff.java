package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameMode;

public final class Staff extends AnimatedListHudElement {
    private static final String[] STAFF_KEYWORDS = new String[]{
        "helper",
        "хелпер",
        "moder",
        "модер",
        "admin",
        "админ",
        "curator",
        "куратор",
        "owner",
        "владел",
        "staff",
        "стафф",
        "developer",
        "разраб",
        "support",
        "саппорт",
        "стажёр",
        "стажер",
        "trainee",
        "гл.мод",
        "ст.мод",
        "мл.мод",
        "st.mod",
        "ml.mod",
        "gl.mod",
        "мод",
        "mod",
        "dev"
    };
    private static final Pattern BRACKETED_RANK_PATTERN = Pattern.compile("[\\[({【«]([^\\])}】»]+)[\\])}»]");
    private static final Pattern QUESTION_MARKS_PATTERN = Pattern.compile("[?？]+");
    private static final Pattern RANK_SEPARATOR_PATTERN = Pattern.compile("[|•»«:—–]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern INVALID_PLAYER_NAME_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Zа-яА-ЯёЁєЄіІїЇґҐ0-9.\\-_/ ]");
    private static final Pattern EDGE_PUNCTUATION_PATTERN = Pattern.compile("^[.\\-_/ ]+|[.\\-_/ ]+$");
    private static final Pattern NON_LETTER_RANK_PREFIX_PATTERN = Pattern.compile("[^a-zа-яё.]");
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("§.");

    public Staff() {
        super("Staff", "staff", 7.0F, 330.0F);
    }

    @Override
    protected String getHeaderGlyph() {
        return "G";
    }

    @Override
    protected String getHeaderLabel() {
        return "Staff";
    }

    @Override
    protected List<AnimatedListHudElement.HudListEntry> getEntries(boolean chatScreenOpen) {
        ArrayList staffEntries = new ArrayList();
        if (CLIENT.getNetworkHandler() == null) {
            return staffEntries;
        }

         for (net.minecraft.client.network.PlayerListEntry playerEntry : CLIENT.getNetworkHandler().getPlayerList()) {
             String playerName = playerEntry.getProfile().name();
            if (playerName != null) {
                String normalizedPlayerName = normalizePlayerName(playerName);
                if (normalizedPlayerName.isEmpty()) {
                    normalizedPlayerName = QUESTION_MARKS_PATTERN.matcher(playerName).replaceAll("");
                }

                String teamPrefix = "";
                 Team team = playerEntry.getScoreboardTeam();
                if (team != null) {
                    teamPrefix = stripFormattingCodes(team.getPrefix().getString());
                }

                 String displayPrefix = playerEntry.getDisplayName() != null ? stripFormattingCodes(playerEntry.getDisplayName().getString()) : "";
                String staffRole = extractStaffRole(teamPrefix);
                if (staffRole == null) {
                    staffRole = extractStaffRole(displayPrefix.replace(playerName, ""));
                }

                if (staffRole == null && !playerName.equals(normalizedPlayerName)) {
                    staffRole = extractStaffRole(displayPrefix.replace(normalizedPlayerName, ""));
                }

                if (staffRole != null) {
                     boolean spectator = playerEntry.getGameMode() == GameMode.SPECTATOR;
                    RgbaColor statusColor = spectator ? RgbaColor.rgb(255, 200, 120) : RgbaColor.rgb(130, 235, 160);
                    String statusLabel = staffRole.isEmpty() ? (spectator ? "spec" : "") : staffRole;
                    staffEntries.add(new AnimatedListHudElement.HudListEntry(normalizedPlayerName, statusLabel, null, statusColor, null, -1.0F));
                    if (staffEntries.size() >= 12) {
                        break;
                    }
                }
            }
        }

        return staffEntries;
    }

    private static String extractStaffRole(String roleText) {
        if (roleText != null && !roleText.isEmpty()) {
            if (!containsStaffKeyword(roleText)) {
                return null;
            }

            Matcher matcher = BRACKETED_RANK_PATTERN.matcher(roleText);

            while (matcher.find()) {
                String bracketedRoleText = matcher.group(1);
                if (containsStaffKeyword(bracketedRoleText)) {
                    String normalizedBracketedRole = normalizePlayerName(bracketedRoleText);
                    if (!normalizedBracketedRole.isEmpty()) {
                        return normalizedBracketedRole;
                    }
                }
            }

            String[] roleFragments = RANK_SEPARATOR_PATTERN.split(roleText);

            for (String roleFragment : roleFragments) {
                if (containsStaffKeyword(roleFragment)) {
                    String strippedRoleFragment = extractRolePrefix(roleFragment);
                    String normalizedRoleFragment = normalizePlayerName(strippedRoleFragment.isEmpty() ? roleFragment : strippedRoleFragment);
                    if (!normalizedRoleFragment.isEmpty()) {
                        return normalizedRoleFragment;
                    }
                }
            }

            String fallbackRoleText = extractRolePrefix(roleText);
            String normalizedFallbackRole = normalizePlayerName(fallbackRoleText.isEmpty() ? roleText : fallbackRoleText);
            return normalizedFallbackRole.isEmpty() ? null : normalizedFallbackRole;
        } else {
            return null;
        }
    }

    private static boolean containsStaffKeyword(String candidateText) {
        if (candidateText != null && !candidateText.isEmpty()) {
            String normalizedText = candidateText.toLowerCase(Locale.ROOT);

            for (String staffKeyword : STAFF_KEYWORDS) {
                if (staffKeyword.length() <= 3) {
                    if (containsWholeWord(normalizedText, staffKeyword)) {
                        return true;
                    }
                } else if (normalizedText.contains(staffKeyword)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private static boolean containsWholeWord(String textToSearch, String keyword) {
        int searchStartIndex = 0;

        while (true) {
            int matchIndex = textToSearch.indexOf(keyword, searchStartIndex);
            if (matchIndex < 0) {
                return false;
            }

            int matchEndIndex = matchIndex + keyword.length();
            boolean startsAtWordBoundary = matchIndex == 0 || !isWordCharacter(textToSearch.charAt(matchIndex - 1));
            boolean endsAtWordBoundary = matchEndIndex == textToSearch.length() || !isWordCharacter(textToSearch.charAt(matchEndIndex));
            if (startsAtWordBoundary && endsAtWordBoundary) {
                return true;
            }

            searchStartIndex = matchIndex + 1;
        }
    }

    private static boolean isWordCharacter(char character) {
        return character >= 'a' && character <= 'z' || character >= 1072 && character <= 1103 || character == 1105;
    }

    private static String extractRolePrefix(String roleLabel) {
        if (roleLabel == null) {
            return "";
        }

        String[] roleLabelWords = WHITESPACE_PATTERN.split(roleLabel.trim());

        for (int wordIndex = 0; wordIndex < roleLabelWords.length; wordIndex++) {
            if (containsStaffKeyword(roleLabelWords[wordIndex])) {
                if (wordIndex > 0) {
                    String normalizedPrefixWord = NON_LETTER_RANK_PREFIX_PATTERN.matcher(roleLabelWords[wordIndex - 1].toLowerCase(Locale.ROOT)).replaceAll("");
                    if (isSeniorityPrefix(normalizedPrefixWord)) {
                        return roleLabelWords[wordIndex - 1] + " " + roleLabelWords[wordIndex];
                    }
                }

                return roleLabelWords[wordIndex];
            }
        }

        return "";
    }

    private static boolean isSeniorityPrefix(String prefix) {
        return switch (prefix) {
        case "ст", "ст.", "мл", "мл.", "гл", "гл.", "st", "st.", "ml", "ml.", "gl", "gl.", "d", "d.", "старший", "младший", "главный", "chief", "senior", "head", "lead" -> true;
            default -> false;
        };
    }

    private static String normalizePlayerName(String playerName) {
        if (playerName == null) {
            return "";
        }

        String normalizedName = QUESTION_MARKS_PATTERN.matcher(playerName).replaceAll("");
        normalizedName = INVALID_PLAYER_NAME_CHARACTERS_PATTERN.matcher(normalizedName).replaceAll("");
        normalizedName = EDGE_PUNCTUATION_PATTERN.matcher(WHITESPACE_PATTERN.matcher(normalizedName).replaceAll(" ").trim()).replaceAll("");
        return normalizedName.length() > 16 ? normalizedName.substring(0, 16).trim() : normalizedName;
    }

    private static String stripFormattingCodes(String formattedText) {
        return formattedText == null ? "" : FORMATTING_CODE_PATTERN.matcher(formattedText).replaceAll("").replace("§", "").replace(' ', ' ').trim();
    }
}

