/*
 * Copyright (c) 2022 - 2023 trinity-tech.io
 * Copyright (c) 2023 -      bosonnetwork.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.bosonnetwork.kademlia;

/**
 * @hidden
 */
public class ExponentialWeightendMovingAverage {
	private double weight = 0.3;
	private double average = Double.NaN;

	public ExponentialWeightendMovingAverage(double weight, double average) {
		this.weight = weight;
		this.average = average;
	}

	public ExponentialWeightendMovingAverage(double weight) {
		this.weight = weight;
	}

	public ExponentialWeightendMovingAverage() {
	}

	public ExponentialWeightendMovingAverage setWeight(double weight) {
		this.weight = weight;
		return this;
	}

	public ExponentialWeightendMovingAverage setValue(double average) {
		this.average = average;
		return this;
	}

	public void updateAverage(double value) {
		if(Double.isNaN(average))
			average = value;
		else
			average = value * weight + average * (1.0 - weight);
	}

	public double getAverage() {
		return average;
	}

	public double getAverage(double defaultValue)
	{
		return Double.isNaN(average) ? defaultValue : average;
	}
}
