public class OperatingLine extends LinearFunction {

    public OperatingLine(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    // x1 > x0 is assumed here
    public OperatingLine(double x1, double x0, double y1, double y0) {
        this.slope = (y1 - y0) / (x1 - x0);
        this.intercept = y1 - this.slope * (x1);
    }

    public OperatingLine(LinearFunction source) {
        this.intercept = source.intercept;
        this.slope = source.slope;
    }

    @Override
    public LinearFunction clone() {
        return null;
    }
}
