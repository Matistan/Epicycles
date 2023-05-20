package me.matistan;

public class ComplexNumber {
    float re;
    float im;
    void add(ComplexNumber c) {
        this.re += c.re;
        this.im += c.im;
    }
    ComplexNumber multiply(ComplexNumber c) {
        float rr = re * c.re - im * c.im;
        float mm = re * c.im + im * c.re;
        return new ComplexNumber(rr, mm);
    }
    ComplexNumber(float re, float im) {
        this.re = re;
        this.im = im;
    }
}