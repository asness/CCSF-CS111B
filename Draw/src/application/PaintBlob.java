package application;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PaintBlob extends Circle {
	private static double gravityConstant = 300;

	private double bounceTime;
	private double canvasHeight;
	private double hueRotationSpeed = 360 * new SecureRandom().nextDouble();
	private double startingYFlipped;
	private double startingHueDegrees;

	/* animation */
	private AnimationTimer animator;
	private int frameNumber = 0;
	private double frameRate = 1.0/60;
	private double lastFrameTime = 0;

	public PaintBlob(double x, double startingY, double radius, Color startingColor, double canvasHeight) {
		super(x, startingY, radius);
		this.canvasHeight = canvasHeight;
		startingYFlipped = canvasHeight - startingY;
		setFill(startingColor);
		startingHueDegrees = colorToHueDegrees(startingColor);
		// Solve for bounceTime such that y(bounceTime) == 0.
		// (See heightFunction() below)
		bounceTime = Math.sqrt(startingYFlipped / gravityConstant);
		loadAnimator();
	}

	private static double colorToHueDegrees(Color c) {
		/*  In the HSB color model:
		 *  color  : red   yel.  green cyan  blue  mag.  [red]
		 *  degrees: 000   060   120   180   240   300   [360]
		 */
		if (c == Color.RED) {
			return 0;
		} else if (c == Color.YELLOW) {
			return 60;
		} else {
			// c == Color.BLUE
			return 240; 
		}
	}

	private double heightFunction(double t) {
		/* Model a bouncing ball,
		 * using the infamous kinematic equation
		 * y(t) == y0 - g*t^2.
		 *
		 * To keep things elastic, model the parabola as a periodic function
		 * on the interval [-bounceTime, bounceTime].
		 */

		double bouncePeriod = 2 * bounceTime;
		double tAdjusted = t % bouncePeriod;
		if (tAdjusted > bounceTime)
			tAdjusted -= bouncePeriod;
		double newHeight = startingYFlipped - gravityConstant*Math.pow(tAdjusted, 2);
		return canvasHeight - newHeight;
	}

	private double hueFunction(double t) {
		// Rotate through all hue values between 0 and 360 degrees.
		double hue = (startingHueDegrees + hueRotationSpeed * t) % 360;
		return hue;
	}

	public void loadAnimator() {
		animator = new AnimationTimer() {
			@Override public void handle(long now) {
				/* handle() parameter is current clock time in nanoseconds;
				 * convert it to seconds.	
				 */
				double nowInMilliseconds = TimeUnit.MILLISECONDS.convert(now, TimeUnit.NANOSECONDS);
				double nowInSeconds = nowInMilliseconds / 1000.0;
				if (nowInSeconds > (lastFrameTime + frameRate)) {
					// 1. Increment the current frame.  
					frameNumber++;
					// 2. Refresh graphics using the new frame number.
					double thisFrameTime = frameNumber*frameRate;
					setCenterY(heightFunction(thisFrameTime));
					setFill(Color.hsb(hueFunction(thisFrameTime), 1, 1));
					// Update the time.
					lastFrameTime = nowInSeconds;
				}
			}
		};
	}

	public void startAnimation() {
		animator.start();
	}

	public void stopAnimation() {
		animator.stop();
	}

}


