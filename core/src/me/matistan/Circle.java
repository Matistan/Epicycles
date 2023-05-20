package me.matistan;

public class Circle {
    float re;
    float im;
    int freq;
    float radius;
    float phase;
    Circle(ComplexNumber complexNumber, int freq, float radius, float phase) {
        this.re = complexNumber.re;
        this.im = complexNumber.im;
        this.freq = freq;
        this.radius = radius;
        this.phase = phase;
    }
}
