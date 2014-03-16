/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.modulator;

import heronarts.lx.LXUtils;
import heronarts.lx.parameter.FixedParameter;
import heronarts.lx.parameter.LXParameter;

import java.lang.IllegalStateException;

/**
 * An extendable modulator class that lets a custom normalized function be supplied
 * by simply extending this class and supplying a compute() and invert() method.
 */
public abstract class FunctionalModulator extends LXRangeModulator {
        
    public FunctionalModulator(double startValue, double endValue, double periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public FunctionalModulator(LXParameter startValue, double endValue, double periodMs) {
        this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public FunctionalModulator(double startValue, LXParameter endValue, double periodMs) {
        this(new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }
    
    public FunctionalModulator(double startValue, double endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }
    
    public FunctionalModulator(LXParameter startValue, LXParameter endValue, double periodMs) {
        this(startValue, endValue, new FixedParameter(periodMs));
    }
    
    public FunctionalModulator(LXParameter startValue, double endValue, LXParameter periodMs) {
        this(startValue, new FixedParameter(endValue), periodMs);
    }

    public FunctionalModulator(double startValue, LXParameter endValue, LXParameter periodMs) {
        this(new FixedParameter(startValue), endValue, periodMs);
    }
    
    public FunctionalModulator(LXParameter startValue, LXParameter endValue, LXParameter periodMs) {
        this("SIN", startValue, endValue, periodMs);
    }
    
    public FunctionalModulator(String label, double startValue, double endValue, double periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), new FixedParameter(periodMs));
    }

    public FunctionalModulator(String label, LXParameter startValue, double endValue, double periodMs) {
        this(label, startValue, new FixedParameter(endValue), new FixedParameter(periodMs));
    }
    
    public FunctionalModulator(String label, double startValue, LXParameter endValue, double periodMs) {
        this(label, new FixedParameter(startValue), endValue, new FixedParameter(periodMs));
    }

    public FunctionalModulator(String label, double startValue, double endValue, LXParameter periodMs) {
        this(label, new FixedParameter(startValue), new FixedParameter(endValue), periodMs);
    }

    public FunctionalModulator(String label, LXParameter startValue, LXParameter endValue, double periodMs) {
        this(label, startValue, endValue, new FixedParameter(periodMs));
    }
    
    public FunctionalModulator(String label, LXParameter startValue, double endValue, LXParameter periodMs) {
        this(label, startValue, new FixedParameter(endValue), periodMs);
    }
    
    public FunctionalModulator(String label, double startValue, LXParameter endValue, LXParameter periodMs) {
        this(label, new FixedParameter(startValue), endValue, periodMs);
    }
    
    public FunctionalModulator(String label, LXParameter startValue, LXParameter endValue, LXParameter periodMs) {
        super(label, startValue, endValue, periodMs);
    }
        
    @Override
    protected double computeNormalizedValue(double deltaMs, double basis) {
        double computed = this.compute(basis);
        if ((computed < 0) || (computed > 1)) {
            throw new IllegalStateException(getClass().getName() + ".compute() must return a value between 0-1, returned " + computed + " for argument " + basis);  
        }
        return computed;
    }
    
    /**
     * Subclasses determine the basis based on a normalized value from 0 to 1.
     * 
     * @param normalizedValue A normalize value from 0 to 1
     */
    protected double computeNormalizedBasis(double basis, double normalizedValue) {
        double inverted = this.invert(basis, normalizedValue);
        if ((inverted < 0) || (inverted > 1)) {
            throw new IllegalStateException(getClass().getName() + ".invert() must return a value between 0-1, returned " + inverted + " for argument " + basis);  
        }
        return inverted;
    }
    
    /**
     * Subclasses override this method to compute the value of the function. Basis is
     * a value from 0-1, the result must be a value from 0-1.
     * 
     * @param basis
     * @return
     */
    public abstract double compute(double basis);
    
    /**
     * Subclasses optionally override this method to support inversion of the value to
     * a basis. This is not well-defined for all functions. If it is not implemented,
     * an UnsupportedOperationException may be thrown at runtime on invocations to
     * methods that would directly change the value or bounds of the function. 
     *  
     * @param basis Previous basis, from 0-1
     * @param value New value from 0-1
     * @return New basis, from 0-1
     */
    public double invert(double basis, double value) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not implement invert(), may not directly change range or value.");
    }
}
