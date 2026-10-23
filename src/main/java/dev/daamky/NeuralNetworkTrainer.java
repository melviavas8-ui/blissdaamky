package dev.daamky;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public final class NeuralNetworkTrainer {
    private static final String pc434e70d = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int MAX_WINDOW_LENGTH = 64;
   private static final int WINDOW_STRIDE = 32;
   private static final int WINDOW_OVERLAP = 16;
   private static final int BATCH_SIZE = 16;
   private static final float MAX_GRADIENT_NORM = 5.0F;
   private static final float FEATURE_NOISE_SCALE = 0.3F;
   private static final double LOG_TWO_PI = Math.log(Math.PI * 2);
   private final RecurrentMixtureDensityModel model;
   private final float[][] parameters;
   private final float[][] firstMoments;
   private final float[][] secondMoments;
   private int optimizerStep;
   public volatile int currentEpoch;
   public volatile int totalEpochs;
   public volatile float progress;
   public volatile float trainingLoss = Float.NaN;
   public volatile float validationLoss = Float.NaN;
   public volatile float bestValidationLoss = Float.NaN;
// https://t.me/pasterplace

   public NeuralNetworkTrainer(RecurrentMixtureDensityModel model) {
      this.model = model;
      this.parameters = model.getParameterArrays();
      this.firstMoments = new float[this.parameters.length][];
      this.secondMoments = new float[this.parameters.length][];

      for (int parameterIndex = 0; parameterIndex < this.parameters.length; parameterIndex++) {
         this.firstMoments[parameterIndex] = new float[this.parameters[parameterIndex].length];
         this.secondMoments[parameterIndex] = new float[this.parameters[parameterIndex].length];
      }
   }
// https://t.me/pasterplace

   public void initializeInputNormalization(List<float[][]> trainingSequences) {
      int featureCount = this.model.inputSize;
      double[] featureSums = new double[featureCount];
      double[] featureSquaredSums = new double[featureCount];
      long sampleCount = 0L;

      for (float[][] sequence : trainingSequences) {
         for (float[] inputVector : sequence) {
            for (int featureIndex = 0; featureIndex < featureCount; featureIndex++) {
               featureSums[featureIndex] += inputVector[featureIndex];
               featureSquaredSums[featureIndex] += inputVector[featureIndex] * inputVector[featureIndex];
            }

            sampleCount++;
         }
      }

      for (int featureIndex = 0; featureIndex < featureCount; featureIndex++) {
         double mean = featureSums[featureIndex] / Math.max(1L, sampleCount);
         double variance = featureSquaredSums[featureIndex] / Math.max(1L, sampleCount) - mean * mean;
         this.model.inputMean[featureIndex] = (float)mean;
         this.model.inputStandardDeviation[featureIndex] = (float)Math.max(0.001, Math.sqrt(Math.max(0.0, variance)));
      }
   }
// https://t.me/pasterplace

   public float train(List<float[][]> inputSequences, List<float[][]> targetSequences, int epochCount, float initialLearningRate, IntPredicate shouldStop, Consumer<String> statusConsumer) {
      ArrayList windows = new ArrayList();

      for (int sequenceIndex = 0; sequenceIndex < inputSequences.size(); sequenceIndex++) {
         float[][] normalizedInputs = this.normalizeSequence((float[][])inputSequences.get(sequenceIndex));
         Object targets = (float[][])targetSequences.get(sequenceIndex);
         int sequenceLength = normalizedInputs.length;
// https://t.me/pasterplace

         for (int windowStart = 0; windowStart < sequenceLength; windowStart += 32) {
            int windowEnd = Math.min(sequenceLength, windowStart + 64);
            if (windowEnd - windowStart < 8) {
               break;
            }
// https://t.me/pasterplace

            int overlapStart = windowStart == 0 ? 0 : Math.max(0, windowStart - 16);
            float[][] windowInputs = new float[windowEnd - overlapStart][];
            float[][] windowTargets = new float[windowEnd - overlapStart][];
            System.arraycopy(normalizedInputs, overlapStart, windowInputs, 0, windowEnd - overlapStart);
            System.arraycopy(targets, overlapStart, windowTargets, 0, windowEnd - overlapStart);
            windows.add(new NeuralNetworkTrainer.TrainingWindow(windowInputs, windowTargets, windowStart - overlapStart));
            if (windowEnd == sequenceLength) {
               break;
            }
         }
      }

      if (windows.isEmpty()) {
         return Float.NaN;
      }

      Random shuffleRandom = new Random(1337L);
      Collections.shuffle(windows, shuffleRandom);
      int validationWindowCount = windows.size() >= 20 ? Math.max(1, windows.size() / 10) : 0;
      List validationWindows = windows.subList(0, validationWindowCount);
      ArrayList trainingWindows = new ArrayList(windows.subList(validationWindowCount, windows.size()));
      float bestValidationLoss = validationWindows.isEmpty() ? Float.MAX_VALUE : this.calculateAverageLoss(validationWindows);
      if (bestValidationLoss != Float.MAX_VALUE) {
         this.bestValidationLoss = bestValidationLoss;
      }

      float[][] bestParameters = copyMatrix(this.parameters);
      this.totalEpochs = epochCount;

      for (int epochIndex = 0; epochIndex < epochCount; epochIndex++) {
         this.currentEpoch = epochIndex;
         if (shouldStop.test(epochIndex)) {
            break;
         }

         Collections.shuffle(trainingWindows, shuffleRandom);
         float epochLearningRate = (float)(initialLearningRate * 0.5 * (1.0 + Math.cos(Math.PI * epochIndex / epochCount)));
         double epochLossSum = 0.0;
         long epochPredictionCount = 0L;

         for (int batchStart = 0; batchStart < trainingWindows.size(); batchStart += 16) {
            List batchWindows = trainingWindows.subList(batchStart, Math.min(trainingWindows.size(), batchStart + 16));
            float[][][] windowGradientResults = new float[batchWindows.size()][][];
            double[] windowLosses = new double[batchWindows.size()];
            long[] windowPredictionCounts = new long[batchWindows.size()];
            long batchRandomSeed = shuffleRandom.nextLong();
            IntStream.range(0, batchWindows.size())
               .parallel()
               .forEach(
                  windowIndex -> {
                     float[][] windowGradientMatrix = createZeroMatrixLike(this.parameters);
                     double[] lossAndPredictionCount = this.calculateWindowLossAndGradients(
                        augmentTrainingWindow((NeuralNetworkTrainer.TrainingWindow)batchWindows.get(windowIndex), new Random(batchRandomSeed + windowIndex)), windowGradientMatrix
                     );
                     windowGradientResults[windowIndex] = windowGradientMatrix;
                     windowLosses[windowIndex] = lossAndPredictionCount[0];
                     windowPredictionCounts[windowIndex] = (long)lossAndPredictionCount[1];
                  }
               );
            float[][] batchGradients = createZeroMatrixLike(this.parameters);
            long batchPredictionCount = 0L;

            for (int batchWindowIndex = 0; batchWindowIndex < batchWindows.size(); batchWindowIndex++) {
               accumulateGradients(batchGradients, windowGradientResults[batchWindowIndex]);
               epochLossSum += windowLosses[batchWindowIndex];
               batchPredictionCount += windowPredictionCounts[batchWindowIndex];
            }

            epochPredictionCount += batchPredictionCount;
            this.progress = (epochIndex + (float)Math.min(trainingWindows.size(), batchStart + 16) / trainingWindows.size()) / epochCount;
            if (batchPredictionCount > 0L) {
               this.applyAdamUpdate(batchGradients, 1.0F / (float)batchPredictionCount, epochLearningRate);
            }
         }

         float epochTrainingLoss = (float)(epochLossSum / Math.max(1L, epochPredictionCount));
         float epochValidationLoss = validationWindows.isEmpty() ? epochTrainingLoss : this.calculateAverageLoss(validationWindows);
         this.trainingLoss = epochTrainingLoss;
         this.validationLoss = epochValidationLoss;
         if (epochValidationLoss < bestValidationLoss) {
            bestValidationLoss = epochValidationLoss;
            bestParameters = copyMatrix(this.parameters);
            this.bestValidationLoss = bestValidationLoss;
         }

         statusConsumer.accept("Эпоха " + (epochIndex + 1) + "/" + epochCount + " | train " + String.format("%.4f", epochTrainingLoss) + " | val " + String.format("%.4f", epochValidationLoss));
      }

      for (int parameterIndex = 0; parameterIndex < this.parameters.length; parameterIndex++) {
         System.arraycopy(bestParameters[parameterIndex], 0, this.parameters[parameterIndex], 0, this.parameters[parameterIndex].length);
      }
// https://t.me/pasterplace

      return bestValidationLoss;
   }
// https://t.me/pasterplace

   private static NeuralNetworkTrainer.TrainingWindow augmentTrainingWindow(NeuralNetworkTrainer.TrainingWindow window, Random random) {
      float[][] noisyInputs = new float[window.inputs.length][];

      for (int inputRowIndex = 0; inputRowIndex < noisyInputs.length; inputRowIndex++) {
         float[] noisyInput = window.inputs[inputRowIndex].clone();
         noisyInput[2] += (float)random.nextGaussian() * 0.3F;
         noisyInput[3] += (float)random.nextGaussian() * 0.3F;
         noisyInputs[inputRowIndex] = (float[])noisyInput;
      }

      return new NeuralNetworkTrainer.TrainingWindow(noisyInputs, window.targets, window.predictionOffset);
   }
// https://t.me/pasterplace

   private float calculateAverageLoss(List<NeuralNetworkTrainer.TrainingWindow> windows) {
      double negativeLogLikelihoodSum = 0.0;
      long predictionCount = 0L;

      for (NeuralNetworkTrainer.TrainingWindow window : windows) {
         double[] lossAndCount = this.calculateWindowLossAndGradients(window, (float[][])null);
         negativeLogLikelihoodSum += lossAndCount[0];
         predictionCount += (long)lossAndCount[1];
      }

      return (float)(negativeLogLikelihoodSum / Math.max(1L, predictionCount));
   }
// https://t.me/pasterplace

   private float[][] normalizeSequence(float[][] sequence) {
      float[][] normalizedSequence = new float[sequence.length][this.model.inputSize];
// https://t.me/pasterplace

      for (int rowIndex = 0; rowIndex < sequence.length; rowIndex++) {
         this.model.normalizeInput(sequence[rowIndex], normalizedSequence[rowIndex]);
      }

      return normalizedSequence;
   }
// https://t.me/pasterplace

   private double[] calculateWindowLossAndGradients(NeuralNetworkTrainer.TrainingWindow window, float[][] parameterGradients) {
      int sequenceLength = window.inputs.length;
      int hiddenSize = this.model.hiddenSize;
      int inputSize = this.model.inputSize;
      int mixtureCount = this.model.mixtureCount;
      int outputSize = this.model.outputSize;
      float[][] hiddenStates = new float[sequenceLength + 1][hiddenSize];
      float[][] updateGateActivations = new float[sequenceLength][hiddenSize];
      float[][] resetGateActivations = new float[sequenceLength][hiddenSize];
      float[][] candidateActivations = new float[sequenceLength][hiddenSize];
      float[][] candidateRecurrentSums = new float[sequenceLength][hiddenSize];
      float[][] outputLogits = new float[sequenceLength][outputSize];
// https://t.me/pasterplace

      for (int timeStep = 0; timeStep < sequenceLength; timeStep++) {
         this.model
            .stepRecurrentCell(window.inputs[timeStep], hiddenStates[timeStep], hiddenStates[timeStep + 1], updateGateActivations[timeStep], resetGateActivations[timeStep], candidateActivations[timeStep], candidateRecurrentSums[timeStep]);
         this.model.projectOutput(hiddenStates[timeStep + 1], outputLogits[timeStep]);
      }

      double totalNegativeLogLikelihood = 0.0;
      int predictionCount = 0;
      float[][] outputGradients = new float[sequenceLength][outputSize];

      for (int predictionStep = window.predictionOffset; predictionStep < sequenceLength; predictionStep++) {
         totalNegativeLogLikelihood += mixtureDensityLossAndGradient(outputLogits[predictionStep], window.targets[predictionStep], outputGradients[predictionStep], mixtureCount);
         predictionCount++;
      }

      if (parameterGradients == null) {
         return new double[]{totalNegativeLogLikelihood, predictionCount};
      }

      float[] inputWeightGradients = parameterGradients[0];
      float[] recurrentWeightGradients = parameterGradients[1];
      float[] gateBiasGradients = parameterGradients[2];
      float[] candidateRecurrentBiasParameterGradients = parameterGradients[3];
      float[] outputWeightGradients = parameterGradients[4];
      float[] outputBiasGradients = parameterGradients[5];
      float[] previousHiddenStateGradients = new float[hiddenSize];
      float[] currentHiddenStateGradients = new float[hiddenSize];
      float[] updateGateGradients = new float[hiddenSize];
      float[] resetGateGradients = new float[hiddenSize];
      float[] candidateStateGradients = new float[hiddenSize];
      float[] candidateRecurrentBiasGradients = new float[hiddenSize];

      for (int reverseTimeStep = sequenceLength - 1; reverseTimeStep >= 0; reverseTimeStep--) {
         float[] currentHiddenState = hiddenStates[reverseTimeStep + 1];
         float[] previousHiddenState = hiddenStates[reverseTimeStep];
         float[] outputGradient = outputGradients[reverseTimeStep];
         System.arraycopy(previousHiddenStateGradients, 0, currentHiddenStateGradients, 0, hiddenSize);
         if (reverseTimeStep >= window.predictionOffset) {
            for (int outputIndex = 0; outputIndex < outputSize; outputIndex++) {
               float outputGradientValue = outputGradient[outputIndex];
               if (outputGradientValue != 0.0F) {
                  outputBiasGradients[outputIndex] += outputGradientValue;
                  int outputWeightOffset = outputIndex * hiddenSize;

                  for (int hiddenIndex = 0; hiddenIndex < hiddenSize; hiddenIndex++) {
                     outputWeightGradients[outputWeightOffset + hiddenIndex] = outputWeightGradients[outputWeightOffset + hiddenIndex] + outputGradientValue * currentHiddenState[hiddenIndex];
                     currentHiddenStateGradients[hiddenIndex] += this.model.outputWeights[outputWeightOffset + hiddenIndex] * outputGradientValue;
                  }
               }
            }
         }

         Arrays.fill(previousHiddenStateGradients, 0.0F);
         float[] updateGateValues = updateGateActivations[reverseTimeStep];
         float[] resetGateValues = resetGateActivations[reverseTimeStep];
         float[] candidateValues = candidateActivations[reverseTimeStep];
         float[] candidateRecurrentValues = candidateRecurrentSums[reverseTimeStep];

         for (int hiddenIndex = 0; hiddenIndex < hiddenSize; hiddenIndex++) {
            float downstreamHiddenGradient = currentHiddenStateGradients[hiddenIndex];
            previousHiddenStateGradients[hiddenIndex] += downstreamHiddenGradient * updateGateValues[hiddenIndex];
            float candidatePreactivationGradient = downstreamHiddenGradient * (1.0F - updateGateValues[hiddenIndex]) * (1.0F - candidateValues[hiddenIndex] * candidateValues[hiddenIndex]);
            float updateGatePreactivationGradient = downstreamHiddenGradient * (previousHiddenState[hiddenIndex] - candidateValues[hiddenIndex]) * updateGateValues[hiddenIndex] * (1.0F - updateGateValues[hiddenIndex]);
            float resetGatePreactivationGradient = candidatePreactivationGradient * candidateRecurrentValues[hiddenIndex] * resetGateValues[hiddenIndex] * (1.0F - resetGateValues[hiddenIndex]);
            candidateStateGradients[hiddenIndex] = candidatePreactivationGradient;
            updateGateGradients[hiddenIndex] = updateGatePreactivationGradient;
            resetGateGradients[hiddenIndex] = resetGatePreactivationGradient;
            candidateRecurrentBiasGradients[hiddenIndex] = candidatePreactivationGradient * resetGateValues[hiddenIndex];
         }

         float[] inputValues = window.inputs[reverseTimeStep];
// https://t.me/pasterplace

         for (int hiddenIndex = 0; hiddenIndex < hiddenSize; hiddenIndex++) {
            gateBiasGradients[hiddenIndex] += updateGateGradients[hiddenIndex];
            gateBiasGradients[hiddenSize + hiddenIndex] = gateBiasGradients[hiddenSize + hiddenIndex] + resetGateGradients[hiddenIndex];
            gateBiasGradients[2 * hiddenSize + hiddenIndex] = gateBiasGradients[2 * hiddenSize + hiddenIndex] + candidateStateGradients[hiddenIndex];
            candidateRecurrentBiasParameterGradients[hiddenIndex] += candidateRecurrentBiasGradients[hiddenIndex];
            int updateWeightOffset = hiddenIndex * inputSize;
            int resetWeightOffset = (hiddenSize + hiddenIndex) * inputSize;
            int candidateWeightOffset = (2 * hiddenSize + hiddenIndex) * inputSize;

            for (int inputIndex = 0; inputIndex < inputSize; inputIndex++) {
               inputWeightGradients[updateWeightOffset + inputIndex] = inputWeightGradients[updateWeightOffset + inputIndex] + updateGateGradients[hiddenIndex] * inputValues[inputIndex];
               inputWeightGradients[resetWeightOffset + inputIndex] = inputWeightGradients[resetWeightOffset + inputIndex] + resetGateGradients[hiddenIndex] * inputValues[inputIndex];
               inputWeightGradients[candidateWeightOffset + inputIndex] = inputWeightGradients[candidateWeightOffset + inputIndex] + candidateStateGradients[hiddenIndex] * inputValues[inputIndex];
            }

            int updateRecurrentWeightOffset = hiddenIndex * hiddenSize;
            int resetRecurrentWeightOffset = (hiddenSize + hiddenIndex) * hiddenSize;
            int candidateRecurrentWeightOffset = (2 * hiddenSize + hiddenIndex) * hiddenSize;

            for (int previousHiddenIndex = 0; previousHiddenIndex < hiddenSize; previousHiddenIndex++) {
               recurrentWeightGradients[updateRecurrentWeightOffset + previousHiddenIndex] = recurrentWeightGradients[updateRecurrentWeightOffset + previousHiddenIndex] + updateGateGradients[hiddenIndex] * previousHiddenState[previousHiddenIndex];
               recurrentWeightGradients[resetRecurrentWeightOffset + previousHiddenIndex] = recurrentWeightGradients[resetRecurrentWeightOffset + previousHiddenIndex] + resetGateGradients[hiddenIndex] * previousHiddenState[previousHiddenIndex];
               recurrentWeightGradients[candidateRecurrentWeightOffset + previousHiddenIndex] = recurrentWeightGradients[candidateRecurrentWeightOffset + previousHiddenIndex] + candidateRecurrentBiasGradients[hiddenIndex] * previousHiddenState[previousHiddenIndex];
               previousHiddenStateGradients[previousHiddenIndex] += this.model.recurrentWeights[updateRecurrentWeightOffset + previousHiddenIndex] * updateGateGradients[hiddenIndex]
                  + this.model.recurrentWeights[resetRecurrentWeightOffset + previousHiddenIndex] * resetGateGradients[hiddenIndex]
                  + this.model.recurrentWeights[candidateRecurrentWeightOffset + previousHiddenIndex] * candidateRecurrentBiasGradients[hiddenIndex];
            }
         }
      }

      return new double[]{totalNegativeLogLikelihood, predictionCount};
   }
// https://t.me/pasterplace

   private static double mixtureDensityLossAndGradient(float[] mixtureLogits, float[] target, float[] logitGradients, int mixtureCount) {
      double maximumMixtureLogit = Double.NEGATIVE_INFINITY;

      for (int mixtureIndex = 0; mixtureIndex < mixtureCount; mixtureIndex++) {
         maximumMixtureLogit = Math.max(maximumMixtureLogit, mixtureLogits[mixtureIndex]);
      }

      double exponentiatedMixtureWeights = 0.0;

      for (int mixtureIndex = 0; mixtureIndex < mixtureCount; mixtureIndex++) {
         exponentiatedMixtureWeights += Math.exp(mixtureLogits[mixtureIndex] - maximumMixtureLogit);
      }

      double mixtureLogNormalizer = maximumMixtureLogit + Math.log(exponentiatedMixtureWeights);
      double[] componentLogProbabilities = new double[mixtureCount];
      double maximumComponentLogProbability = Double.NEGATIVE_INFINITY;

      for (int mixtureIndex = 0; mixtureIndex < mixtureCount; mixtureIndex++) {
         double componentLogDensity = 0.0;

         for (int coordinateIndex = 0; coordinateIndex < 2; coordinateIndex++) {
            double logStandardDeviation = mixtureLogits[3 * mixtureCount + 2 * mixtureIndex + coordinateIndex];
            double standardizedResidual = (target[coordinateIndex] - mixtureLogits[mixtureCount + 2 * mixtureIndex + coordinateIndex]) / Math.exp(logStandardDeviation);
            componentLogDensity += -0.5 * standardizedResidual * standardizedResidual - logStandardDeviation - 0.5 * LOG_TWO_PI;
         }

         componentLogProbabilities[mixtureIndex] = mixtureLogits[mixtureIndex] - mixtureLogNormalizer + componentLogDensity;
         maximumComponentLogProbability = Math.max(maximumComponentLogProbability, componentLogProbabilities[mixtureIndex]);
      }

      double exponentiatedComponentWeights = 0.0;

      for (int mixtureIndex = 0; mixtureIndex < mixtureCount; mixtureIndex++) {
         exponentiatedComponentWeights += Math.exp(componentLogProbabilities[mixtureIndex] - maximumComponentLogProbability);
      }

      double componentLogNormalizer = maximumComponentLogProbability + Math.log(exponentiatedComponentWeights);

      for (int mixtureIndex = 0; mixtureIndex < mixtureCount; mixtureIndex++) {
         double posteriorComponentProbability = Math.exp(componentLogProbabilities[mixtureIndex] - componentLogNormalizer);
         double mixtureProbability = Math.exp(mixtureLogits[mixtureIndex] - mixtureLogNormalizer);
         logitGradients[mixtureIndex] = (float)(mixtureProbability - posteriorComponentProbability);

         for (int coordinateIndex = 0; coordinateIndex < 2; coordinateIndex++) {
            int logStandardDeviationOffset = 3 * mixtureCount + 2 * mixtureIndex + coordinateIndex;
            double standardDeviation = Math.exp(mixtureLogits[logStandardDeviationOffset]);
            double standardizedResidual = (target[coordinateIndex] - mixtureLogits[mixtureCount + 2 * mixtureIndex + coordinateIndex]) / standardDeviation;
            logitGradients[mixtureCount + 2 * mixtureIndex + coordinateIndex] = (float)(-posteriorComponentProbability * standardizedResidual / standardDeviation);
            logitGradients[logStandardDeviationOffset] = !(mixtureLogits[logStandardDeviationOffset] <= -7.0F) && !(mixtureLogits[logStandardDeviationOffset] >= 3.0F) ? (float)(posteriorComponentProbability * (1.0 - standardizedResidual * standardizedResidual)) : 0.0F;
         }
      }

      return -componentLogNormalizer;
   }
// https://t.me/pasterplace

   private void applyAdamUpdate(float[][] gradients, float gradientScale, float learningRate) {
      double gradientNormSquared = 0.0;

      for (float[] gradientRow : gradients) {
         for (float gradientValue : gradientRow) {
            double scaledGradientValue = gradientValue * gradientScale;
            gradientNormSquared += scaledGradientValue * scaledGradientValue;
         }
      }

      gradientNormSquared = Math.sqrt(gradientNormSquared);
      float gradientNormScale = gradientNormSquared > 5.0 ? (float)(5.0 / gradientNormSquared) : 1.0F;
      this.optimizerStep++;
      float firstMomentDecay = 0.9F;
      float secondMomentDecay = 0.999F;
      float firstMomentCorrection = (float)(1.0 - Math.pow(firstMomentDecay, this.optimizerStep));
      float secondMomentCorrection = (float)(1.0 - Math.pow(secondMomentDecay, this.optimizerStep));

      for (int parameterArrayIndex = 0; parameterArrayIndex < this.parameters.length; parameterArrayIndex++) {
         float[] parameterArray = this.parameters[parameterArrayIndex];
         float[] gradientArray = gradients[parameterArrayIndex];
         float[] firstMomentArray = this.firstMoments[parameterArrayIndex];
         float[] secondMomentArray = this.secondMoments[parameterArrayIndex];

         for (int parameterIndex = 0; parameterIndex < parameterArray.length; parameterIndex++) {
            float clippedGradient = gradientArray[parameterIndex] * gradientScale * gradientNormScale;
            firstMomentArray[parameterIndex] = firstMomentDecay * firstMomentArray[parameterIndex] + (1.0F - firstMomentDecay) * clippedGradient;
            secondMomentArray[parameterIndex] = secondMomentDecay * secondMomentArray[parameterIndex] + (1.0F - secondMomentDecay) * clippedGradient * clippedGradient;
            parameterArray[parameterIndex] -= learningRate * (firstMomentArray[parameterIndex] / firstMomentCorrection) / ((float)Math.sqrt(secondMomentArray[parameterIndex] / secondMomentCorrection) + 1.0E-8F);
         }
      }
   }
// https://t.me/pasterplace

   private static float[][] createZeroMatrixLike(float[][] shape) {
      float[][] zeroMatrix = new float[shape.length][];

      for (int rowIndex = 0; rowIndex < shape.length; rowIndex++) {
         zeroMatrix[rowIndex] = new float[shape[rowIndex].length];
      }

      return zeroMatrix;
   }
// https://t.me/pasterplace

   private static float[][] copyMatrix(float[][] source) {
      float[][] copy = new float[source.length][];

      for (int rowIndex = 0; rowIndex < source.length; rowIndex++) {
         copy[rowIndex] = (float[])source[rowIndex].clone();
      }

      return copy;
   }
// https://t.me/pasterplace

   private static void accumulateGradients(float[][] accumulator, float[][] increment) {
      for (int rowIndex = 0; rowIndex < accumulator.length; rowIndex++) {
         float[] accumulatorRow = accumulator[rowIndex];
         float[] incrementRow = increment[rowIndex];

         for (int parameterIndex = 0; parameterIndex < accumulatorRow.length; parameterIndex++) {
            accumulatorRow[parameterIndex] += incrementRow[parameterIndex];
         }
      }
   }
// https://t.me/pasterplace

   private record TrainingWindow(float[][] inputs, float[][] targets, int predictionOffset) {
// https://t.me/pasterplace

      private TrainingWindow(float[][] inputs, float[][] targets, int predictionOffset) {
         this.inputs = inputs;
         this.targets = targets;
         this.predictionOffset = predictionOffset;
      }
   }
}

