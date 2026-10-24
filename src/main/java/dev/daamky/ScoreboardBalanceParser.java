package dev.daamky;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;

public final class ScoreboardBalanceParser {
   private static final Pattern COIN_AMOUNT_PATTERN = Pattern.compile(
      "(?:\\u043c\\u043e\\u043d\\u0435\\u0442(?:\\u043e\\u043a|\\u043a\\u0438|\\u044b)?|coins?)\\s*:\\s*([\\d][\\d\\s\\u00A0.,]*)([k\\u043a]*)", 66
   );

   private ScoreboardBalanceParser() {
   }

   public static long findDisplayedBalance(net.minecraft.scoreboard.Scoreboard scoreboard, String scoreboardEntryName) {
      long balanceInHundredths = findBalanceInScoreboard(scoreboard, scoreboardEntryName);
      return balanceInHundredths < 0L ? -1L : Math.round(balanceInHundredths / 100.0);
   }

   public static long findBalanceInScoreboard(net.minecraft.scoreboard.Scoreboard scoreboard, String scoreboardEntryName) {
      if (scoreboard == null) {
         return -1L;
      }

      ScoreboardObjective objective = null;
      if (scoreboardEntryName != null) {
         Team team = scoreboard.getScoreHolderTeam(scoreboardEntryName);
         if (team != null) {
            ScoreboardDisplaySlot scoreboardDisplaySlot = ScoreboardDisplaySlot.fromFormatting(team.getColor());
            if (scoreboardDisplaySlot != null) {
               objective = scoreboard.getObjectiveForSlot(scoreboardDisplaySlot);
            }
         }
      }

      if (objective == null) {
         objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
      }

      if (objective == null) {
         return -1L;
      }

      for (net.minecraft.scoreboard.ScoreboardEntry scoreboardEntry : scoreboard.getScoreboardEntries(objective)) {
         if (!scoreboardEntry.hidden()) {
            Team team2 = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
            MutableText mutableText = Team.decorateName(team2, scoreboardEntry.name());
            long balanceInHundredths = parseCoinAmount(stripFormattingCodes(mutableText.getString()));
            if (balanceInHundredths >= 0L) {
               return balanceInHundredths;
            }
         }
      }

      return -1L;
   }

   public static long parseCoinAmount(String displayedAmount) {
      if (displayedAmount != null && !displayedAmount.isBlank()) {
         Matcher amountMatcher = COIN_AMOUNT_PATTERN.matcher(displayedAmount);
         if (!amountMatcher.find()) {
            return -1L;
         }

         try {
            String amountWithoutSpaces = amountMatcher.group(1).replaceAll("[\\s\\u00A0]", "");
            String suffix = amountMatcher.group(2) == null ? "" : amountMatcher.group(2);
            char decimalSeparator = findDecimalSeparator(amountWithoutSpaces, suffix);
            StringBuilder normalizedAmountBuilder = new StringBuilder(amountWithoutSpaces.length());

            for (int digitIndex = 0; digitIndex < amountWithoutSpaces.length(); digitIndex++) {
               char character = amountWithoutSpaces.charAt(digitIndex);
               if (Character.isDigit(character)) {
                  normalizedAmountBuilder.append(character);
               } else if ((character == '.' || character == ',') && character == decimalSeparator) {
                  normalizedAmountBuilder.append('.');
               }
            }

            if (normalizedAmountBuilder.isEmpty()) {
               return -1L;
            }

            BigDecimal parsedAmount = new BigDecimal(normalizedAmountBuilder.toString());

            for (int suffixIndex = 0; suffixIndex < suffix.length(); suffixIndex++) {
               parsedAmount = parsedAmount.movePointRight(3);
            }

            return parsedAmount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
         } catch (Exception parseException) {
            return -1L;
         }
      } else {
         return -1L;
      }
   }

   public static String stripFormattingCodes(String formattedText) {
      StringBuilder plainTextBuilder = new StringBuilder(formattedText.length());

      for (int characterIndex = 0; characterIndex < formattedText.length(); characterIndex++) {
         char character = formattedText.charAt(characterIndex);
         if (character == 167) {
            characterIndex++;
         } else {
            plainTextBuilder.append(character);
         }
      }

      return plainTextBuilder.toString();
   }

   private static char findDecimalSeparator(String amountText, String suffix) {
      int commaIndex = amountText.lastIndexOf(44);
      int periodIndex = amountText.lastIndexOf(46);
      if (commaIndex >= 0 && periodIndex >= 0) {
         return (char)(commaIndex > periodIndex ? ',' : '.');
      } else if (commaIndex >= 0) {
         return (char)(isDecimalSeparator(amountText, commaIndex, suffix) ? ',' : '\u0000');
      } else if (periodIndex >= 0) {
         return (char)(isDecimalSeparator(amountText, periodIndex, suffix) ? '.' : '\u0000');
      } else {
         return '\u0000';
      }
   }

   private static boolean isDecimalSeparator(String amountText, int separatorIndex, String suffix) {
      int trailingDigitCount = amountText.length() - separatorIndex - 1;
      if (trailingDigitCount <= 0) {
         return false;
      } else {
         return !suffix.isEmpty() ? true : trailingDigitCount != 3;
      }
   }
}

