package dev.daamky;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public final class RecurrentMixtureDensityModel {
    private static final String p7b4481b4 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final int MODEL_MAGIC = 1313296981;
   public final int inputSize;
   public final int hiddenSize;
   public final int mixtureCount;
   public final int outputSize;
   final float[] inputWeights;
   final float[] recurrentWeights;
   final float[] gateBiases;
   final float[] candidateRecurrentBias;
   final float[] outputWeights;
   final float[] outputBiases;
   public final float[] inputMean;
   public final float[] inputStandardDeviation;
// https://t.me/pasterplace

   public RecurrentMixtureDensityModel(int inputSize, int hiddenSize, int mixtureCount) {
      this.inputSize = inputSize;
      this.hiddenSize = hiddenSize;
      this.mixtureCount = mixtureCount;
      this.outputSize = mixtureCount * 5;
      this.inputWeights = new float[3 * hiddenSize * inputSize];
      this.recurrentWeights = new float[3 * hiddenSize * hiddenSize];
      this.gateBiases = new float[3 * hiddenSize];
      this.candidateRecurrentBias = new float[hiddenSize];
      this.outputWeights = new float[this.outputSize * hiddenSize];
      this.outputBiases = new float[this.outputSize];
      this.inputMean = new float[inputSize];
      this.inputStandardDeviation = new float[inputSize];
      Arrays.fill(this.inputStandardDeviation, 1.0F);
   }
// https://t.me/pasterplace

   public void initializeWeights(long seed) {
      Random random = new Random(seed);
      float inputWeightScale = (float)Math.sqrt(1.0 / this.inputSize);
      float hiddenWeightScale = (float)Math.sqrt(1.0 / this.hiddenSize);

      for (int inputWeightIndex = 0; inputWeightIndex < this.inputWeights.length; inputWeightIndex++) {
         this.inputWeights[inputWeightIndex] = (float)random.nextGaussian() * inputWeightScale;
      }

      for (int recurrentWeightIndex = 0; recurrentWeightIndex < this.recurrentWeights.length; recurrentWeightIndex++) {
         this.recurrentWeights[recurrentWeightIndex] = (float)random.nextGaussian() * hiddenWeightScale;
      }

      for (int outputWeightIndex = 0; outputWeightIndex < this.outputWeights.length; outputWeightIndex++) {
         this.outputWeights[outputWeightIndex] = (float)random.nextGaussian() * hiddenWeightScale * 0.5F;
      }

      for (int mixtureIndex = 0; mixtureIndex < this.mixtureCount; mixtureIndex++) {
         this.outputBiases[this.mixtureCount + 2 * mixtureIndex] = (float)random.nextGaussian() * 0.3F;
         this.outputBiases[this.mixtureCount + 2 * mixtureIndex + 1] = (float)random.nextGaussian() * 0.3F;
         this.outputBiases[3 * this.mixtureCount + 2 * mixtureIndex] = -1.0F;
         this.outputBiases[3 * this.mixtureCount + 2 * mixtureIndex + 1] = -1.0F;
      }
   }
// https://t.me/pasterplace

   float[][] getParameterArrays() {
      return new float[][]{this.inputWeights, this.recurrentWeights, this.gateBiases, this.candidateRecurrentBias, this.outputWeights, this.outputBiases};
   }
// https://t.me/pasterplace

   static float sigmoid(float value) {
      return 1.0F / (1.0F + (float)Math.exp(-value));
   }
// https://t.me/pasterplace

   public void stepRecurrentCell(float[] input, float[] previousHiddenState, float[] nextHiddenState, float[] updateGateOutput, float[] resetGateOutput, float[] candidateOutput, float[] candidateRecurrentOutput) {
      int hiddenUnitCount = this.hiddenSize;

      for (int hiddenIndex = 0; hiddenIndex < hiddenUnitCount; hiddenIndex++) {
         float updateGatePreactivation = this.gateBiases[hiddenIndex];
         float resetGatePreactivation = this.gateBiases[hiddenUnitCount + hiddenIndex];
         float candidatePreactivation = this.gateBiases[2 * hiddenUnitCount + hiddenIndex];
         int updateInputWeightOffset = hiddenIndex * this.inputSize;
         int resetInputWeightOffset = (hiddenUnitCount + hiddenIndex) * this.inputSize;
         int candidateInputWeightOffset = (2 * hiddenUnitCount + hiddenIndex) * this.inputSize;

         for (int inputIndex = 0; inputIndex < this.inputSize; inputIndex++) {
            float inputValue = input[inputIndex];
            updateGatePreactivation += this.inputWeights[updateInputWeightOffset + inputIndex] * inputValue;
            resetGatePreactivation += this.inputWeights[resetInputWeightOffset + inputIndex] * inputValue;
            candidatePreactivation += this.inputWeights[candidateInputWeightOffset + inputIndex] * inputValue;
         }

         float updateRecurrentSum = 0.0F;
         float resetRecurrentSum = 0.0F;
         float candidateRecurrentSum = this.candidateRecurrentBias[hiddenIndex];
         int updateRecurrentWeightOffset = hiddenIndex * hiddenUnitCount;
         int resetRecurrentWeightOffset = (hiddenUnitCount + hiddenIndex) * hiddenUnitCount;
         int candidateRecurrentWeightOffset = (2 * hiddenUnitCount + hiddenIndex) * hiddenUnitCount;

         for (int previousHiddenIndex = 0; previousHiddenIndex < hiddenUnitCount; previousHiddenIndex++) {
            float previousHiddenValue = previousHiddenState[previousHiddenIndex];
            updateRecurrentSum += this.recurrentWeights[updateRecurrentWeightOffset + previousHiddenIndex] * previousHiddenValue;
            resetRecurrentSum += this.recurrentWeights[resetRecurrentWeightOffset + previousHiddenIndex] * previousHiddenValue;
            candidateRecurrentSum += this.recurrentWeights[candidateRecurrentWeightOffset + previousHiddenIndex] * previousHiddenValue;
         }

         float updateGate = sigmoid(updateGatePreactivation + updateRecurrentSum);
         float resetGate = sigmoid(resetGatePreactivation + resetRecurrentSum);
         float candidateActivation = (float)Math.tanh(candidatePreactivation + resetGate * candidateRecurrentSum);
         updateGateOutput[hiddenIndex] = updateGate;
         resetGateOutput[hiddenIndex] = resetGate;
         candidateOutput[hiddenIndex] = candidateActivation;
         candidateRecurrentOutput[hiddenIndex] = candidateRecurrentSum;
         nextHiddenState[hiddenIndex] = (1.0F - updateGate) * candidateActivation + updateGate * previousHiddenState[hiddenIndex];
      }
   }
// https://t.me/pasterplace

   public void projectOutput(float[] hiddenState, float[] outputLogits) {
      for (int outputIndex = 0; outputIndex < this.outputSize; outputIndex++) {
         float outputActivation = this.outputBiases[outputIndex];
         int outputWeightOffset = outputIndex * this.hiddenSize;

         for (int hiddenIndex = 0; hiddenIndex < this.hiddenSize; hiddenIndex++) {
            outputActivation += this.outputWeights[outputWeightOffset + hiddenIndex] * hiddenState[hiddenIndex];
         }

         outputLogits[outputIndex] = outputActivation;
      }

      for (int clampedLogScaleIndex = 0; clampedLogScaleIndex < 2 * this.mixtureCount; clampedLogScaleIndex++) {
         int logScaleOutputOffset = 3 * this.mixtureCount + clampedLogScaleIndex;
         outputLogits[logScaleOutputOffset] = Math.max(-7.0F, Math.min(3.0F, outputLogits[logScaleOutputOffset]));
      }
   }
// https://t.me/pasterplace

   public void normalizeInput(float[] inputFeatures, float[] normalizedInput) {
      for (int featureIndex = 0; featureIndex < this.inputSize; featureIndex++) {
         normalizedInput[featureIndex] = (inputFeatures[featureIndex] - this.inputMean[featureIndex]) / this.inputStandardDeviation[featureIndex];
      }
   }
// https://t.me/pasterplace

   public void sampleOutput(float[] outputLogits, Random random, float[] sampledOutput) {
      this.sampleOutput(outputLogits, random, sampledOutput, 1.0F);
   }
// https://t.me/pasterplace

   public void sampleOutput(float[] outputLogits, Random random, float[] sampledOutput, float temperature) {
      float maximumMixtureLogit = Float.NEGATIVE_INFINITY;

      for (int mixtureIndex = 0; mixtureIndex < this.mixtureCount; mixtureIndex++) {
         maximumMixtureLogit = Math.max(maximumMixtureLogit, outputLogits[mixtureIndex]);
      }

      float unnormalizedWeightSum = 0.0F;
      float[] mixtureWeights = new float[this.mixtureCount];
// https://t.me/pasterplace

      for (int mixtureIndex = 0; mixtureIndex < this.mixtureCount; mixtureIndex++) {
         mixtureWeights[mixtureIndex] = (float)Math.exp(outputLogits[mixtureIndex] - maximumMixtureLogit);
         unnormalizedWeightSum += mixtureWeights[mixtureIndex];
      }

      float randomWeightThreshold = random.nextFloat() * unnormalizedWeightSum;
      int selectedMixture = this.mixtureCount - 1;
// https://t.me/pasterplace

      for (int mixtureIndex = 0; mixtureIndex < this.mixtureCount; mixtureIndex++) {
         randomWeightThreshold -= mixtureWeights[mixtureIndex];
         if (randomWeightThreshold <= 0.0F) {
            selectedMixture = mixtureIndex;
            break;
         }
      }
// https://t.me/pasterplace

      for (int coordinateIndex = 0; coordinateIndex < 2; coordinateIndex++) {
         float coordinateMean = outputLogits[this.mixtureCount + 2 * selectedMixture + coordinateIndex];
         float coordinateStandardDeviation = (float)Math.exp(outputLogits[3 * this.mixtureCount + 2 * selectedMixture + coordinateIndex]) * temperature;
         sampledOutput[coordinateIndex] = coordinateMean + coordinateStandardDeviation * (float)random.nextGaussian();
      }
   }
// https://t.me/pasterplace

   public void save(DataOutputStream output) throws IOException {
      output.writeInt(1313296981);
      output.writeInt(this.inputSize);
      output.writeInt(this.hiddenSize);
      output.writeInt(this.mixtureCount);

      for (float[] parameterArray : this.getParameterArrays()) {
         for (float parameterValue : parameterArray) {
            output.writeFloat(parameterValue);
         }
      }

      for (float featureIndex : this.inputMean) {
         output.writeFloat(featureIndex);
      }

      for (float featureIndex : this.inputStandardDeviation) {
         output.writeFloat(featureIndex);
      }
   }
// https://t.me/pasterplace

   public static RecurrentMixtureDensityModel load(DataInputStream input) throws IOException {
      if (input.readInt() != 1313296981) {
         throw new IOException("bad model");
      }

      RecurrentMixtureDensityModel model = new RecurrentMixtureDensityModel(input.readInt(), input.readInt(), input.readInt());

      for (float[] parameterArray : model.getParameterArrays()) {
         for (int parameterIndex = 0; parameterIndex < parameterArray.length; parameterIndex++) {
            parameterArray[parameterIndex] = input.readFloat();
         }
      }

      for (int featureIndex = 0; featureIndex < model.inputSize; featureIndex++) {
         model.inputMean[featureIndex] = input.readFloat();
      }

      for (int featureIndex = 0; featureIndex < model.inputSize; featureIndex++) {
         model.inputStandardDeviation[featureIndex] = input.readFloat();
      }

      return model;
   }
}

