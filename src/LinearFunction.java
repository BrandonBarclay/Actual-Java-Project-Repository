public abstract class LinearFunction {

    protected double slope;
    protected double intercept;

    public LinearFunction() {
        ;
    }

    public abstract LinearFunction clone();

    public double interpolateY(double x) {
        return this.slope * x + this.intercept;
    }

    public double calculateIntersectionPointX(LinearFunction that) {
        return (that.intercept - this.intercept) / (this.slope - that.slope);
    }

    public void reCalculateCoefficients(double x1, double x0, double y1, double y0) {
        this.slope = (y1 - y0) / (x1 - x0);
        this.intercept = y1 - this.slope * (x1 - x0);
    }

    public void setCoefficients(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public double getSlope() {
        return this.slope;
    }

    public double getIntercept() {
        return this.intercept;
    }

    public boolean equals(Object source) {
        if ((source == null) || (this.getClass() != source.getClass())) return false;
        if (this == source) return true;
        LinearFunction that = (LinearFunction) source;
        return ((this.slope == that.slope) && (this.intercept == that.intercept));
    }

    public String toString() {
        return "Y = " + this.slope + " * X + " + this.intercept;
    }
}
