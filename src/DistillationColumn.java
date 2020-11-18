import java.io.IOException;

public class DistillationColumn {

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
    private int numberOfTrays;
    private double xDistillate;
    private double xBottoms;


    public DistillationColumn(double feedTemperature, EquilibriumData data, int ventureNumber, double epsilon) throws IOException {
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
        this.numberOfTrays = solveColumn(feedTemperature);
        this.distillateFlowRate = (feedFlowRate * (xFeed - xBottoms)) / (xDistillate + xBottoms) ;
        this.bottomsFlowRate = feedFlowRate - distillateFlowRate;
        System.out.println("Number of equilibrium stages: " + Math.ceil(this.numberOfTrays));
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

    public int solveColumn(double feedTemp) {
        double increment = 0.00001;

        // Calculate Q, Find the coefficients of Q-Line, and find intersection of Q-Line with Equilibrium Curve
        double Q = calculateQ(feedTemp);

        double[] qLineCoefficients;
        double intersectionPointX;

        if (Q == 1) {
            qLineCoefficients = calculateQLineCoefficients(0.9999999);
            intersectionPointX = this.interpolator.interpolateY(this.xFeed);
        } else if (Q == 0) {
            qLineCoefficients = calculateQLineCoefficients(0.0);
            intersectionPointX = this.interpolator.interpolateX(this.xFeed);
        } else {
            qLineCoefficients = calculateQLineCoefficients(Q);
            intersectionPointX = interpolator.qLineIntersection(qLineCoefficients, this.xFeed);
        }

        // Find the slope of the Enriching line that is tangent to the equilibrium curve for
        // non-deal mixtures, ideal mixtures will return right away
        double[] enrichingLineCoefficients = calculateEnrichingLineCoefficients(intersectionPointX);
        double   newIntersectionX = intersectionPointX;

        while (!CubicSplineInterpolator.floatEqualLessThan(
                this.interpolator.enrichingLineIntersection(enrichingLineCoefficients, this.xDistillate),
                intersectionPointX)) {
            newIntersectionX -= 0.01;
            enrichingLineCoefficients = calculateEnrichingLineCoefficients(newIntersectionX);
        }

        // Calculate ideal reflux ratio from the enriching line intercept, use heuristic to find real reflux ratio
        double idealRefluxRatio = (xDistillate / enrichingLineCoefficients[1]) - 1;
        System.out.println("Ideal Reflux Ratio = " + idealRefluxRatio);

        double realRefluxRatio = 1.5 * idealRefluxRatio;
        System.out.println("Real Reflux Ratio = " + realRefluxRatio);

        // Calculate new enriching line based on real reflux ratio
        enrichingLineCoefficients[1] = xDistillate / (realRefluxRatio + 1);
        enrichingLineCoefficients[0] = (xDistillate - enrichingLineCoefficients[1]) / (xDistillate);

        double sum;
        double qLineEnrichingLineIntersectionPoint = xFeed + increment * qLineCoefficients[0] / 100;
        double yQLine;
        double yEnrichingLine;
        do {
            qLineEnrichingLineIntersectionPoint += increment * qLineCoefficients[0] / 100;
            yEnrichingLine = enrichingLineCoefficients[0] * qLineEnrichingLineIntersectionPoint + enrichingLineCoefficients[1];
            yQLine =         qLineCoefficients[0] * qLineEnrichingLineIntersectionPoint         + qLineCoefficients[1];
            sum = yEnrichingLine - yQLine;
        } while (!CubicSplineInterpolator.floatEqual(sum, 0));
        System.out.println("YQLine = " + yQLine + " YEnrichingLine = " + yEnrichingLine);
        System.out.println("The enriching line intersects the q line at X = " + qLineEnrichingLineIntersectionPoint);

        // Calculate the rectifying line based on the intersection point between the Q-Line and Enriching Line
        double[] rectifyingLineCoefficients = calculateRectifyingLineCoefficients(enrichingLineCoefficients, qLineEnrichingLineIntersectionPoint);

        // Output proposed solution to the console
        System.out.println("The enriching line is : Y = " + enrichingLineCoefficients[0] + " * X + " + enrichingLineCoefficients[1]);
        System.out.println("The Q line is : Y = " + qLineCoefficients[0] + " * X + " + qLineCoefficients[1]);
        System.out.println("The rectifying line is : Y = " + rectifyingLineCoefficients[0] + " * X + " + rectifyingLineCoefficients[1]);

        return calculateNumTrays(enrichingLineCoefficients, rectifyingLineCoefficients, qLineEnrichingLineIntersectionPoint);
    }

    public int calculateNumTrays(double[] enrichingLineCoefficients, double[] rectifyingLineCoefficient, double intersectionX) {
        int trayNumber = 0;
        double x = xDistillate;
        double y = xDistillate;
        int feedTray = 0;
        System.out.println("Data Point (x = " + x + " y = " + y + ")");
        do {
            ++trayNumber;

            // increment tray and move across towards equilibrium curve
            x = interpolator.interpolateX(y);
            System.out.println("Data Point (x = " + x + " y = " + y + ")");

            // Calculates the new Y value on either the enriching or rectifying line depending on which side of feed tray
            if (x > intersectionX) {
                ++feedTray; // Feed tray gets incremented until the new point is on the rectifying line
                y = enrichingLineCoefficients[0] * x + enrichingLineCoefficients[1];
            } else {
                y = rectifyingLineCoefficient[0] * x + rectifyingLineCoefficient[1];
            }
            System.out.println("Data Point (x = " + x + " y = " + y + ")");
        } while (x > xBottoms);
        return trayNumber;
    }

    public double[] calculateEnrichingLineCoefficients(double intersectionPointX) {
        double slope = (xDistillate - interpolator.interpolateY(intersectionPointX)) / (xDistillate - intersectionPointX);
        double intercept = xDistillate - xDistillate * slope;
        return new double[]{slope, intercept};
    }

    public double[] calculateQLineCoefficients(double q) {
        double intercept = -xFeed / (q - 1);
        double slope = q / (q - 1);
        return new double[]{slope, intercept};
    }

    public double[] calculateRectifyingLineCoefficients(double[] LineCoefficients, double intersectionX) {
        double slope  = ((LineCoefficients[0] * intersectionX + LineCoefficients[1]) - xBottoms) / (intersectionX - xBottoms);
        double intercept = xBottoms - slope * xBottoms;
        return new double[]{slope, intercept};
    }
}
