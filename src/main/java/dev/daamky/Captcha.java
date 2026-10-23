package dev.daamky;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.map.MapState.UpdateData;

public final class Captcha implements BotSessionCallback {
    private static final String sourceAttributionUrl = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final int MAP_SIZE = 128;
    private static final int SOLVER_IMAGE_WIDTH = 250;
    private static final int SOLVER_IMAGE_HEIGHT = 150;
    private static final long CAPTCHA_JOIN_WINDOW_MS = 15000L;
    private static final long CAPTCHA_MESSAGE_WINDOW_MS = 10000L;
    private static final long DUPLICATE_IMAGE_SUPPRESSION_MS = 6000L;
    private static final long MAP_UPDATE_QUIET_PERIOD_MS = 700L;
    private static final long WALL_CAPTURE_TIMEOUT_MS = 5000L;
    private static final int MAX_API_ATTEMPTS = 3;
    private static final String[] CAPTCHA_KEYWORDS = new String[]{
        "капч", "captcha", "введите код", "введи код", "код с карты", "введите номер", "номер с картин", "с картинки", "картинке", "введите число"
    };
    private static final String[] SERVER_RESULT_KEYWORDS = new String[]{"неправильно", "не прошли", "проверк", "попыт", "успешн", "passed", "wrong", "verifi"};
    private static final ExecutorService CAPTCHA_EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "bot-captcha-solver");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile boolean captchaEnabled = true;
    private static volatile String answerPrefix = "";
    private final BotSession disconnect;
    private final Map<Integer, int[]> mapPixelsById = new ConcurrentHashMap<>();
    private final Map<Integer, Long> mapSnapshotLogTimes = new ConcurrentHashMap<>();
    private final Map<Integer, double[]> mapFramePositions = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> mapIdByFrameId = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> mapRotationByFrameId = new ConcurrentHashMap<>();
    private final AtomicBoolean solveInProgress = new AtomicBoolean(false);
    private final AtomicBoolean captchaAnalysisScheduled = new AtomicBoolean(false);
    private final Object mapPixelsLock = new Object();
    private volatile double[] playerLook;
    private volatile long lastMapUpdateAtMs;
    private volatile int lastUpdatedMapId = -1;
    private volatile int lastCaptchaImageHash;
    private volatile long sessionResetAtMs;
    private volatile long lastCaptchaPromptAtMs;
    private volatile long lastSolveFinishedAtMs;
// https://t.me/pasterplace

    public Captcha(BotSession disconnect) {
        this.disconnect = disconnect;
    }
// https://t.me/pasterplace

    public static void setCaptchaEnabled(boolean enabled) {
        captchaEnabled = enabled;
    }
// https://t.me/pasterplace

    public static boolean isCaptchaEnabled() {
        return captchaEnabled;
    }
// https://t.me/pasterplace

    public static void setAnswerPrefix(String prefix) {
        answerPrefix = prefix == null ? "" : prefix;
    }
// https://t.me/pasterplace

    @Override
    public void onSessionReset() {
        synchronized (this.mapPixelsLock) {
            this.mapPixelsById.clear();
        }
// https://t.me/pasterplace

        this.mapFramePositions.clear();
        this.mapIdByFrameId.clear();
        this.mapRotationByFrameId.clear();
        this.playerLook = null;
        this.lastUpdatedMapId = -1;
        this.lastMapUpdateAtMs = 0L;
        this.lastCaptchaImageHash = 0;
        this.sessionResetAtMs = System.currentTimeMillis();
        this.lastCaptchaPromptAtMs = 0L;
        this.solveInProgress.set(false);
        this.log("join — captcha armed (window 15000ms)");
    }
// https://t.me/pasterplace

    @Override
    public void onMapDecoration(int frameEntityId, double worldX, double worldY, double worldZ, int entityData) {
        if (captchaEnabled) {
            this.mapFramePositions.put(frameEntityId, new double[]{worldX, worldY, worldZ, entityData});
        }
    }
// https://t.me/pasterplace

    @Override
    public void onMapColorUpdate(int frameEntityId, int mapId) {
        if (captchaEnabled) {
            this.mapIdByFrameId.put(frameEntityId, mapId);
            this.lastMapUpdateAtMs = System.currentTimeMillis();
            this.scheduleCaptchaAnalysis();
        }
    }
// https://t.me/pasterplace

    @Override
    public void onMapCursorUpdate(int frameEntityId, int rotation) {
        if (captchaEnabled && this.mapFramePositions.containsKey(frameEntityId)) {
            this.mapRotationByFrameId.put(frameEntityId, rotation);
            this.lastMapUpdateAtMs = System.currentTimeMillis();
        }
    }
// https://t.me/pasterplace

    @Override
    public void onPlayerLookUpdate(double playerX, double playerY, double playerZ, float yawDegrees) {
        this.playerLook = new double[]{playerX, playerY, playerZ, yawDegrees};
    }
// https://t.me/pasterplace

    private void log(String message) {
        BotCaptchaLog.II1Il11l111II11IIl("[" + this.disconnect.getUsername() + "] " + message);
    }
// https://t.me/pasterplace

    @Override
    public void onMapUpdate(int mapId, UpdateData updateData) {
        if (captchaEnabled && updateData != null) {
            byte[] colors = updateData.colors();
            int updateWidth = updateData.width();
            int updateHeight = updateData.height();
            if (updateWidth > 0 && updateHeight > 0 && colors.length >= updateWidth * updateHeight) {
                int startX = updateData.startX();
                int startZ = updateData.startZ();
                synchronized (this.mapPixelsLock) {
                    int[] mapPixels = this.mapPixelsById.computeIfAbsent(mapId, ignoredMapId -> new int[16384]);

                    for (int sourceRow = 0; sourceRow < updateHeight; sourceRow++) {
                        int mapZ = startZ + sourceRow;
                        if (mapZ >= 0 && mapZ < 128) {
                            for (int sourceColumn = 0; sourceColumn < updateWidth; sourceColumn++) {
                                int mapX = startX + sourceColumn;
                                if (mapX >= 0 && mapX < 128) {
                                    mapPixels[mapX + mapZ * 128] = colors[sourceColumn + sourceRow * updateWidth] & 255;
                                }
                            }
                        }
                    }
                }

                this.lastUpdatedMapId = mapId;
                this.lastMapUpdateAtMs = System.currentTimeMillis();
                this.logMapSnapshot(mapId);
                this.scheduleCaptchaAnalysis();
            }
        }
    }
// https://t.me/pasterplace

    private void logMapSnapshot(int mapId) {
        long nowMs = System.currentTimeMillis();
        Long lastSnapshotLogAtMs = this.mapSnapshotLogTimes.get(mapId);
        if (lastSnapshotLogAtMs == null || nowMs - lastSnapshotLogAtMs >= 500L) {
            int[] mapPixels = this.copyMapPixels(mapId);
            if (mapPixels != null) {
                this.mapSnapshotLogTimes.put(mapId, nowMs);
                boolean uniformMap = isUniformMap(mapPixels);
                this.log(
                "map "
                + mapId
                + " distinct="
                + countDistinctMapColors(mapPixels)
                + " uniform="
                + uniformMap
                + " hash="
                + Arrays.hashCode(mapPixels)
                + (uniformMap ? "" : " -> captcha-map-" + mapId + ".png")
                );
                if (!uniformMap) {
                    CAPTCHA_EXECUTOR.submit(() -> saveImage(renderMapPixels(mapPixels), "captcha-map-" + mapId + ".png"));
                }
            }
        }
    }
// https://t.me/pasterplace

    @Override
    public void onServerMessage(String message) {
        if (captchaEnabled && message != null && !message.isEmpty()) {
            String lowercaseMessage = message.toLowerCase(Locale.ROOT);
// https://t.me/pasterplace

            for (String captchaKeyword : CAPTCHA_KEYWORDS) {
                if (lowercaseMessage.contains(captchaKeyword)) {
                    this.lastCaptchaPromptAtMs = System.currentTimeMillis();
                    this.log("keyword '" + captchaKeyword + "' in: " + BotCaptchaLog.l1I1IlllIlI(message));
                    this.scheduleCaptchaAnalysis();
                    return;
                }
            }

            for (String resultKeyword : SERVER_RESULT_KEYWORDS) {
                if (lowercaseMessage.contains(resultKeyword)) {
                    this.log("server: " + BotCaptchaLog.l1I1IlllIlI(message));
                    return;
                }
            }
        }
    }
// https://t.me/pasterplace

    private void scheduleCaptchaAnalysis() {
        if (this.lastUpdatedMapId >= 0 || !this.mapIdByFrameId.isEmpty()) {
            long nowMs = System.currentTimeMillis();
            boolean recentCaptchaPrompt = nowMs - this.lastCaptchaPromptAtMs <= 10000L;
            boolean recentSessionReset = this.sessionResetAtMs != 0L && nowMs - this.sessionResetAtMs <= 15000L;
            if (recentCaptchaPrompt || recentSessionReset) {
                if (this.captchaAnalysisScheduled.compareAndSet(false, true)) {
                    CAPTCHA_EXECUTOR.submit(() -> {
                        try {
                            long quietPeriodElapsedMs;
                            while ((quietPeriodElapsedMs = System.currentTimeMillis() - this.lastMapUpdateAtMs) < 700L) {
                                Thread.sleep(700L - quietPeriodElapsedMs);
                            }

                            this.waitForCompleteCaptchaWall();
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            return;
                        } finally {
                            this.captchaAnalysisScheduled.set(false);
                        }

                        this.solveCaptchaMap(this.lastUpdatedMapId);
                    });
                }
            }
        }
    }
// https://t.me/pasterplace

    private void solveCaptchaMap(int mapId) {
        if (this.solveInProgress.compareAndSet(false, true)) {
            Captcha.CaptchaImageRegion wallImage = this.buildCaptchaWallImage();
            BufferedImage bufferedImage;
            int imageHash;
            String imageDescription;
            if (wallImage != null) {
                bufferedImage = wallImage.image();
                imageHash = wallImage.mapHash();
                double[] playerLook = this.playerLook;
                imageDescription = "wall "
                + wallImage.columnCount()
                + "x"
                + wallImage.rowCount()
                + " tiles="
                + wallImage.tileCount()
                + " frames="
                + this.mapFramePositions.size()
                + " eye="
                + (playerLook == null ? "?" : (int)playerLook[0] + "," + (int)playerLook[1] + "," + (int)playerLook[2] + " yaw=" + (int)playerLook[3]);
            } else {
                int[] mapPixels = mapId < 0 ? null : this.copyMapPixels(mapId);
                if (mapPixels == null || isUniformMap(mapPixels)) {
                    this.solveInProgress.set(false);
                    return;
                }

                bufferedImage = renderMapPixels(mapPixels);
                imageHash = Arrays.hashCode(mapPixels);
                imageDescription = "map " + mapId + " distinct=" + countDistinctMapColors(mapPixels);
            }

            long nowMs = System.currentTimeMillis();
            if (imageHash == this.lastCaptchaImageHash && nowMs - this.lastSolveFinishedAtMs < 6000L) {
                this.solveInProgress.set(false);
            } else {
                this.lastCaptchaImageHash = imageHash;
                BufferedImage solverImage = resizeCaptchaImage(bufferedImage);
                String outputFileName = "captcha-solved-" + this.disconnect.getUsername() + "-" + System.currentTimeMillis() + ".png";
                saveImage(solverImage, outputFileName);
                this.log("launch " + imageDescription + " resized=" + solverImage.getWidth() + "x" + solverImage.getHeight() + " -> " + outputFileName);
                CAPTCHA_EXECUTOR.submit(() -> this.solveCaptchaImage(solverImage));
            }
        }
    }
// https://t.me/pasterplace

    private void waitForCompleteCaptchaWall() throws InterruptedException {
        long deadlineMs = System.currentTimeMillis() + 5000L;
        boolean loggedIncompleteWall = false;

        while (System.currentTimeMillis() < deadlineMs) {
            Captcha.CaptchaImageRegion wallImage = this.createCaptchaWallImage(false);
            if (wallImage == null || wallImage.tileCount() >= wallImage.columnCount() * wallImage.rowCount()) {
                return;
            }

            if (!loggedIncompleteWall) {
                loggedIncompleteWall = true;
                this.log("wall incomplete " + wallImage.tileCount() + "/" + wallImage.columnCount() * wallImage.rowCount() + " — waiting up to 5000ms");
            }

            Thread.sleep(250L);
        }

        this.log("wall still incomplete after 5000ms — sending as is");
    }
// https://t.me/pasterplace

    private Captcha.CaptchaImageRegion buildCaptchaWallImage() {
        return this.createCaptchaWallImage(true);
    }
// https://t.me/pasterplace

    private Captcha.CaptchaImageRegion createCaptchaWallImage(boolean logGeometry) {
        HashMap<Long, List<int[]>> frameGroupsByWallKey = new HashMap<>();
// https://t.me/pasterplace

        for (Map.Entry<Integer, Integer> frameMapEntry : this.mapIdByFrameId.entrySet()) {
            double[] framePosition = this.mapFramePositions.get(frameMapEntry.getKey());
            if (framePosition != null && this.mapPixelsById.containsKey(frameMapEntry.getValue())) {
                int facing = (int)framePosition[3];
                if (facing >= 2 && facing <= 5) {
                    double horizontalCoordinate = facing >= 4 ? framePosition[0] : framePosition[2];
                    long wallGroupKey = (long)facing << 40 ^ Math.round(horizontalCoordinate * 4.0) & 1099511627775L;
                    frameGroupsByWallKey.computeIfAbsent(wallGroupKey, groupKey -> new ArrayList<>()).add(new int[]{frameMapEntry.getKey(), frameMapEntry.getValue()});
                }
            }
        }
// https://t.me/pasterplace

        if (frameGroupsByWallKey.isEmpty()) {
            return null;
        }

        List<int[]> wallFrames = this.selectWallMapsFacingPlayer(frameGroupsByWallKey);
        if (wallFrames != null && !wallFrames.isEmpty()) {
            int wallFacing = (int)this.mapFramePositions.get(wallFrames.get(0)[0])[3];
            if (logGeometry) {
                StringBuilder geometryLog = new StringBuilder("wall-geometry facing=" + wallFacing + ":");

                for (int[] frameAndMapIds : wallFrames) {
                    double[] framePosition = this.mapFramePositions.get(frameAndMapIds[0]);
                    geometryLog.append(" [map=")
                    .append(frameAndMapIds[1])
                    .append(" x=")
                    .append(framePosition[0])
                    .append(" y=")
                    .append(framePosition[1])
                    .append(" z=")
                    .append(framePosition[2])
                    .append(" rot=")
                    .append(this.mapRotationByFrameId.getOrDefault(frameAndMapIds[0], 0))
                    .append(']');
                }

                this.log(geometryLog.toString());
            }

            boolean useZAxis = wallFacing >= 4;
            double minHorizontal = Double.MAX_VALUE;
            double maxHorizontal = -Double.MAX_VALUE;
            double minVertical = Double.MAX_VALUE;
            double maxVertical = -Double.MAX_VALUE;
// https://t.me/pasterplace

            for (int[] frameAndMapIds : wallFrames) {
                double[] framePosition = this.mapFramePositions.get(frameAndMapIds[0]);
                double horizontalPosition = useZAxis ? framePosition[2] : framePosition[0];
                minHorizontal = Math.min(minHorizontal, horizontalPosition);
                maxHorizontal = Math.max(maxHorizontal, horizontalPosition);
                minVertical = Math.min(minVertical, framePosition[1]);
                maxVertical = Math.max(maxVertical, framePosition[1]);
            }

            int columnCount = (int)Math.round(maxHorizontal - minHorizontal) + 1;
            int rowCount = (int)Math.round(maxVertical - minVertical) + 1;
            if (columnCount >= 1 && rowCount >= 1 && columnCount <= 8 && rowCount <= 8 && columnCount * rowCount >= 2) {
                BufferedImage compositeImage = new BufferedImage(columnCount * 128, rowCount * 128, 1);
                Graphics2D compositeGraphics = compositeImage.createGraphics();
                compositeGraphics.setColor(Color.WHITE);
                compositeGraphics.fillRect(0, 0, compositeImage.getWidth(), compositeImage.getHeight());
                int imageHash = 1;
                int placedTileCount = 0;

                for (int[] frameAndMapIds : wallFrames) {
                    int[] mapPixels = this.copyMapPixels(frameAndMapIds[1]);
                    if (mapPixels != null) {
                        int mapRotation = this.mapRotationByFrameId.getOrDefault(frameAndMapIds[0], 0) & 3;
                        if (mapRotation != 0) {
                            mapPixels = rotateMapPixels(mapPixels, mapRotation);
                        }

                        double[] framePosition = this.mapFramePositions.get(frameAndMapIds[0]);
                        double horizontalPosition = useZAxis ? framePosition[2] : framePosition[0];

                        int column = switch (wallFacing) {
                        case 2 -> (int)Math.round(maxHorizontal - horizontalPosition);
                        case 3 -> (int)Math.round(horizontalPosition - minHorizontal);
                        case 4 -> (int)Math.round(horizontalPosition - minHorizontal);
                            default -> (int)Math.round(maxHorizontal - horizontalPosition);
                        };
                        int row = (int)Math.round(maxVertical - framePosition[1]);
                        if (column >= 0 && column < columnCount && row >= 0 && row < rowCount) {
                            compositeGraphics.drawImage(renderMapPixels(mapPixels), column * 128, row * 128, null);
                            imageHash = imageHash * 31 + frameAndMapIds[1];
                            imageHash = imageHash * 31 + Arrays.hashCode(mapPixels);
                            imageHash = imageHash * 31 + column * 17 + row;
                            placedTileCount++;
                        }
                    }
                }

                compositeGraphics.dispose();
                return placedTileCount == 0 ? null : new Captcha.CaptchaImageRegion(compositeImage, imageHash, columnCount, rowCount, placedTileCount);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
// https://t.me/pasterplace

    private List<int[]> selectWallMapsFacingPlayer(Map<Long, List<int[]>> frameGroupsByWallKey) {
        double[] playerLook = this.playerLook;
        List<int[]> bestFrameGroup = null;
        double bestFacingScore = -1.0;
        if (playerLook != null) {
            double yawRadians = Math.toRadians(playerLook[3]);
            double facingVectorX = -Math.sin(yawRadians);
            double facingVectorZ = Math.cos(yawRadians);

            for (List<int[]> candidateFrames : frameGroupsByWallKey.values()) {
                double averageFrameX = 0.0;
                double averageFrameZ = 0.0;

                for (int[] frameAndMapIds : candidateFrames) {
                    double[] framePosition = this.mapFramePositions.get(frameAndMapIds[0]);
                    averageFrameX += framePosition[0];
                    averageFrameZ += framePosition[2];
                }

                averageFrameX /= candidateFrames.size();
                averageFrameZ /= candidateFrames.size();
                double deltaX = averageFrameX - playerLook[0];
                double deltaZ = averageFrameZ - playerLook[2];
                double distanceToWall = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                if (!(distanceToWall < 0.01) && !(distanceToWall > 32.0)) {
                    double dotProduct = deltaX / distanceToWall * facingVectorX + deltaZ / distanceToWall * facingVectorZ;
                    if (!(dotProduct < 0.2)) {
                        double facingScore = dotProduct * 100.0 / (1.0 + distanceToWall);
                        if (facingScore > bestFacingScore) {
                            bestFacingScore = facingScore;
                            bestFrameGroup = candidateFrames;
                        }
                    }
                }
            }
        }

        if (bestFrameGroup == null) {
            for (List<int[]> candidateFrames : frameGroupsByWallKey.values()) {
                if (bestFrameGroup == null || candidateFrames.size() > bestFrameGroup.size()) {
                    bestFrameGroup = candidateFrames;
                }
            }
        }

        return bestFrameGroup;
    }
// https://t.me/pasterplace

    private int[] copyMapPixels(int mapId) {
        synchronized (this.mapPixelsLock) {
            int[] mapPixels = this.mapPixelsById.get(mapId);
            return mapPixels == null ? null : (int[])mapPixels.clone();
        }
    }
// https://t.me/pasterplace

    private static void saveImage(BufferedImage image, String fileName) {
        try {
            Path outputFilePath = MinecraftClient.getInstance().runDirectory.toPath().resolve(fileName);
            ImageIO.write(image, "png", outputFilePath.toFile());
        } catch (Exception saveError) {
        }
    }
// https://t.me/pasterplace

    private void solveCaptchaImage(BufferedImage image) {
        String username = this.disconnect.getUsername();
        String lastApiErrorMessage = null;

        try {
            for (int attemptNumber = 1; attemptNumber <= 3; attemptNumber++) {
                if (!this.disconnect.isOnline()) {
                    this.log("abort: bot not joined");
                    return;
                }

                try {
                    this.log("api attempt " + attemptNumber + "/3");
                    String rawAnswer = CaptchaSolver.solveImageCaptcha(image);
                    if (rawAnswer != null && !rawAnswer.isBlank()) {
                        String answer = rawAnswer.trim();
                        boolean sent = this.disconnect.sendChatMessage(answerPrefix + answer);
                        this.log("sent answer '" + answer + "' ok=" + sent);
                        if (sent) {
                            notifyCaptchaResult(username + ": капча решена -> " + answer, false);
                        } else {
                            notifyCaptchaResult(username + ": капча не отправлена (дисконнект)", true);
                        }
// https://t.me/pasterplace

                        return;
                    }

                    this.log("attempt " + attemptNumber + ": no answer from api");
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception attemptError) {
                    lastApiErrorMessage = attemptError.getMessage();
                    this.log("attempt " + attemptNumber + " error: " + lastApiErrorMessage);
                }

                Thread.sleep(1000L * attemptNumber);
            }

            this.log("FAILED after 3 attempts" + (lastApiErrorMessage != null ? " (" + lastApiErrorMessage + ")" : ""));
            notifyCaptchaResult(username + (lastApiErrorMessage != null ? ": ошибка капчи " + lastApiErrorMessage : ": капча не решена"), true);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } finally {
            this.lastSolveFinishedAtMs = System.currentTimeMillis();
            this.solveInProgress.set(false);
        }
    }
// https://t.me/pasterplace

    private static int countDistinctMapColors(int[] mapPixels) {
        boolean[] seenColors = new boolean[256];
        int distinctColorCount = 0;

        for (int pixelColorId : mapPixels) {
            int colorIndex = pixelColorId & 0xFF;
            if (!seenColors[colorIndex]) {
                seenColors[colorIndex] = true;
                distinctColorCount++;
            }
        }

        return distinctColorCount;
    }
// https://t.me/pasterplace

    private static boolean isUniformMap(int[] mapPixels) {
        int firstColor = mapPixels[0];

        for (int color : mapPixels) {
            if (color != firstColor) {
                return false;
            }
        }

        return true;
    }
// https://t.me/pasterplace

    private static int[] rotateMapPixels(int[] sourcePixels, int quarterTurns) {
        int[] rotatedPixels = sourcePixels;

        for (int turn = 0; turn < quarterTurns; turn++) {
            int[] nextPixels = new int[16384];

            for (int row = 0; row < 128; row++) {
                for (int column = 0; column < 128; column++) {
                    nextPixels[column + row * 128] = rotatedPixels[row + (127 - column) * 128];
                }
            }

            rotatedPixels = nextPixels;
        }

        return rotatedPixels;
    }
// https://t.me/pasterplace

    private static BufferedImage resizeCaptchaImage(BufferedImage sourceImage) {
        if (sourceImage.getWidth() == 250 && sourceImage.getHeight() == 150) {
            return sourceImage;
        }

        BufferedImage resizedImage = new BufferedImage(250, 150, 1);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(sourceImage, 0, 0, 250, 150, null);
        graphics.dispose();
        return resizedImage;
    }
// https://t.me/pasterplace

    private static BufferedImage renderMapPixels(int[] mapPixels) {
        BufferedImage image = new BufferedImage(128, 128, 1);

        for (int mapRow = 0; mapRow < 128; mapRow++) {
            for (int mapColumn = 0; mapColumn < 128; mapColumn++) {
                int color = MapColor.getRenderColor(mapPixels[mapColumn + mapRow * 128]);
                if (color >>> 24 == 0) {
                    color = 16777215;
                }

                image.setRGB(mapColumn, mapRow, color & 16777215);
            }
        }

        return image;
    }
// https://t.me/pasterplace

    private static void notifyCaptchaResult(String message, boolean isError) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient != null) {
            minecraftClient.execute(() -> {
                if (minecraftClient.player != null) {
                    if (isError) {
                        ClientChatLogger.l1I1IlllIlI(message);
                    } else {
                        ClientChatLogger.II1Il11l111II11IIl(message);
                    }
                }
            });
        }
    }
// https://t.me/pasterplace

    private record CaptchaImageRegion(BufferedImage image, int mapHash, int columnCount, int rowCount, int tileCount) {

        private CaptchaImageRegion(BufferedImage image, int mapHash, int columnCount, int rowCount, int tileCount) {
            this.image = image;
            this.mapHash = mapHash;
            this.columnCount = columnCount;
            this.rowCount = rowCount;
            this.tileCount = tileCount;
        }
    }
}

