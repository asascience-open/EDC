/*
 * Applied Science Associates, Inc.
 * Copyright 2009. All Rights Reserved.
 *
 * Complex.java
 *
 * Created on Jan 28, 2009 @ 5:08:25 PM
 */
package com.asascience.utilities;

/**
 * 
 * @author CBM <cmueller@asascience.com>
 */
public class Complex {

  private final double re; // the real part
  private final double im; // the imaginary part

  /**
   * Constructs the complex number z = u + i*v
   *
   * @param real
   *            Real part
   * @param imaginary
   *            Imaginary part
   */
  public Complex(double real, double imaginary) {
    this.re = real;
    im = imaginary;
  }

  /**
   * Real part of this Complex number (the x-coordinate in rectangular
   * coordinates).
   *
   * @return Re[z] where z is this Complex number.
   */
  public double real() {
    return re;
  }

  /**
   * Imaginary part of this Complex number (the y-coordinate in rectangular
   * coordinates).
   *
   * @return Im[z] where z is this Complex number.
   */
  public double imag() {
    return im;
  }

  /**
   * Modulus of this Complex number (the distance from the origin in polar
   * coordinates).
   *
   * @return |z| where z is this Complex number.
   */
  public double mod() {
    if (re != 0 || im != 0) {
      return Math.sqrt(re * re + im * im);
    } else {
      return 0d;
    }
  }

  /**
   * Argument of this Complex number (the angle in radians with the x-axis in
   * polar coordinates).
   *
   * @return arg(z) where z is this Complex number.
   */
  public double arg() {
    return Math.atan2(im, re);
  }

  /**
   * Complex conjugate of this Complex number (the conjugate of x+i*y is
   * x-i*y).
   *
   * @return z-bar where z is this Complex number.
   */
  public Complex conj() {
    return new Complex(re, -im);
  }

  /**
   * Addition of Complex numbers (doesn't change this Complex number). <br>
   * (x+i*y) + (s+i*t) = (x+s)+i*(y+t).
   *
   * @param w
   *            is the number to add.
   * @return z+w where z is this Complex number.
   */
  public Complex plus(Complex w) {
    return new Complex(re + w.real(), im + w.imag());
  }

  /**
   * Subtraction of Complex numbers (doesn't change this Complex number). <br>
   * (x+i*y) - (s+i*t) = (x-s)+i*(y-t).
   *
   * @param w
   *            is the number to subtract.
   * @return z-w where z is this Complex number.
   */
  public Complex minus(Complex w) {
    return new Complex(re - w.real(), im - w.imag());
  }

  /**
   * Complex multiplication (doesn't change this Complex number).
   *
   * @param w
   *            is the number to multiply by.
   * @return z*w where z is this Complex number.
   */
  public Complex times(Complex w) {
    return new Complex(re * w.real() - im * w.imag(), re * w.imag() + im * w.real());
  }

  /**
   * Division of Complex numbers (doesn't change this Complex number). <br>
   * (x+i*y)/(s+i*t) = ((x*s+y*t) + i*(y*s-y*t)) / (s^2+t^2)
   *
   * @param w
   *            is the number to divide by
   * @return new Complex number z/w where z is this Complex number
   */
  public Complex div(Complex w) {
    double den = Math.pow(w.mod(), 2);
    return new Complex((re * w.real() + im * w.imag()) / den, (im * w.real() - re * w.imag()) / den);
  }

  /**
   * Complex exponential (doesn't change this Complex number).
   *
   * @return exp(z) where z is this Complex number.
   */
  public Complex exp() {
    return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
  }

  /**
   * Principal branch of the Complex logarithm of this Complex number.
   * (doesn't change this Complex number). The principal branch is the branch
   * with -pi < arg <= pi.
   *
   * @return log(z) where z is this Complex number.
   */
  public Complex log() {
    return new Complex(Math.log(this.mod()), this.arg());
  }

  /**
   * Complex square root (doesn't change this complex number). Computes the
   * principal branch of the square root, which is the value with 0 <= arg <
   * pi.
   *
   * @return sqrt(z) where z is this Complex number.
   */
  public Complex sqrt() {
    double r = Math.sqrt(this.mod());
    double theta = this.arg() / 2;
    return new Complex(r * Math.cos(theta), r * Math.sin(theta));
  }

  // Real cosh function (used to compute complex trig functions)
  private double cosh(double theta) {
    return (Math.exp(theta) + Math.exp(-theta)) / 2;
  }

  // Real sinh function (used to compute complex trig functions)
  private double sinh(double theta) {
    return (Math.exp(theta) - Math.exp(-theta)) / 2;
  }

  /**
   * Sine of this Complex number (doesn't change this Complex number). <br>
   * sin(z) = (exp(i*z)-exp(-i*z))/(2*i).
   *
   * @return sin(z) where z is this Complex number.
   */
  public Complex sin() {
    return new Complex(cosh(im) * Math.sin(re), sinh(im) * Math.cos(re));
  }

  /**
   * Cosine of this Complex number (doesn't change this Complex number). <br>
   * cos(z) = (exp(i*z)+exp(-i*z))/ 2.
   *
   * @return cos(z) where z is this Complex number.
   */
  public Complex cos() {
    return new Complex(cosh(im) * Math.cos(re), -sinh(im) * Math.sin(re));
  }

  /**
   * Hyperbolic sine of this Complex number (doesn't change this Complex
   * number). <br>
   * sinh(z) = (exp(z)-exp(-z))/2.
   *
   * @return sinh(z) where z is this Complex number.
   */
  public Complex sinh() {
    return new Complex(sinh(re) * Math.cos(im), cosh(re) * Math.sin(im));
  }

  /**
   * Hyperbolic cosine of this Complex number (doesn't change this Complex
   * number). <br>
   * cosh(z) = (exp(z) + exp(-z)) / 2.
   *
   * @return cosh(z) where z is this Complex number.
   */
  public Complex cosh() {
    return new Complex(cosh(re) * Math.cos(im), sinh(re) * Math.sin(im));
  }

  /**
   * Tangent of this Complex number (doesn't change this Complex number). <br>
   * tan(z) = sin(z)/cos(z).
   *
   * @return tan(z) where z is this Complex number.
   */
  public Complex tan() {
    return (this.sin()).div(this.cos());
  }

  /**
   * Negative of this complex number (chs stands for change sign). This
   * produces a new Complex number and doesn't change this Complex number. <br>
   * -(x+i*y) = -x-i*y.
   *
   * @return -z where z is this Complex number.
   */
  public Complex chs() {
    return new Complex(-re, -im);
  }

  /**
   * String representation of this Complex number.
   *
   * @return x+i*y, x-i*y, x, or i*y as appropriate.
   */
  public String toString() {
    if (re != 0 && im > 0) {
      return re + " + " + im + "i";
    }
    if (re != 0 && im < 0) {
      return re + " - " + (-im) + "i";
    }
    if (im == 0) {
      return String.valueOf(re);
    }
    if (re == 0) {
      return im + "i";
    }
    // shouldn't get here (unless Inf or NaN)
    return re + " + i*" + im;

  }
}
