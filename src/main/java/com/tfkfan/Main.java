package com.tfkfan;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Main {
    private static int frameWidth = 70;
    private static int whiteBg = -1;
    private static int grayBg = -8882056;
    private static int leftRankOffset = 149;
    private static int topRankOffset = 589;
    private static int rankFrameWidth = 30;
    private static int rankFrameHeight = 27;

    private static int leftSuitOffset = 149;
    private static int topSuitOffset = 617;
    private static int suitFrameWidth = 23;
    private static int suitFrameHeight = 21;

    public static String processImage(BufferedImage image) {
        if (image.getRGB(0, 0) == grayBg) {
            for (int y = 0; y < image.getHeight(); y++)
                for (int x = 0; x < image.getWidth(); x++)
                    if (image.getRGB(x, y) == grayBg) image.setRGB(x, y, whiteBg);
        }
        var isCardRank = false;
        var stringBuilder = new StringBuilder();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                var rgb = image.getRGB(x, y);
                if (rgb == whiteBg) isCardRank = true;
                if (rgb == whiteBg)
                    stringBuilder.append(" ");
                else
                    stringBuilder.append("*");
            }
            stringBuilder.append("\n");
        }
        if (!isCardRank) return "";
        else return stringBuilder.toString();
    }

    public static List<Card> parseCards(BufferedImage image, int frameWidth, int cardsNum) {
        return Stream.iterate(0, x -> x + 1)
                .limit(cardsNum)
                .map(n -> {
                    var rankStr = processImage(
                            image.getSubimage(
                                    n * frameWidth + leftRankOffset, topRankOffset, rankFrameWidth, rankFrameHeight
                            )
                    );

                    var suitStr = processImage(
                            image.getSubimage(
                                    n * frameWidth + leftSuitOffset, topSuitOffset, suitFrameWidth, suitFrameHeight
                            )
                    );
                    if (!rankStr.isBlank() && !suitStr.isBlank())
                        return new Card(rankStr, suitStr);
                    else return null;
                }).filter(Objects::nonNull).toList();
    }

    public static int levenshtein(String targetStr, String sourceStr) {
        int m = targetStr.length(), n = sourceStr.length();
        int[][] delta = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            delta[i][0] = i;
        for (int j = 1; j <= n; j++)
            delta[0][j] = j;
        for (int j = 1; j <= n; j++)
            for (int i = 1; i <= m; i++) {
                if (targetStr.charAt(i - 1) == sourceStr.charAt(j - 1))
                    delta[i][j] = delta[i - 1][j - 1];
                else
                    delta[i][j] = Math.min(delta[i - 1][j] + 1,
                            Math.min(delta[i][j - 1] + 1, delta[i - 1][j - 1] + 1));
            }
        return delta[m][n];
    }

    public static String findSymbol(List<AssetsFileContent> assets, String targetStr) {
        var min = 1000000;
        var value = "";
        for (var assetsFileContent : assets) {
            var levenshtein = levenshtein(assetsFileContent.getContent(), targetStr);
            if (levenshtein < min) {
                min = levenshtein;
                value = assetsFileContent.getFileName();
            }
        }
        return value;
    }

    public static List<AssetsFileContent> getAssetsContent(String directory) {
        return Arrays.stream(Objects.requireNonNull(new File(directory).listFiles()))
                .map(file -> {
                    try {
                        return new AssetsFileContent(file.getName().replace(".txt", ""), Files.readString(file.toPath(), Charset.forName("UTF-8")));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public static void main(String[] args) throws IOException {
        var suites = getAssetsContent(args[2]);
        var ranks = getAssetsContent(args[1]);
        for (var file : Objects.requireNonNull(new File(args[0]).listFiles())) {
            System.out.printf("%s - %s \n", file.getName(), parseCards(ImageIO.read(file), frameWidth, 5)
                    .stream().map(card ->
                            findSymbol(ranks, card.getRank()) + findSymbol(suites, card.getSuit()))
                    .reduce("", (acc, s) -> acc + s));
        }
    }
}