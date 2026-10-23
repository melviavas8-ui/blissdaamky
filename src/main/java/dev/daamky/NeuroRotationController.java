package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class NeuroRotationController {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    public static final int RECORDING_SAMPLE_SIZE = 16;
    public static final int TRAINING_INPUT_SIZE = 17;
    private static final float PREDICTION_OUTPUT_SCALE = 5.0F;
    private static final long RECORDING_IDLE_TIMEOUT_MILLIS = 3000L;
    private static final int TARGET_SNAPSHOT_HISTORY_LIMIT = 40;
    private static final double MAX_TARGET_DISTANCE = 10.0;
    private static final int INFERENCE_STATE_TIMEOUT_TICKS = 40;
    private static final int MODEL_HIDDEN_SIZE = 64;
    private static final int MODEL_MIXTURE_COUNT = 6;
    private static final int DEFAULT_TRAINING_EPOCHS = 60;
    private static final float TRAINING_LEARNING_RATE = 0.003F;
    private static final int DEFAULT_FINETUNE_EPOCHS = 20;
    private static final float TARGETED_OUTPUT_TEMPERATURE = 0.6F;
    private static final float FINETUNE_LEARNING_RATE = 8.0E-4F;
    private static final NeuroRotationController INSTANCE = new NeuroRotationController();
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Path datasetDirectory = FabricLoader.getInstance().getGameDir().resolve("daamky").resolve("neuro");
    private final Random random = new Random();
    private String currentRecordingName;
    private final List<float[]> recordedSamples = new ArrayList<>();
    private float[] currentRecordingSample;
    private double accumulatedMouseYawDelta;
    private double accumulatedMousePitchDelta;
    private LivingEntity currentTarget;
    private long lastTargetAttackMillis;
    private int lastAttackPlayerAge = -1000;
    private int savedSampleCount;
    private final ArrayDeque<NeuroRotationController.RotationTrainingSnapshot> rotationSnapshotHistory = new ArrayDeque<>();
    private NeuroRotationController.RotationTrainingSnapshot pendingRotationSnapshot;
    private int pendingTargetEntityId = -1;
    private volatile boolean trainingInProgress;
    private volatile boolean cancelTrainingRequested;
    private volatile NeuralNetworkTrainer trainingWorker;
    private volatile String trainingModelName;
    private volatile String trainingStatus = "";
    private volatile boolean isFineTuning;
    private volatile boolean trainingSucceeded;
    private volatile int trainingSampleCount;
    private volatile long trainingFinishedAtMillis;
    private RecurrentMixtureDensityModel activeModel;
    private String activeModelName;
    private float[] recurrentHiddenState;
    private float[] previousFeatureVector;
    private float previousYawDelta;
    private float previousPitchDelta;
    private int inferenceStepCount = -1;
// https://t.me/pasterplace

    private NeuroRotationController() {
    }
// https://t.me/pasterplace

    public static NeuroRotationController getInstance() {
        return INSTANCE;
    }
// https://t.me/pasterplace

    public static void accumulateMouseDelta(double yawDelta, double pitchDelta) {
        INSTANCE.accumulatedMouseYawDelta += yawDelta * 0.15;
        INSTANCE.accumulatedMousePitchDelta += pitchDelta * 0.15;
    }
// https://t.me/pasterplace

    public boolean isModelLoaded() {
        return this.activeModel != null;
    }
// https://t.me/pasterplace

    public String getActiveModelName() {
        return this.activeModelName;
    }
// https://t.me/pasterplace

    public void handleNeuroCommand(String[] textArray) {
        if (textArray.length == 0) {
            ClientChatLogger.II1Il11l111II11IIl(".neuro record <имя> | stop | train <имя> [эпохи] | tune <модель> [запись] [эпохи] | play <имя> | list");
        } else {
            String commandName = textArray[0].toLowerCase(Locale.ROOT);
            String primaryName = textArray.length > 1 ? sanitizeFileName(textArray[1]) : null;
            switch (commandName) {
            case "record":
                requireName(primaryName);
                this.startRecording(primaryName);
                break;
            case "stop":
                this.stopCurrentOperation();
                break;
            case "train":
                requireName(primaryName);
                this.startTraining(primaryName, primaryName, false, textArray.length > 2 ? Integer.parseInt(textArray[2]) : 60);
                break;
            case "tune":
            case "finetune":
                requireName(primaryName);
                String secondaryName = textArray.length > 2 ? sanitizeFileName(textArray[2]) : primaryName;
                this.startTraining(primaryName, secondaryName, true, textArray.length > 3 ? Integer.parseInt(textArray[3]) : 20);
                break;
            case "play":
                requireName(primaryName);
                this.loadAndActivateModel(primaryName);
                break;
            case "list":
                this.listRecordings();
                break;
            default:
                ClientChatLogger.l1I1IlllIlI("Неизвестная подкоманда: " + commandName);
            }
        }
    }
// https://t.me/pasterplace

    private static void requireName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("укажите имя");
        }
    }
// https://t.me/pasterplace

    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^A-Za-z0-9_\\-а-яА-ЯёЁ]", "");
    }
// https://t.me/pasterplace

    private void startRecording(String recordingName) {
        if (this.currentRecordingName != null) {
            this.flushRecordingBatch();
        }

        this.currentRecordingName = recordingName;
        this.savedSampleCount = 0;
        this.recordedSamples.clear();
        this.currentRecordingSample = null;
        this.currentTarget = null;
        ClientChatLogger.II1Il11l111II11IIl("Запись «" + recordingName + "» начата. Записываются бои: удар по игроку + 3 с после последнего удара.");
    }
// https://t.me/pasterplace

    private void stopCurrentOperation() {
        if (this.currentRecordingName != null) {
            this.flushRecordingBatch();
            ClientChatLogger.II1Il11l111II11IIl("Запись «" + this.currentRecordingName + "» остановлена, сохранено тиков: " + this.savedSampleCount);
            this.currentRecordingName = null;
            this.currentTarget = null;
        } else if (this.trainingInProgress) {
            this.cancelTrainingRequested = true;
            ClientChatLogger.II1Il11l111II11IIl("Обучение будет остановлено после текущей эпохи");
        } else if (this.activeModel != null) {
            ClientChatLogger.II1Il11l111II11IIl("Воспроизведение «" + this.activeModelName + "» выключено");
            this.activeModel = null;
            this.activeModelName = null;
            this.resetInferenceState();
        } else {
            ClientChatLogger.II1Il11l111II11IIl("Нечего останавливать");
        }
    }
// https://t.me/pasterplace

    private void listRecordings() {
        try {
            if (!Files.isDirectory(this.datasetDirectory)) {
                ClientChatLogger.II1Il11l111II11IIl("Записей нет");
                return;
            }

            try (Stream<Path> stream = Files.list(this.datasetDirectory)) {
                stream.forEach(path -> ClientChatLogger.II1Il11l111II11IIl(path.getFileName().toString()));
            }
        } catch (IOException listFailure) {
            ClientChatLogger.l1I1IlllIlI(listFailure.getMessage());
        }
    }
// https://t.me/pasterplace

    @EventTarget
    public void onAttack(EventAttack eventAttack) {
        if (eventAttack.isPost() && this.client.player != null) {
            this.lastAttackPlayerAge = this.client.player.age;
            if (this.currentRecordingName != null) {
                Entity entity = eventAttack.target();
                if (entity instanceof PlayerEntity) {
                    PlayerEntity entity2 = (PlayerEntity)entity;
                    if (entity2 != this.client.player) {
                        if (this.currentTarget != entity2 && !this.recordedSamples.isEmpty()) {
                            this.flushRecordingBatch();
                        }
// https://t.me/pasterplace

                        if (this.recordedSamples.isEmpty() && this.currentRecordingSample == null) {
                            this.pendingTargetEntityId = entity2.getId();
                        }

                        this.currentTarget = entity2;
                        this.lastTargetAttackMillis = System.currentTimeMillis();
                    }
                }
            }
        }
    }
// https://t.me/pasterplace

    @EventTarget
    public void onWorldChange(EventWorldChange eventWorldChange) {
        this.flushRecordingBatch();
        this.currentTarget = null;
        this.resetInferenceState();
    }
// https://t.me/pasterplace

    @EventTarget(priority = 0)
    public void onUpdate(EventUpdate eventUpdate) {
        double mouseYawDelta = this.accumulatedMouseYawDelta;
        double mousePitchDelta = this.accumulatedMousePitchDelta;
        this.accumulatedMouseYawDelta = 0.0;
        this.accumulatedMousePitchDelta = 0.0;
        if (this.currentRecordingName != null && this.client.player != null && this.client.world != null) {
            if (this.pendingRotationSnapshot != null) {
                this.pendingRotationSnapshot.inputFeatures[14] = (float)mouseYawDelta;
                this.pendingRotationSnapshot.inputFeatures[15] = (float)mousePitchDelta;
                this.rotationSnapshotHistory.addLast(this.pendingRotationSnapshot);

                while (this.rotationSnapshotHistory.size() > 40) {
                    this.rotationSnapshotHistory.removeFirst();
                }

                this.pendingRotationSnapshot = null;
            }

            if (this.pendingTargetEntityId != -1) {
                this.appendTargetHistoryToRecording(this.pendingTargetEntityId);
                this.pendingTargetEntityId = -1;
            }

            Rotation lIll1Il1lll111l11l1I1llllI2 = new Rotation(
            this.client.player.getYaw(), this.client.player.getPitch()
            );
            if (this.client.currentScreen == null) {
                this.pendingRotationSnapshot = this.captureRotationSnapshot(lIll1Il1lll111l11l1I1llllI2);
            } else {
                this.rotationSnapshotHistory.clear();
            }

            if (this.currentRecordingSample != null) {
                this.currentRecordingSample[14] = (float)mouseYawDelta;
                this.currentRecordingSample[15] = (float)mousePitchDelta;
                this.recordedSamples.add(this.currentRecordingSample);
                this.currentRecordingSample = null;
            }

            boolean withinRecordingWindow = this.currentTarget != null && System.currentTimeMillis() - this.lastTargetAttackMillis <= 3000L;
            if (withinRecordingWindow && this.client.currentScreen == null) {
                boolean targetInvalid = !this.currentTarget.isAlive()
                || this.currentTarget.isRemoved()
                || this.currentTarget.distanceTo(this.client.player) > 10.0;
                Rotation lIll1Il1lll111l11l1I1llllI22 = new Rotation(
                this.client.player.getYaw(), this.client.player.getPitch()
                );
                Rotation playerRotation = targetInvalid ? null : this.calculateTargetRotation(this.currentTarget, lIll1Il1lll111l11l1I1llllI22);
                this.currentRecordingSample = this.createRecordingSample(
                lIll1Il1lll111l11l1I1llllI22,
                playerRotation,
                targetInvalid ? 0.0F : (float)RotationUtils.distanceToBox(this.client.player.getEyePos(), this.currentTarget.getBoundingBox())
                );
            } else {
                this.flushRecordingBatch();
                if (!withinRecordingWindow) {
                    this.currentTarget = null;
                }
            }
        } else {
            this.rotationSnapshotHistory.clear();
            this.pendingRotationSnapshot = null;
            this.pendingTargetEntityId = -1;
        }
    }
// https://t.me/pasterplace

    private NeuroRotationController.RotationTrainingSnapshot captureRotationSnapshot(Rotation lIll1Il1lll111l11l1I1llllI2) {
        HashMap<Integer, float[]> hashMap = new HashMap<>();

        for (PlayerEntity player : this.client.world.getPlayers()) {
            if (player != this.client.player && player.isAlive() && !(player.distanceTo(this.client.player) > 10.0)) {
                Rotation lIll1Il1lll111l11l1I1llllI22 = this.calculateTargetRotation((LivingEntity)player, lIll1Il1lll111l11l1I1llllI2);
                hashMap.put(
                player.getId(),
                new float[]{
                    lIll1Il1lll111l11l1I1llllI22.getYaw(),
                    lIll1Il1lll111l11l1I1llllI22.getPitch(),
                    (float)RotationUtils.distanceToBox(this.client.player.getEyePos(), player.getBoundingBox())
                }
                );
            }
        }

        return new NeuroRotationController.RotationTrainingSnapshot(this.createRecordingSample(lIll1Il1lll111l11l1I1llllI2, null, 0.0F), hashMap);
    }
// https://t.me/pasterplace

    private void appendTargetHistoryToRecording(int targetEntityId) {
        if (this.recordedSamples.isEmpty()) {
            ArrayList arrayList = new ArrayList();
            Iterator snapshotIterator = this.rotationSnapshotHistory.descendingIterator();
// https://t.me/pasterplace

            while (snapshotIterator.hasNext()) {
                Object snapshot = (NeuroRotationController.RotationTrainingSnapshot)snapshotIterator.next();
                Object targetState = ((NeuroRotationController.RotationTrainingSnapshot)snapshot).targetLabelsByEntityId.get(targetEntityId);
                if (targetState == null) {
                    break;
                }
// https://t.me/pasterplace

                Object sampleFeatures = (float[])((NeuroRotationController.RotationTrainingSnapshot)snapshot).inputFeatures.clone();
                ((Object[])sampleFeatures)[2] = ((Object[])targetState)[0];
                ((Object[])sampleFeatures)[3] = ((Object[])targetState)[1];
                ((Object[])sampleFeatures)[4] = 0.0F;
                ((Object[])sampleFeatures)[5] = ((Object[])targetState)[2];
                arrayList.add(sampleFeatures);
            }

            Collections.reverse(arrayList);
            this.recordedSamples.addAll(arrayList);
            this.rotationSnapshotHistory.clear();
        }
    }
// https://t.me/pasterplace

    private Rotation calculateTargetRotation(LivingEntity entity, Rotation lIll1Il1lll111l11l1I1llllI2) {
        Vec3d playerEyePosition = this.client.player.getEyePos();
        Rotation lIll1Il1lll111l11l1I1llllI22 = RotationUtils.lookAt(
        playerEyePosition, RotationUtils.getAimPoint(entity, playerEyePosition, true)
        );
        return new Rotation(
        lIll1Il1lll111l11l1I1llllI2.getYaw() + MathHelper.wrapDegrees(lIll1Il1lll111l11l1I1llllI22.getYaw() - lIll1Il1lll111l11l1I1llllI2.getYaw()),
        lIll1Il1lll111l11l1I1llllI22.getPitch()
        );
    }
// https://t.me/pasterplace

    private float[] createRecordingSample(
    Rotation lIll1Il1lll111l11l1I1llllI2, Rotation lIll1Il1lll111l11l1I1llllI22, float targetDistance
    ) {
        return new float[]{
            lIll1Il1lll111l11l1I1llllI2.getYaw(),
            lIll1Il1lll111l11l1I1llllI2.getPitch(),
            lIll1Il1lll111l11l1I1llllI22 == null ? Float.NaN : lIll1Il1lll111l11l1I1llllI22.getYaw(),
            lIll1Il1lll111l11l1I1llllI22 == null ? Float.NaN : lIll1Il1lll111l11l1I1llllI22.getPitch(),
            lIll1Il1lll111l11l1I1llllI22 == null ? 1.0F : 0.0F,
            targetDistance,
            (this.client.options.forwardKey.isPressed() ? 1.0F : 0.0F)
            - (this.client.options.backKey.isPressed() ? 1.0F : 0.0F),
            (this.client.options.leftKey.isPressed() ? 1.0F : 0.0F)
            - (this.client.options.rightKey.isPressed() ? 1.0F : 0.0F),
            this.client.options.jumpKey.isPressed() ? 1.0F : 0.0F,
            this.client.player.isSprinting() ? 1.0F : 0.0F,
            this.client.player.isOnGround() ? 1.0F : 0.0F,
            CombatUtils.getAttackCooldownProgress(),
            (float)this.client.player.getVelocity().horizontalLength(),
            Math.min(60, this.client.player.age - this.lastAttackPlayerAge),
            0.0F,
            0.0F
        };
    }
// https://t.me/pasterplace

    private void flushRecordingBatch() {
        if (this.currentRecordingSample != null) {
            this.currentRecordingSample = null;
        }

        if (this.currentRecordingName != null && this.recordedSamples.size() >= 8) {
            float[] lastSample = this.recordedSamples.get(this.recordedSamples.size() - 1);
            float finalTargetYaw = lastSample[0] + lastSample[14];
            float finalTargetPitch = lastSample[1] + lastSample[15];

            for (float[] recordedSample : this.recordedSamples) {
                if (recordedSample[4] > 0.5F) {
                    recordedSample[2] = finalTargetYaw;
                    recordedSample[3] = finalTargetPitch;
                }
            }

            try {
                Files.createDirectories(this.datasetDirectory);

                try (
                OutputStream outputStream = Files.newOutputStream(
                this.datasetDirectory.resolve(this.currentRecordingName + ".rec"), StandardOpenOption.CREATE, StandardOpenOption.APPEND
                );
                DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
                ) {
                    dataOutputStream.writeInt(this.recordedSamples.size());

                    for (float[] sampleFrame : this.recordedSamples) {
                        for (float featureValue : sampleFrame) {
                            dataOutputStream.writeFloat(featureValue);
                        }
                    }
                }

                this.savedSampleCount = this.savedSampleCount + this.recordedSamples.size();
            } catch (IOException saveException) {
                ClientChatLogger.l1I1IlllIlI("Не удалось сохранить запись: " + saveException.getMessage());
            }

            this.recordedSamples.clear();
        } else {
            this.recordedSamples.clear();
        }
    }
// https://t.me/pasterplace

    static void buildTrainingInputFeatures(float[] previousSample, float[] currentSample, float mouseYawDelta, float mousePitchDelta, float[] trainingInput) {
        trainingInput[0] = MathHelper.wrapDegrees(currentSample[2] - currentSample[0]) / 30.0F;
        trainingInput[1] = (currentSample[3] - currentSample[1]) / 30.0F;
        trainingInput[2] = mouseYawDelta / 5.0F;
        trainingInput[3] = mousePitchDelta / 5.0F;
        if (previousSample != null && previousSample[4] == currentSample[4]) {
            trainingInput[4] = MathHelper.wrapDegrees(currentSample[2] - previousSample[2]) / 5.0F;
            trainingInput[5] = (currentSample[3] - previousSample[3]) / 5.0F;
        } else {
            trainingInput[4] = 0.0F;
            trainingInput[5] = 0.0F;
        }

        trainingInput[6] = currentSample[4];
        trainingInput[7] = currentSample[5] / 6.0F;
        trainingInput[8] = currentSample[6];
        trainingInput[9] = currentSample[7];
        trainingInput[10] = currentSample[8];
        trainingInput[11] = currentSample[9];
        trainingInput[12] = currentSample[10];
        trainingInput[13] = currentSample[11];
        trainingInput[14] = currentSample[12] * 5.0F;
        trainingInput[15] = currentSample[13] / 20.0F;
        trainingInput[16] = currentSample[1] / 90.0F;
    }
// https://t.me/pasterplace

    private List<float[][]> readRecordingBatches(Path path) throws IOException {
        ArrayList<float[][]> arrayList = new ArrayList<>();

        try (
        InputStream inputStream = Files.newInputStream(path);
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
        ) {
            while (true) {
                int sampleCount;
                try {
                    sampleCount = dataInputStream.readInt();
                } catch (EOFException endOfRecording) {
                    return arrayList;
                }

                float[][] recordingSamples = new float[sampleCount][16];

                for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
                    for (int featureIndex = 0; featureIndex < 16; featureIndex++) {
                        recordingSamples[sampleIndex][featureIndex] = dataInputStream.readFloat();
                    }
                }

                arrayList.add(recordingSamples);
            }
        }
    }
// https://t.me/pasterplace

    private void startTraining(String modelName, String recordingName, boolean fineTuneExistingModel, int epochCount) {
        if (this.trainingInProgress) {
            ClientChatLogger.l1I1IlllIlI("Обучение уже идёт");
        } else {
            Path path = this.datasetDirectory.resolve(recordingName + ".rec");
            if (!Files.exists(path)) {
                ClientChatLogger.l1I1IlllIlI("Нет записи «" + recordingName + "»");
            } else {
                Path path2 = this.datasetDirectory.resolve(modelName + ".nn");
                if (fineTuneExistingModel && !Files.exists(path2)) {
                    ClientChatLogger.l1I1IlllIlI("Нет модели «" + modelName + "». Сначала .neuro train " + modelName);
                } else {
                    if (recordingName.equals(this.currentRecordingName)) {
                        this.flushRecordingBatch();
                    }

                    this.trainingInProgress = true;
                    this.cancelTrainingRequested = false;
                    this.trainingModelName = modelName;
                    this.isFineTuning = fineTuneExistingModel;
                    this.trainingStatus = "Загрузка данных";
                    this.trainingWorker = null;
                    this.trainingSucceeded = false;
                    this.trainingFinishedAtMillis = 0L;
                    Thread thread = new Thread(
                    () -> {
                        boolean modelSavedSuccessfully = false;

                        try {
                List<float[][]> recordingBatches = this.readRecordingBatches(path);
                ArrayList<float[][]> trainingInputSequences = new ArrayList<>();
                ArrayList<float[][]> trainingTargetSequences = new ArrayList<>();
                            int totalSampleCount = 0;

                            for (float[][] combatRecording : recordingBatches) {
                                float[][] inputFeatureSequence = new float[combatRecording.length][17];
                                float[][] targetSequence = new float[combatRecording.length][2];

                                for (int sampleIndex = 0; sampleIndex < combatRecording.length; sampleIndex++) {
                                    float previousYawDelta = sampleIndex > 0 ? combatRecording[sampleIndex - 1][14] : 0.0F;
                                    float previousPitchDelta = sampleIndex > 0 ? combatRecording[sampleIndex - 1][15] : 0.0F;
                                    buildTrainingInputFeatures(
                                    sampleIndex > 0 ? combatRecording[sampleIndex - 1] : null,
                                    combatRecording[sampleIndex],
                                    previousYawDelta,
                                    previousPitchDelta,
                                    inputFeatureSequence[sampleIndex]
                                    );
                                    targetSequence[sampleIndex][0] = combatRecording[sampleIndex][14] / 5.0F;
                                    targetSequence[sampleIndex][1] = combatRecording[sampleIndex][15] / 5.0F;
                                }

                                trainingInputSequences.add(inputFeatureSequence);
                                trainingTargetSequences.add(targetSequence);
                                totalSampleCount += combatRecording.length;
                            }

                            this.trainingSampleCount = totalSampleCount;
                            this.publishTrainingStatus(
                            (fineTuneExistingModel ? "Дообучение «" : "Обучение «") + modelName + "» на «" + recordingName + "»: " + recordingBatches.size() + " боёв, " + totalSampleCount + " тиков"
                            );
                            RecurrentMixtureDensityModel model;
                            if (fineTuneExistingModel) {
                                try (
                                InputStream inputStream = Files.newInputStream(path2);
                                DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
                                ) {
                                    model = RecurrentMixtureDensityModel.load(dataInputStream);
                                }

                                if (model.inputSize != 17) {
                                    throw new IOException("модель устарела, обучите заново через train");
                                }
                            } else {
                                model = new RecurrentMixtureDensityModel(17, 64, 6);
                                model.initializeWeights(System.nanoTime());
                            }

                            NeuralNetworkTrainer trainer = new NeuralNetworkTrainer(model);
                            if (!fineTuneExistingModel) {
                                trainer.initializeInputNormalization(trainingInputSequences);
                            }

                            this.trainingWorker = trainer;
                            this.trainingStatus = fineTuneExistingModel ? "Дообучение" : "Обучение";
                            int[] epochUpdateCount = new int[]{0};
                            float validationLoss = trainer.train(
                            trainingInputSequences, trainingTargetSequences, epochCount, fineTuneExistingModel ? 8.0E-4F : 0.003F, currentEpoch -> this.cancelTrainingRequested, progressMessage -> {
                                epochUpdateCount[0]++;
                                if (epochUpdateCount[0] == 1 || epochUpdateCount[0] % 5 == 0) {
                                    this.publishTrainingStatus(progressMessage);
                                }
                            }
                            );
                            if (!Float.isNaN(validationLoss)) {
                                this.trainingStatus = "Сохранение";
                                Files.createDirectories(this.datasetDirectory);

                                try (
                                OutputStream outputStream = Files.newOutputStream(path2);
                                DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
                                ) {
                                    model.save(dataOutputStream);
                                }

                                modelSavedSuccessfully = true;
                                this.trainingStatus = "Готово";
                                this.publishTrainingStatus(
                                "Модель «"
                                + modelName
                                + "» "
                                + (fineTuneExistingModel ? "дообучена" : "обучена")
                                + ", лучшая val NLL: "
                                + String.format("%.4f", validationLoss)
                                + ". Запуск: .neuro play "
                                + modelName
                                );
                                if (modelName.equals(this.activeModelName)) {
                                    this.client.execute(() -> this.loadAndActivateModel(modelName));
                                }

                                return;
                            }
// https://t.me/pasterplace

                            this.trainingStatus = "Мало данных";
                            this.publishTrainingStatus("§cСлишком мало данных для обучения");
                        } catch (Throwable trainingFailure) {
                            this.trainingStatus = "Ошибка";
                            this.publishTrainingStatus("§cОшибка обучения: " + trainingFailure.getMessage());
                            return;
                        } finally {
                            this.trainingSucceeded = modelSavedSuccessfully;
                            this.trainingFinishedAtMillis = System.currentTimeMillis();
                            this.trainingInProgress = false;
                        }
                    },
                    "Daamky-Neuro-Train"
                    );
                    thread.setDaemon(true);
                    thread.setPriority(1);
                    thread.start();
                }
            }
        }
    }
// https://t.me/pasterplace

    public boolean isRecording() {
        return this.currentRecordingName != null;
    }
// https://t.me/pasterplace

    public String getRecordingName() {
        return this.currentRecordingName;
    }
// https://t.me/pasterplace

    public int getRecordedSampleCount() {
        return this.savedSampleCount + this.recordedSamples.size();
    }
// https://t.me/pasterplace

    public boolean isTrainingInProgress() {
        return this.trainingInProgress;
    }
// https://t.me/pasterplace

    public boolean isTrainingOverlayVisible() {
        return this.trainingInProgress || this.trainingFinishedAtMillis > 0L && System.currentTimeMillis() - this.trainingFinishedAtMillis < 6000L;
    }
// https://t.me/pasterplace

    public String getTrainingModelName() {
        return this.trainingModelName == null ? "" : this.trainingModelName;
    }
// https://t.me/pasterplace

    public boolean hasTrainingError() {
        return "������".equals(this.trainingStatus) || "���� ������".equals(this.trainingStatus);
    }
// https://t.me/pasterplace

    public String getDisplayInfo() {
        NeuralNetworkTrainer trainer = this.trainingWorker;
        return this.trainingInProgress && trainer != null && trainer.totalEpochs > 0
        ? (this.isFineTuning ? "Дообучение " : "Эпоха ") + (trainer.currentEpoch + 1) + "/" + trainer.totalEpochs
        : this.trainingStatus;
    }
// https://t.me/pasterplace

    public float getTrainingProgress() {
        if (!this.trainingInProgress && this.trainingSucceeded) {
            return 1.0F;
        }

        NeuralNetworkTrainer trainer = this.trainingWorker;
        return trainer == null ? 0.0F : trainer.progress;
    }
// https://t.me/pasterplace

    public String getTrainingSummary() {
        String summaryText = this.trainingSampleCount + " тиков";
        NeuralNetworkTrainer trainer = this.trainingWorker;
        if (trainer != null && !Float.isNaN(trainer.validationLoss)) {
            summaryText = summaryText + " · val " + String.format("%.3f", trainer.validationLoss);
        }
// https://t.me/pasterplace

        return summaryText;
    }
// https://t.me/pasterplace

    private void publishTrainingStatus(String statusMessage) {
        this.client.execute(() -> ClientChatLogger.II1Il11l111II11IIl(statusMessage));
    }
// https://t.me/pasterplace

    private void loadAndActivateModel(String modelName) {
        Path path = this.datasetDirectory.resolve(modelName + ".nn");
        if (!Files.exists(path)) {
            ClientChatLogger.l1I1IlllIlI("Нет модели «" + modelName + "». Сначала .neuro train " + modelName);
        } else {
            try (
            InputStream inputStream = Files.newInputStream(path);
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
            ) {
                RecurrentMixtureDensityModel badModel = RecurrentMixtureDensityModel.load(dataInputStream);
                if (badModel.inputSize == 17) {
                    this.activeModel = badModel;
                    this.activeModelName = modelName;
                    this.resetInferenceState();
                    Aura aura = Aura.getInstance();
                    if (aura != null) {
                        aura.selectNeuroRotation();
                    }

                    ClientChatLogger.II1Il11l111II11IIl("Воспроизведение «" + modelName + "» включено, ротация Aura: Neuro");
                } else {
                    ClientChatLogger.l1I1IlllIlI("Модель устарела, переобучите её");
                }
            } catch (IOException loadFailure) {
                ClientChatLogger.l1I1IlllIlI("Не удалось загрузить модель: " + loadFailure.getMessage());
            }
        }
    }
// https://t.me/pasterplace

    public void resetInferenceState() {
        this.recurrentHiddenState = this.activeModel == null ? null : new float[this.activeModel.hiddenSize];
        this.previousFeatureVector = null;
        this.previousYawDelta = 0.0F;
        this.previousPitchDelta = 0.0F;
        this.inferenceStepCount = -1;
    }
// https://t.me/pasterplace

    public Rotation predictRotationForTarget(
    Rotation lIll1Il1lll111l11l1I1llllI2, Rotation lIll1Il1lll111l11l1I1llllI22, LivingEntity livingEntity
    ) {
        if (this.activeModel != null && this.client.player != null) {
            this.inferenceStepCount = -1;
            float targetDistance = (float)RotationUtils.distanceToBox(this.client.player.getEyePos(), livingEntity.getBoundingBox());
            return this.inferRotation(lIll1Il1lll111l11l1I1llllI2, lIll1Il1lll111l11l1I1llllI22, targetDistance, null, livingEntity);
        } else {
            return null;
        }
    }
// https://t.me/pasterplace

    public Rotation predictRotationWithoutTarget(Rotation lIll1Il1lll111l11l1I1llllI2) {
        if (this.activeModel != null && this.client.player != null && this.recurrentHiddenState != null && this.previousFeatureVector != null) {
            if (this.inferenceStepCount < 0) {
                this.inferenceStepCount = 0;
            }

            if (++this.inferenceStepCount > 40) {
                this.resetInferenceState();
                return null;
            } else {
                Rotation lIll1Il1lll111l11l1I1llllI22 = new Rotation(
                lIll1Il1lll111l11l1I1llllI2.getYaw()
                + MathHelper.wrapDegrees(this.client.player.getYaw() - lIll1Il1lll111l11l1I1llllI2.getYaw()),
                this.client.player.getPitch()
                );
                if (Math.abs(lIll1Il1lll111l11l1I1llllI22.getYaw() - lIll1Il1lll111l11l1I1llllI2.getYaw()) < 1.0F
                && Math.abs(lIll1Il1lll111l11l1I1llllI22.getPitch() - lIll1Il1lll111l11l1I1llllI2.getPitch()) < 1.0F) {
                    this.resetInferenceState();
                    return null;
                } else {
                    return this.inferRotation(lIll1Il1lll111l11l1I1llllI2, null, 0.0F, lIll1Il1lll111l11l1I1llllI22, null);
                }
            }
        } else {
            return null;
        }
    }
// https://t.me/pasterplace

    private Rotation inferRotation(
    Rotation lIll1Il1lll111l11l1I1llllI2,
    Rotation lIll1Il1lll111l11l1I1llllI22,
    float targetDistance,
    Rotation lIll1Il1lll111l11l1I1llllI23,
    LivingEntity livingEntity
    ) {
        float[] currentFeatures = this.createRecordingSample(lIll1Il1lll111l11l1I1llllI2, lIll1Il1lll111l11l1I1llllI22, targetDistance);
        if (lIll1Il1lll111l11l1I1llllI22 == null) {
            currentFeatures[2] = lIll1Il1lll111l11l1I1llllI23.getYaw();
            currentFeatures[3] = lIll1Il1lll111l11l1I1llllI23.getPitch();
        }

        float[] trainingInput = new float[17];
        buildTrainingInputFeatures(this.previousFeatureVector, currentFeatures, this.previousYawDelta, this.previousPitchDelta, trainingInput);
        float[] normalizedInput = new float[17];
        this.activeModel.normalizeInput(trainingInput, normalizedInput);
        int hiddenSize = this.activeModel.hiddenSize;
        float[] nextHiddenState = new float[hiddenSize];
        this.activeModel.stepRecurrentCell(normalizedInput, this.recurrentHiddenState, nextHiddenState, new float[hiddenSize], new float[hiddenSize], new float[hiddenSize], new float[hiddenSize]);
        this.recurrentHiddenState = nextHiddenState;
        float[] mixtureParameters = new float[this.activeModel.outputSize];
        this.activeModel.projectOutput(this.recurrentHiddenState, mixtureParameters);
        float[] sampledOutput = new float[2];
        this.activeModel.sampleOutput(mixtureParameters, this.random, sampledOutput, livingEntity != null ? 0.6F : 1.0F);
        float yawDelta = MathHelper.clamp(sampledOutput[0] * 5.0F, -180.0F, 180.0F);
        float pitchDelta = MathHelper.clamp(sampledOutput[1] * 5.0F, -90.0F, 90.0F);
        float rotationStep = (float)RotationUtils.mouseSensitivityStep();
        if (rotationStep > 0.0F) {
            yawDelta = Math.round(yawDelta / rotationStep) * rotationStep;
            pitchDelta = Math.round(pitchDelta / rotationStep) * rotationStep;
        }

        float predictedPitch = MathHelper.clamp(lIll1Il1lll111l11l1I1llllI2.getPitch() + pitchDelta, -90.0F, 90.0F);
        this.previousYawDelta = yawDelta;
        this.previousPitchDelta = predictedPitch - lIll1Il1lll111l11l1I1llllI2.getPitch();
        this.previousFeatureVector = currentFeatures;
        return new Rotation(lIll1Il1lll111l11l1I1llllI2.getYaw() + yawDelta, predictedPitch);
    }
// https://t.me/pasterplace

    private record RotationTrainingSnapshot(float[] inputFeatures, Map<Integer, float[]> targetLabelsByEntityId) {

        private RotationTrainingSnapshot(float[] inputFeatures, Map<Integer, float[]> targetLabelsByEntityId) {
            this.inputFeatures = inputFeatures;
            this.targetLabelsByEntityId = targetLabelsByEntityId;
        }
    }
}

