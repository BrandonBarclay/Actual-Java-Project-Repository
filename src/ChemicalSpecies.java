import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class ChemicalSpecies {

    private String name;
    private double heatCapacity;                // kJ / kmol k
    private double normalBoilingPoint;          // k
    private double latentHeat;                  // kJ / kmol
    private double maxFeedRate;                 // kmol / h
    private double distillateFractionRequired;  // mol%
    private double bottomsFractionRequired;     // mol%
    private double rawCost;                     // $ / kmol
    private double distillateSalePrice;         // $ / kmol
    private double bottomsSalePrice;            // $ / kmol
    private double xFeed;

    public ChemicalSpecies(String name, double[] parameters) throws  IllegalArgumentException {
        if ((parameters == null) || (parameters.length != 10)) throw new IllegalArgumentException("Parameters not length 10");
        this.name = name;
        this.heatCapacity = parameters[0];
        this.normalBoilingPoint = parameters[1];
        this.latentHeat = parameters[2];
        this.maxFeedRate = parameters[3];
        this.distillateFractionRequired = parameters[4];
        this.bottomsFractionRequired = parameters[5];
        this.rawCost = parameters[6];
        this.distillateSalePrice = parameters[7];
        this.bottomsSalePrice = parameters[8];
        this.xFeed = parameters[9];
    }

    public ChemicalSpecies(ChemicalSpecies that) {
        this.name = that.name;
        this.heatCapacity = that.heatCapacity;
        this.normalBoilingPoint = that.normalBoilingPoint;
        this.latentHeat = that.latentHeat;
        this.maxFeedRate = that.maxFeedRate;
        this.distillateFractionRequired = that.distillateFractionRequired;
        this.bottomsFractionRequired = that.bottomsFractionRequired;
        this.rawCost = that.rawCost;
        this.distillateSalePrice = that.distillateSalePrice;
        this.bottomsSalePrice = that.bottomsSalePrice;
        this.xFeed = that.xFeed;

    }

    public static ChemicalSpecies[] importDataFromCSV(String fileName, int ventureNumber) throws IOException {
                BufferedReader      reader = new BufferedReader(new FileReader(fileName));
                ChemicalSpecies[]   CSVData = new ChemicalSpecies[2];
                String[]            splitLine;
                String              line;

                for (int i = 1; i < ventureNumber; ++i) { // skip to the correct place in the file
                    reader.readLine();
                    reader.readLine();
                }

                line = reader.readLine();
                for (int i = 0; i < 2; ++i) {
                    if (line == null) throw new IOException("Tried to read past the end of file");

                    splitLine = line.split(",");

                    double[] constructorParameters = new double[splitLine.length - 1];

                    String name = splitLine[0];

                    for (int j = 0; j < constructorParameters.length; ++j) {
                        constructorParameters[j] = Double.parseDouble(splitLine[j + 1]);
                    }

                    CSVData[i] = new ChemicalSpecies(name, constructorParameters);
                    line = reader.readLine();
                }
        return CSVData;
    }

    public ChemicalSpecies clone() {
        return new ChemicalSpecies(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChemicalSpecies that = (ChemicalSpecies) o;
        return  Double.compare(that.heatCapacity, heatCapacity) == 0 &&
                Double.compare(that.normalBoilingPoint, normalBoilingPoint) == 0 &&
                Double.compare(that.latentHeat, latentHeat) == 0 &&
                Double.compare(that.maxFeedRate, maxFeedRate) == 0 &&
                Double.compare(that.distillateFractionRequired, distillateFractionRequired) == 0 &&
                Double.compare(that.bottomsFractionRequired, bottomsFractionRequired) == 0 &&
                Double.compare(that.rawCost, rawCost) == 0 &&
                Double.compare(that.distillateSalePrice, distillateSalePrice) == 0 &&
                Double.compare(that.bottomsSalePrice, bottomsSalePrice) == 0 &&
                Double.compare(that.xFeed, xFeed) == 0 &&
                Objects.equals(name, that.name);
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getHeatCapacity() {
        return this.heatCapacity;
    }
    public void setHeatCapacity(double heatCapacity) {
        this.heatCapacity = heatCapacity;
    }
    public double getNormalBoilingPoint() {
        return this.normalBoilingPoint;
    }
    public void setNormalBoilingPoint(double normalBoilingPoint) {
        this.normalBoilingPoint = normalBoilingPoint;
    }
    public double getLatentHeat() {
        return this.latentHeat;
    }
    public void setLatentHeat(double latentHeat) {
        this.latentHeat = latentHeat;
    }
    public double getMaxFeedRate() {
        return this.maxFeedRate;
    }
    public void setMaxFeedRate(double maxFeedRate) {
        this.maxFeedRate = maxFeedRate;
    }
    public double getDistillateFractionRequired() {
        return this.distillateFractionRequired;
    }
    public void setDistillateFractionRequired(double distillateFractionRequired) {
        this.distillateFractionRequired = distillateFractionRequired;
    }
    public double getBottomsFractionRequired() {
        return this.bottomsFractionRequired;
    }
    public void setBottomsFractionRequired(double bottomsFractionRequired) {
        this.bottomsFractionRequired = bottomsFractionRequired;
    }
    public double getRawCost() {
        return this.rawCost;
    }
    public void setRawCost(double rawCost) {
        this.rawCost = rawCost;
    }
    public double getDistillateSalePrice() {
        return this.distillateSalePrice;
    }
    public void setDistillateSalePrice(double distillateSalePrice) {
        this.distillateSalePrice = distillateSalePrice;
    }
    public double getBottomsSalePrice() {
        return this.bottomsSalePrice;
    }
    public void setBottomsSalePrice(double bottomsSalePrice) {
        this.bottomsSalePrice = bottomsSalePrice;
    }
    public double getxFeed() {
        return this.xFeed;
    }
    public void setxFeed(double xFeed) {
        this.xFeed = xFeed;
    }
}
