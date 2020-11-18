import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class newDistColumn {

    private ChemicalSpecies mostVolatileComponent;
    private ChemicalSpecies leastVolatileComponent;
    private CubicSplineInterpolator interpolator;
    private LinearFunction qLine;
    private LinearFunction rectifyingLine;
    private LinearFunction enrichingLine;
    private double feedFlowRate; // must be less than max
    private double xFeed;
    private double distillateFlowRate;
    private double bottomsFlowRate;
    private double numberOfTrays;
    private double xDistillate;
    private double xBottoms;

    public newDistColumn(double feedTemperature, EquilibriumData data, int ventureNumber, double epsilon) throws IOException {
        ChemicalSpecies[] chemicalSpecies;
        chemicalSpecies = ChemicalSpecies.importDataFromCSV("RawMaterialPhysicalProperties", ventureNumber);
        if (chemicalSpecies[0].getNormalBoilingPoint() < chemicalSpecies[1].getNormalBoilingPoint()) {
            this.mostVolatileComponent = chemicalSpecies[0].clone();
            this.leastVolatileComponent = chemicalSpecies[1].clone();
        } else {
            this.leastVolatileComponent = chemicalSpecies[0].clone();
            this.mostVolatileComponent = chemicalSpecies[1].clone();
        }
        this.interpolator = new CubicSplineInterpolator(data.getXData(ventureNumber), data.getYData(ventureNumber), epsilon);
        this.feedFlowRate = mostVolatileComponent.getMaxFeedRate();
        this.xFeed = mostVolatileComponent.getxFeed();
        this.xDistillate = mostVolatileComponent.getDistillateFractionRequired();
        this.xBottoms = mostVolatileComponent.getBottomsFractionRequired();
        this.distillateFlowRate = (feedFlowRate * (xFeed - xBottoms)) / (xDistillate + xBottoms) ;
        this.bottomsFlowRate = feedFlowRate - distillateFlowRate;
        List<double[]> trayPoints = solveColumn(feedTemperature);
        this.numberOfTrays = trayPoints.size() / 2.0;
        System.out.println(trayPoints.size() / 2);
        this.exportToCSV("DistillationColumnData.csv", trayPoints);
        System.out.println("Number of equilibrium stages: " + Math.ceil(this.numberOfTrays));
    }

    public List<double[]> solveColumn(double feedTemp) {

        double Q = calculateQ(feedTemp);

        QLineFunction qLine = new QLineFunction(Q, xFeed);

        double intX = this.interpolator.QLInt(qLine, xFeed);

        OperatingLine enrichingLine = cELC(intX);

        double newIntX = intX;

        while (!CubicSplineInterpolator.floatEqualLessThan( // sum ting wong
                this.interpolator.ELInt(enrichingLine, this.xDistillate),
                newIntX)) {
            newIntX -= 0.0001;
            enrichingLine = cELC(newIntX);
            System.out.println("newIntX = " + newIntX);
        }

        double idealRFR = (xDistillate / enrichingLine.getIntercept()) - 1;

        double rRFR = 1.5 * idealRFR;

        enrichingLine = new OperatingLine(xDistillate, 0, xDistillate, (xDistillate / (rRFR + 1)));
        System.out.println(enrichingLine);

        double intersectionPoint = qLine.calculateIntersectionPointX(enrichingLine);
        System.out.println("Intersection point: " + intersectionPoint);

        OperatingLine rectifiyngLine = cRLC(intersectionPoint, qLine.interpolateY(intersectionPoint));

        System.out.println("The Enriching Line is : " + enrichingLine.toString());
        System.out.println("The Q line is : " + qLine.toString());
        System.out.println("The Rectifying line is : " + rectifiyngLine.toString());


        List<double[]> numTray = this.calcNumTray(enrichingLine, rectifiyngLine,intersectionPoint);
        System.out.println("New number of trays is : " + numTray);
        return numTray;
    }

    public List<double[]> calcNumTray(OperatingLine enrichingLine, OperatingLine rectifyingLine, double intersectionX) {
        int trayNumber = 0;
        double x = xDistillate;
        double y = xDistillate;
        int feedTray = 0;
        List<double[]> trayPoints = new ArrayList<>();
        System.out.println("Data Point (x = " + x + " y = " + y + ")");
        do {
            ++trayNumber;
            double[] dataPoint;
            // increment tray and move across towards equilibrium curve
            x = this.interpolator.interpolateX(y);
            dataPoint = new double[]{x, y};
            trayPoints.add(dataPoint);
            //System.out.println("Data Point (x = " + x + " y = " + y + ")");

            // Calculates the new Y value on either the enriching or rectifying line depending on which side of feed tray
            if (x > intersectionX) {
                ++feedTray; // Feed tray gets incremented until the new point is on the rectifying line
                y = enrichingLine.interpolateY(x);
            } else {
                y = rectifyingLine.interpolateY(x);
            }
            dataPoint = new double[]{x, y};
            trayPoints.add(dataPoint);
            //System.out.println("Data Point (x = " + x + " y = " + y + ")");

        } while (x > this.xBottoms);
        return trayPoints;
    }

    public OperatingLine cELC(double QeQIntersectionPoint) {
        return new OperatingLine(this.xDistillate, QeQIntersectionPoint,
                                 this.xDistillate, this.interpolator.interpolateY(QeQIntersectionPoint));
    }

    public OperatingLine cRLC(double XintersectionPoint, double YIntersectionPoint) {
        return new OperatingLine(XintersectionPoint, this.xBottoms, YIntersectionPoint, this.xBottoms);
    }

    public double calculateQ(double feedTemp) {
        double q;
        double averageBoilingPoint = (this.mostVolatileComponent.getNormalBoilingPoint()
                + this.leastVolatileComponent.getNormalBoilingPoint())
                / 2;
        double averageHeatCapacity = (this.mostVolatileComponent.getHeatCapacity()
                + this.leastVolatileComponent.getHeatCapacity()) / 2;
        double latentHeat = this.mostVolatileComponent.getLatentHeat();
        q = (latentHeat + (feedTemp - averageBoilingPoint) * averageHeatCapacity) / latentHeat;
        System.out.println("The value of Q is : " + q);
        return q;
    }

    public void exportToCSV(String fileName, List<double[]> dataPoints) throws IOException {

        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(writer);

        String dataPointX;
        String dataPointY;

        for (double[] dataPoint : dataPoints) {
            dataPointX = Double.toString(dataPoint[0]);
            dataPointY = Double.toString(dataPoint[1]);
            bw.append(dataPointX).append(",").append(dataPointY).append("\n");
        }

        bw.close();
        System.out.println("Data was written to the file " + fileName);
    }
}
