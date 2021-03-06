package org.firstinspires.ftc.teamcode.ignoreThis;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import java.util.Queue;
import java.util.LinkedList;

abstract class AutoBase extends LinearOpMode {
    /**
     * Clock to time operations
     */
    private ElapsedTime clock = new ElapsedTime();

    /**
     * Right color sensor
     */
    ColorSensor CBR;

    /**
     * Left color sensor
     */
    ColorSensor CBL;

    /**
     * Front-right Servo
     */
    DcMotor FR = null;

    /**
     * Front-left Servo
     */
    DcMotor FL = null;

    /**
     * Back-right Servo
     */
    DcMotor BR = null;

    /**
     * Back-left Servo
     */
    DcMotor BL = null;

    /**
     * Jewel Servo
     */
    Servo JS = null;

    /**
     * Left grabber servo
     */
    Servo SL = null;

    /**
     * Right grabber servo
     */
    Servo SR = null;

    DcMotor lift = null;
    /**
     * Right servo closed position
     */
    static final double RIGHT_SERVO_OPEN = 1 - 0.36;
    /**
     * Left servo closed position
     */
    static final double LEFT_SERVO_OPEN = 0.36;

    /**
     * Right servo open position
     */
    static final double RIGHT_SERVO_CLOSED = 1 - 0.73;


    /**
     * Left servo open position
     */
    static final double LEFT_SERVO_CLOSED = 0.60;

    static final double RIGHT_SERVO_AJAR = 1 - 0.62;

    static final double RELIC_FLIPUP = .90;

    static final double RELIC_FLIPDOWN = 0.42;

    static final double RELIC_PICKUP = .17;

    static final double RELIC_DROP = .33;

    static final double LIFT_FLIPUP = .78;

    static final double LIFT_FLIPDOWN = .2;


    /**
     * Left servo open position
     */
    static final double LEFT_SERVO_AJAR = 0.53;

    static final double RIGHT_SERVO_FLAT = 1 - 0.32;

    /**
     * Left servo open position
     */
    static final double LEFT_SERVO_FLAT = 0.32;
    static final double SHORT_DRIVE_TIME = 100;
    static final double SHORT_DRIVE_POWER = .5;
    static final double JEWEL_SERVO_DOWN = .75;
    static final double JEWEL_SERVO_UP = .15;
    OpenGLMatrix lastLocation = null;

    /**
     * Behaviour when the motors are stopped
     */
    static final ZeroPowerBehavior ZERO_POWER_BEHAVIOR = ZeroPowerBehavior.BRAKE;
    public void strafe(boolean strafe) {
        FR.setDirection(strafe ? DcMotor.Direction.FORWARD : DcMotor.Direction.REVERSE);
        FL.setDirection(strafe ? DcMotor.Direction.FORWARD : DcMotor.Direction.FORWARD);
        BR.setDirection(strafe ? DcMotor.Direction.REVERSE : DcMotor.Direction.REVERSE);
        BL.setDirection(strafe ? DcMotor.Direction.REVERSE : DcMotor.Direction.FORWARD);
    }
    public double getAngleDiff(double angle1, double angle2) {
        if(Math.abs(angle1 - angle2) < 180.0)
            return Math.abs(angle1-angle2);
        else if(angle1 > angle2)
        {
            angle1 -= 360;
            return Math.abs(angle2-angle1);
        }
        else
        {
            angle2 -= 360;
            return Math.abs(angle1-angle2);
        }
    }
    BNO055IMU imu = null;

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    /**
     * Jewel servo down position
     */
    /**
     * The power with which to turn when knocking off the jewel.
     */
    private static final int JEWEL_TURN_TIME = 300;
    double counter = 0;

    AnalogInput ultrasonicLeft;
    AnalogInput ultrasonicBack;
    AnalogInput ultrasonicRight;

    ColorSensor CBOT;
    CRServo leftBottom;
    CRServo rightTop;
    CRServo leftTop;
    CRServo rightBottom;
    Servo liftServo;


    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    VuforiaLocalizer vuforia;
    VuforiaLocalizer.Parameters vufParameters = null;
    VuforiaTrackables relicTrackables = null;
    VuforiaTrackable relicTemplate = null;


    /*
    Data smoothing memory
     */
    Queue<Double> smooth_left = new LinkedList<Double>();

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void runOpMode() throws InterruptedException {

        // Declare any local / helper variables here

        // Our initialization code should go here

        // Example: Map the hardware to the arm_motor variable
        telemetry.addData("Status", "Initialized");

        // Initialize the hardware variables.
        FR = hardwareMap.get(DcMotor.class, "FR");
        FL = hardwareMap.get(DcMotor.class, "FL");
        BR = hardwareMap.get(DcMotor.class, "BR");
        BL = hardwareMap.get(DcMotor.class, "BL");
        CBL = hardwareMap.get(ColorSensor.class, "CBL");
        JS = hardwareMap.get(Servo.class, "JS");
        lift = hardwareMap.get(DcMotor.class, "lift");
        SR = hardwareMap.get(Servo.class, "SR");
        SL = hardwareMap.get(Servo.class, "SL");
        ultrasonicLeft = hardwareMap.get(AnalogInput.class, "ultrasonicLeft");

        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        strafe(false);

        JS.setPosition(JEWEL_SERVO_UP);

        openGrabber();

        // Tell the driver that initialization is complete.
        telemetry.addData("Status", "Initialized");

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        while (opModeIsActive()) {
            //    getGlyph();
            pushJewel();
            break;
        }

        // ...
    }

    /**
     * Get the preloaded glyph
     */
    void lift(double power, double time) {
        clock.reset();
        while (!isStopRequested() && opModeIsActive() && clock.milliseconds() < time) {
            if (!isStopRequested() && opModeIsActive()) lift.setPower(power);
        }
        if (!isStopRequested() && opModeIsActive()) lift.setPower(0);
    }
    void intake(double power) {
        if (!isStopRequested() && opModeIsActive()) rightBottom.setPower(-power);
        if (!isStopRequested() && opModeIsActive()) leftBottom.setPower(power);
        if (!isStopRequested() && opModeIsActive()) rightTop.setPower(power);
        if (!isStopRequested() && opModeIsActive()) leftTop.setPower(-power);
    }
    void intake(double power, double time) {
        clock.reset();
        while(!isStopRequested() && opModeIsActive() && clock.milliseconds() < time) {
            if (!isStopRequested() && opModeIsActive()) rightBottom.setPower(-power);
            if (!isStopRequested() && opModeIsActive()) leftBottom.setPower(power);
            if (!isStopRequested() && opModeIsActive()) rightTop.setPower(power);
            if (!isStopRequested() && opModeIsActive()) leftTop.setPower(-power);
        }
        if (!isStopRequested() && opModeIsActive()) rightBottom.setPower(0);
        if (!isStopRequested() && opModeIsActive()) leftBottom.setPower(0);
        if (!isStopRequested() && opModeIsActive()) rightTop.setPower(0);
        if (!isStopRequested() && opModeIsActive()) leftTop.setPower(0);
    }
    void stopIntake() {
        if (!isStopRequested() && opModeIsActive()) rightBottom.setPower(0);
        if (!isStopRequested() && opModeIsActive()) leftBottom.setPower(0);
        if (!isStopRequested() && opModeIsActive()) rightTop.setPower(0);
        if (!isStopRequested() && opModeIsActive()) leftTop.setPower(0);
    }
    void outtake(double power) {
        if (!isStopRequested() && opModeIsActive()) rightBottom.setPower(power);
        if (!isStopRequested() && opModeIsActive()) leftBottom.setPower(-power);
        if (!isStopRequested() && opModeIsActive()) rightTop.setPower(-power);
        if (!isStopRequested() && opModeIsActive()) leftTop.setPower(power);
    }
    void outtake(double power, double time) {
        clock.reset();
        while(!isStopRequested() && opModeIsActive() && clock.milliseconds() < time) {
            if (!isStopRequested() && opModeIsActive()) rightBottom.setPower(power);
            if (!isStopRequested() && opModeIsActive()) leftBottom.setPower(-power);
            if (!isStopRequested() && opModeIsActive()) rightTop.setPower(-power);
            if (!isStopRequested() && opModeIsActive()) leftTop.setPower(power);
        }
        if (!isStopRequested() && opModeIsActive()) rightBottom.setPower(0);
        if (!isStopRequested() && opModeIsActive()) leftBottom.setPower(0);
        if (!isStopRequested() && opModeIsActive()) rightTop.setPower(0);
        if (!isStopRequested() && opModeIsActive()) leftTop.setPower(0);
    }

    /**
     * Push the correct jewel
     */
    void pushJewel() {
        double power = 0.6;
        if (!isStopRequested() && opModeIsActive()) initGyro();
        if (!isStopRequested() && opModeIsActive()) JS.setPosition(JEWEL_SERVO_DOWN);
        if (!isStopRequested() && opModeIsActive()) delay(1500);
        if (isRed() && !isStopRequested() && opModeIsActive()) {
            if (get_colors() == JewelPosition.RED_JEWEL_LEFT && !isStopRequested() && opModeIsActive()) {
                if (!isStopRequested() && opModeIsActive())  drive(.15, 0, 0, JEWEL_TURN_TIME);
                if (!isStopRequested() && opModeIsActive()) JS.setPosition(JEWEL_SERVO_UP);
                if (!isStopRequested() && opModeIsActive()) delay(500);
                if (!isStopRequested() && opModeIsActive())  drive(-.15, 0, 0, JEWEL_TURN_TIME);
            } else if (get_colors() == JewelPosition.RED_JEWEL_RIGHT && !isStopRequested() && opModeIsActive()) {
                if (!isStopRequested() && opModeIsActive())  drive(-.15, 0, 0, JEWEL_TURN_TIME);
                if (!isStopRequested() && opModeIsActive())  JS.setPosition(JEWEL_SERVO_UP);
                if (!isStopRequested() && opModeIsActive())  delay(500);
                if (!isStopRequested() && opModeIsActive())   drive(.15, 0, 0, JEWEL_TURN_TIME);
            } else {
                if (!isStopRequested() && opModeIsActive())  JS.setPosition(JEWEL_SERVO_UP);
                if (!isStopRequested() && opModeIsActive())  delay(500);
            }
        } else {
            if (get_colors() == JewelPosition.RED_JEWEL_RIGHT && !isStopRequested() && opModeIsActive()) {
                if (!isStopRequested() && opModeIsActive())  drive(.15, 0, 0, JEWEL_TURN_TIME);
                if (!isStopRequested() && opModeIsActive())  JS.setPosition(JEWEL_SERVO_UP);
                if (!isStopRequested() && opModeIsActive())  delay(500);
                if (!isStopRequested() && opModeIsActive())  drive(-.15, 0, 0, JEWEL_TURN_TIME);
            } else if (get_colors() == JewelPosition.RED_JEWEL_LEFT) {
                if (!isStopRequested() && opModeIsActive())  drive(-.15, 0, 0, JEWEL_TURN_TIME);
                if (!isStopRequested() && opModeIsActive())   JS.setPosition(JEWEL_SERVO_UP);
                if (!isStopRequested() && opModeIsActive())  delay(500);
                if (!isStopRequested() && opModeIsActive())  drive(.15, 0, 0, JEWEL_TURN_TIME);
            } else {
                if (!isStopRequested() && opModeIsActive())   JS.setPosition(JEWEL_SERVO_UP);
                if (!isStopRequested() && opModeIsActive())  delay(500);
            }
        }
    }

    public void delay(int time) {
        telemetry.addData("delay", "started delay");
        clock.reset();
        while (!isStopRequested() && opModeIsActive() && time > clock.milliseconds()) {
            telemetry.addData("time: ", clock.milliseconds());
            telemetry.update();
        }
    }

    public void drive(double turn, double drive_x, double drive_y, double time) {
        counter = getRuntime() * 1000.0;
        while (!isStopRequested() && opModeIsActive() && getRuntime() * 1000.0 < counter + time) {
            drive(turn, drive_x, drive_y);
        }
        stopRobot();
    }

    public void stopRobot() {
        drive(0, 0, 0);
    }

    public void drive(double turn, double drive_x, double drive_y) {
        double leftPower;
        double rightPower;
        double startTime = clock.milliseconds();

        telemetry.addData("CBL R,G,B", "(" + CBL.red() + ", " + CBL.green() + ", " + CBL.blue() + ")");

        if (Math.abs(turn) < .15) {
            turn = 0;
        }

        if (Math.abs(drive_y) > .15) {
            telemetry.addData("Status", "Driving");
            strafe(false);
            leftPower = Range.clip(drive_y + turn, -1.0, 1.0);
            rightPower = Range.clip(drive_y - turn, -1.0, 1.0);
            FL.setPower(leftPower);
            BL.setPower(leftPower);
            FR.setPower(rightPower);
            BR.setPower(rightPower);
        } else if (Math.abs(drive_x) > .15) {
            telemetry.addData("Status", "Strafing");
            strafe(true);

            leftPower = Range.clip(drive_x + turn, -1.0, 1.0);
            rightPower = Range.clip(drive_x - turn, -1.0, 1.0);

            FL.setPower(leftPower);
            BL.setPower(1.25 * rightPower);
            FR.setPower(1.25 * leftPower);
            BR.setPower(rightPower);
        } else {
            telemetry.addData("Status", "Turning");
            strafe(false);

            leftPower = Range.clip(turn, -1.0, 1.0);
            rightPower = Range.clip(-turn, -1.0, 1.0);

            FL.setPower(leftPower);
            BL.setPower(leftPower);
            FR.setPower(rightPower);
            BR.setPower(rightPower);

        }
        telemetry.update();
        FL.setPower(0);
        BL.setPower(0);
        FR.setPower(0);
        BR.setPower(0);
    }
    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */


    /*
    * The function currently returns the location of the Red Jewel.
    * It can be refactored later to return a color based on the
    * alliance, but this version makes no assumption about what jewel
    * the user wants to know about.
    */
    public JewelPosition get_colors() {
        if (CBL.red() > CBL.blue()) {
            return JewelPosition.RED_JEWEL_RIGHT;
        } else if (CBL.red() < CBL.blue()) {
            return JewelPosition.RED_JEWEL_LEFT;
        }
        return JewelPosition.JEWEL_INCONCLUSIVE;
    }

    public abstract Alliance getAlliance();

    private boolean isRed() {
        return getAlliance() == Alliance.RED;
    }

    private boolean isBlue() {
        return getAlliance() == Alliance.BLUE;
    }

    /**
     * Close the grabber
     */
    void closeGrabber() {
        if (SR.getPosition() != RIGHT_SERVO_CLOSED) {
            SR.setPosition(RIGHT_SERVO_CLOSED);
            SL.setPosition(LEFT_SERVO_CLOSED);
        }
    }

    /**
     * Open the grabber
     */
    void openGrabber() {
        SR.setPosition(RIGHT_SERVO_OPEN);
        SL.setPosition(LEFT_SERVO_OPEN);
    }

    void openGrabberFlat() {
        SR.setPosition(RIGHT_SERVO_FLAT);
        SL.setPosition(LEFT_SERVO_FLAT);
    }

    public enum Alliance {
        BLUE, RED
    }

    public enum JewelPosition {
        RED_JEWEL_LEFT, RED_JEWEL_RIGHT, JEWEL_INCONCLUSIVE
    }

    public void initVuforia() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        vufParameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // OR...  Do Not Activate the Camera Monitor View, to save power
        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        /*
         * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
         * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
         * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
         * web site at https://developer.vuforia.com/license-manager.
         *
         * Vuforia license keys are always 380 characters long, and look as if they contain mostly
         * random data. As an example, here is a example of a fragment of a valid key:
         *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
         * Once you've obtained a license key, copy the string from the Vuforia web site
         * and paste it in to your code onthe next line, between the double quotes.
         */
        vufParameters.vuforiaLicenseKey = "Aeba4Qn/////AAAAGahNOxzreUE8nItPWzsrOlF7uoyrR/qbjue3kUmhxZcfZMSd5MRyEY+3uEoVA+gpQGz5KyP3wEjBxSOAb4+FBYMZ+QblFU4byMG4+aiI+GeeBA+RatQXVzSduRBdniCW4qehTnwS204KTUMXg1ioPvUlbYQmqM5aPMx/2xnYN1b+htNBWV0Bc8Vkyspa0NNgz7PzF1gozlCIc9FgzbzNYoOMhqSG+jhKf47SZQxD6iEAtj5iAkWBvJwFDLr/EfDfPr3BIA6Cpb4xaDc0t4Iz5wJ/p4oLRiEJaLoE/noCcWFjLmPcw9ccwYXThtjC+7u0DsMX+r+1dMikBCZCWWkLzEyjWzy3pOOR3exNRYGZ0vzr";

        /*
         * We also indicate which camera on the RC that we wish to use.
         * Here we chose the back (HiRes) camera (for greater range), but
         * for a competition robot, the front camera might be more convenient.
         */
        vufParameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(vufParameters);

        /**
         * Load the data set containing the VuMarks for Relic Recovery. There's only one trackable
         * in this data set: all three of the VuMarks in the game were created from this one template,
         * but differ in their instance id information.
         * @see VuMarkInstanceId
         */
        relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary
        relicTrackables.activate();
        telemetry.addData("relic", "activated");
    }

    public RelicRecoveryVuMark getPicto() {
        int counts = 0;
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
            return vuMark;
        }
        clock.reset();
        while (!isStopRequested() && opModeIsActive() && clock.milliseconds() < 2000 && counts < 50) {
            vuMark = RelicRecoveryVuMark.from(relicTemplate);
            if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
                return vuMark;
            }
            counts++;
        }
        return vuMark;
    }

    double angle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        return AngleUnit.DEGREES.fromUnit(angles.angleUnit, angles.firstAngle);
    }

    public void turn(double power, double angle) {
        telemetry.addData("test", "test");
        clock.reset();
        double startingAngle = angle();
        while (!isStopRequested() && opModeIsActive() && 6000 > clock.milliseconds() && getAngleDiff(startingAngle, angle()) < angle) {
            telemetry.addData("not working", "plz");
            telemetry.addData("angleDiff", getAngleDiff(startingAngle, angle()));
            telemetry.addData("startingAngle", startingAngle);
            if (angle() - getAngleDiff(startingAngle, angle()) < 20.0) {
                drive((power / Math.abs(power)) * .15, 0, 0);
            } else {
                drive(power, 0, 0);
            }
            telemetry.update();
        }
        FL.setPower(0);
        BL.setPower(0);
        FR.setPower(0);
        BR.setPower(0);
    }

    public void initGyro() {
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        imu.initialize(parameters);
        ultrasonicLeft = hardwareMap.get(AnalogInput.class, "ultrasonicLeft");
        ultrasonicBack = hardwareMap.get(AnalogInput.class, "ultrasonicBack");
        ultrasonicRight = hardwareMap.get(AnalogInput.class, "ultrasonicRight");
    }

    int i;
    public double getLeftDist() {
        return ultrasonicLeft.getVoltage() * 1000 / 6.4;
    }
    public double getRightDist() {
        return ultrasonicLeft.getVoltage() * 1000 / 6.4;
    }
    public double getBackDist() {
        return ultrasonicBack.getVoltage() * 1000 / 6.4;
    }


    public void driveUntilColorRed(double power) {
        while (!isStopRequested() && opModeIsActive() && CBOT.red() < 15) {
            drive(0, power, 0);
        }
        drive(0, 0, 0);
    }
    public void driveUntilColorRedForward(double power) {
        while (!isStopRequested() && opModeIsActive() && CBOT.red() < 15) {
            drive(0, 0, power);
        }
        drive(0, 0, 0);
    }
    public void driveUntilColorBlue(double power) {
        while (!isStopRequested() && opModeIsActive() && CBOT.blue() < 12) {
            drive(0, power, 0);
        }
        drive(0, 0, 0);
    }

    public void driveUntilColorBlueForward(double power) {
        while (!isStopRequested() && opModeIsActive() && CBOT.blue() < 12) {
            drive(0, 0, power);
        }
        drive(0, 0, 0);
    }
}