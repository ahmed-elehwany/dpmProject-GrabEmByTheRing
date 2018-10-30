package ca.mcgill.ecse211.dpmProject;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class contains methods needed to perform all necessary tests and calibrations 
 * including color calibration and the driver
 * @author Team12
 *
 */
public class Tester {
	public static final TextLCD lcd = Project.lcd;
	public static final Port portColor = Project.portColor; // get the port for the light (color sensor)
	public static final SensorModes myColor = Project.myColor; // create the color sensor object;
	public static final SampleProvider myColorSample = Project.myColorSample;
	public static final float[] sampleColor = Project.sampleColor; // create an array for the sensor reading
	public static final EV3LargeRegulatedMotor leftMotor = Project.leftMotor;
	public static final EV3LargeRegulatedMotor rightMotor = Project.rightMotor;
	
	/**
	 * this method is used for rgb reading collection for mean and std calculations 
	 */
	public static void sample() {
		lcd.clear();
		int counter =0;
		//run 100 samples
		while(counter<100) {
			myColorSample.fetchSample(sampleColor, 0); 
			float r = sampleColor[0]*1000; 
			float g = sampleColor[1]*1000; 
			float b = sampleColor[2]*1000; 
			System.out.print(r +",");
			System.out.print(g+",");
			System.out.println(b);
			counter ++;
		}		
	}
	
	/**
	 * This method calibrates the wheel radius value of the robot
	 * since the method convertDistance only uses wheel radius
	 * @param leftMotor left motor of the robot
	 * @param rightMotor right motor of the robot 
	 */
	public static void wheelRadCheck() {
		// reset the motor
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(2000);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// There is nothing to be done here
		}
		//move the robot forward until the Y asis is detected
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		leftMotor.rotate(Navigation.convertDistance(Project.WHEEL_RAD, 2*Project.TILE_SIZE), true);
		rightMotor.rotate(Navigation.convertDistance(Project.WHEEL_RAD, 2*Project.TILE_SIZE), false);
	}
	
	/**
	 * This method calibrates the wheelbase value of the robot once the 
	 * wheel radius is calibrated
	 * @param leftMotor left motor of the robot
	 * @param rightMotor right motor of the robot 
	 */
	public static void trackCheck() {
		// reset the motor
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(2000);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// There is nothing to be done here
		}
		//move the robot forward until the Y asis is detected
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.rotate(Navigation.convertAngle(Project.WHEEL_RAD, Project.TRACK, 360), true);
		rightMotor.rotate(-Navigation.convertAngle(Project.WHEEL_RAD, Project.TRACK, 360), false);
	}
	
	/**
	 * This method is used for ultrasonic (right side) reading collection (to understand the sensor performance)
	 * @param odometer the odomter object used
	 */
	public static void usSample(Odometer odometer) {
		System.out.println("start US sampling");
		int ringCount = 0;
		SampleProvider usDistanceR = Project.usDistanceR;
		float[] usDataR = Project.usDataR;

		boolean foundRing = false;
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		while (foundRing == false) {
			leftMotor.forward();
			rightMotor.forward();
			usDistanceR.fetchSample(usDataR, 0);
			int distance = (int) (usDataR[0] * 100.0);
			System.out.println(distance);
			if (distance < Project.DETECT_DISTANCE) {
				ringCount++;
			} else {
				ringCount = 0;
			}
			// turn and approach if detected
			if (ringCount > 10) {
				System.out.println("..........................detect Ring");
				Sound.beep();
				Sound.beep();
				foundRing = true;
				double currentY = odometer.getXYT()[1];
				double correction = Project.TILE_SIZE - (currentY % Project.TILE_SIZE);
				System.out.println("current Y is " + currentY);
				System.out.println("correction is " + correction);

				leftMotor.rotate(Navigation.convertDistance(Project.WHEEL_RAD, correction), true);
				rightMotor.rotate(Navigation.convertDistance(Project.WHEEL_RAD, correction), true);
			}
		}
		;
	}
}