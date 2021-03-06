package org.firstinspires.ftc.teamcode.GR_Worlds.Autonomous;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

abstract class  WorldsMasterAuto extends LinearOpMode {

    //***********************************HARDWARE INSTANTIATIONS************************************

    //motor + servos and stuff
    ColorSensor colorSensor;
    public ElapsedTime runtime = new ElapsedTime();
    public DcMotor FrontLeft = null;
    public DcMotor FrontRight = null;
    public DcMotor BackLeft = null;
    public DcMotor BackRight = null;
    public int pos = 0;
    public DcMotor Pulley = null;
    public DcMotor NomLeft = null;
    public DcMotor NomRight = null;
    public Servo Servo1 = null;
    public Servo VerticalColorServo = null;
    public Servo HorizontalColorServo = null;
    public Servo elbowServo = null;
    public Servo backPlate = null;
    public Servo jewelHolder = null;
    double startTime = runtime.milliseconds();

    //imu
    BNO055IMU imu;
    Orientation angles;
    Acceleration gravity;

    //other things
    ElapsedTime clock = new ElapsedTime();
    public double veryStartAngle;
    public double facingCryptoAngle;
    static final double COLUMN_TURN_ANGLE = 16;

    //jewel servo positions
    static final double VERTICAL_JEWELSERVO_UP = .9;
    static final double VERTICAL_JEWELSERVO_MID = .6;
    static final double VERTICAL_JEWELSERVO_DOWN = .45;
    static final double HORIZONTAL_JEWELSERVO_MID = .78; //was .73
    static final double HORIZONTAL_JEWELSERVO_TURN = .1; //how much the color servo should turn in either direction
    static final double HORIZONTAL_JEWELSERVO_CCW = HORIZONTAL_JEWELSERVO_MID - HORIZONTAL_JEWELSERVO_TURN;
    static final double HORIZONTAL_JEWELSERVO_CW = HORIZONTAL_JEWELSERVO_MID + HORIZONTAL_JEWELSERVO_TURN;
    static final double FLIP_IN = .18;
    static final double FLIP_OUT = .5;

    //vuforia
    OpenGLMatrix lastLocation = null;
    VuforiaLocalizer vuforia;
    VuforiaLocalizer.Parameters vufParameters = null;
    VuforiaTrackables relicTrackables = null;
    VuforiaTrackable relicTemplate = null;


    //*************************************INITIALIZATION FUNCTIONS*********************************

    public void declare() {
        FrontLeft = hardwareMap.get(DcMotor.class, "FrontLeft");
        BackLeft = hardwareMap.get(DcMotor.class, "BackLeft");
        FrontRight = hardwareMap.get(DcMotor.class, "FrontRight");
        BackRight = hardwareMap.get(DcMotor.class, "BackRight");
        Pulley = hardwareMap.get(DcMotor.class, "Pulley");
        Servo1 = hardwareMap.get(Servo.class, "Servo1");
        colorSensor = hardwareMap.get(ColorSensor.class, "colorSensor");
        VerticalColorServo = hardwareMap.get(Servo.class, "colorServo");
        HorizontalColorServo = hardwareMap.get(Servo.class, "jewelTwister");
        elbowServo = hardwareMap.get(Servo.class, "elbowServo");
        backPlate = hardwareMap.get(Servo.class, "backPlate");
        NomLeft = hardwareMap.get(DcMotor.class, "NomLeft");
        NomRight = hardwareMap.get(DcMotor.class, "NomRight");
        jewelHolder = hardwareMap.get(Servo.class, "jewelHolder");

        //WheelOne.setDirection(DcMotor.Direction.FORWARD);
        // Most robots need the motor on one side to be reversed to drive forward
        // Reverse the motor that runs backwards when connected directly to the battery
        FrontLeft.setDirection(DcMotor.Direction.REVERSE);
        BackLeft.setDirection(DcMotor.Direction.REVERSE);
        FrontRight.setDirection(DcMotor.Direction.FORWARD);
        BackRight.setDirection(DcMotor.Direction.FORWARD);
        Pulley.setDirection(DcMotor.Direction.FORWARD);
        NomLeft.setDirection(DcMotor.Direction.FORWARD);
        NomRight.setDirection(DcMotor.Direction.REVERSE);

        FrontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BackLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FrontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BackRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        VerticalColorServo.setPosition(VERTICAL_JEWELSERVO_UP);
        HorizontalColorServo.setPosition(HORIZONTAL_JEWELSERVO_MID);
        jewelHolder.setPosition(.2);
        pos = getTicks();
    }

    public int getTicks() {
        return FrontRight.getCurrentPosition() - pos;
    }

    public void resetTicks() {
        pos = FrontRight.getCurrentPosition();
    }

    public void moveTicksForward(double pow, double ticks) {
        moveTicks(pow, pow, pow, pow, ticks);
    }

    public void moveTicksBack(double pow, double ticks) {
        moveTicks(-pow, -pow, -pow, -pow, -ticks);
    }

    public void moveTicksLeft(double pow, double ticks) {
        moveTicks(-pow, pow, -pow, pow, ticks);
    }

    public void moveTicksRight(double pow, double ticks) {
        moveTicks(pow, -pow, pow, -pow, -ticks);
    }

    private void moveTicks(double fl, double bl, double fr, double br, double ticks) {
        resetTicks();
        runtime.reset();
        while (opModeIsActive() && !isStopRequested() && Math.abs(getTicks()) < Math.abs(ticks) && runtime.milliseconds() < 10000) {
            FrontLeft.setPower(fl);
            FrontRight.setPower(fr);
            BackRight.setPower(br);
            BackLeft.setPower(bl);
            telemetry.addData("Target", ticks);
            telemetry.addData("Current", getTicks());
            telemetry.update();
        }
        FrontLeft.setPower(0);
        FrontRight.setPower(0);
        BackRight.setPower(0);
        BackLeft.setPower(0);
    }

    public void moveUntilLine(double pow) {
//        boolean noLine = true;
//        while(noLine) {
//            telemetry.addLine("blue reading: "+colorSensorLine.blue());
//            telemetry.addLine("red reading: "+colorSensorLine.red());
//            telemetry.addLine("green reading: "+colorSensorLine.green());
//
////            if(colorSensorLine.){
////            }
////            FrontLeft.setPower(pow);
////            FrontRight.setPower(pow);
////            BackRight.setPower(pow);
////            BackLeft.setPower(pow);
//
//        }
    }


    public void initGyro() {
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
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

//    public void delay(int time) { DO NOT USE DELAY IT IS CODE CANCER
//        double startTime = runtime.milliseconds();
//        while (runtime.milliseconds() - startTime < time && opModeIsActive()) {
//        }
//    }

    //***************************************MOTION FUNCTIONS***************************************
    public void StopDriving() {
        FrontLeft.setPower(0);
        FrontRight.setPower(0);
        BackLeft.setPower(0);
        BackRight.setPower(0);
    }

    public void moveForward(double power, int time) {
        FrontLeft.setPower(power);
        FrontRight.setPower(power);
        BackLeft.setPower(power);
        BackRight.setPower(power);
        sleep(time);
        FrontLeft.setPower(0);
        FrontRight.setPower(0);
        BackLeft.setPower(0);
        BackRight.setPower(0);
    }

    public void moveBackward(double power, int time) {
        FrontLeft.setPower(-power);
        FrontRight.setPower(-power);
        BackLeft.setPower(-power);
        BackRight.setPower(-power);
        sleep(time);
        FrontLeft.setPower(0);
        FrontRight.setPower(0);
        BackLeft.setPower(0);
        BackRight.setPower(0);
    }

    public void strafeRight(double power, int time) {
        FrontLeft.setPower(power);
        FrontRight.setPower(-power);
        BackLeft.setPower(-power);
        BackRight.setPower(power);
        sleep(time);
        FrontLeft.setPower(0);
        FrontRight.setPower(0);
        BackLeft.setPower(0);
        BackRight.setPower(0);
    }

    public void strafeLeft(double power, int time) {
        FrontLeft.setPower(-power);
        FrontRight.setPower(power);
        BackLeft.setPower(power);
        BackRight.setPower(-power);
        sleep(time);
        FrontLeft.setPower(0);
        FrontRight.setPower(0);
        BackLeft.setPower(0);
        BackRight.setPower(0);
    }

    public void turnRight(double power, int time) {
        turn(power);
        sleep(time);
        StopDriving();
    }

    public void turnLeft(double power, int time) {
        turn(-power);
        sleep(time);
        StopDriving();
    }

    public void turn(double turn) { //positive value is right, negative value is left
        FrontLeft.setPower(turn);
        BackLeft.setPower(turn);
        FrontRight.setPower(-turn);
        BackRight.setPower(-turn);
    }

    public void nom(double power) { //positive value is right, negative value is left
        NomRight.setPower(power);
        NomLeft.setPower(power);
    }


    //**********************************VUFORIA FUNCTIONS*******************************************

    public RelicRecoveryVuMark getPicto() {
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
            telemetry.addData("Mark", vuMark);
            telemetry.update();
            return vuMark;
        }
        clock.reset();
        while (clock.milliseconds() < 1500 && opModeIsActive() && !isStopRequested()) {
            vuMark = RelicRecoveryVuMark.from(relicTemplate);
            if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
                telemetry.addData("Mark", vuMark);
                telemetry.update();
                return vuMark;
            }
        }
        telemetry.addData("Mark", vuMark);
        telemetry.update();
        return vuMark;
    }

    public RelicRecoveryVuMark pictograph() {
        double startTime = getRuntime() * 1000;
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);

        if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
            return vuMark;
        } else {
            while (startTime - getRuntime() < 2000 && opModeIsActive() && !isStopRequested()) {
                vuMark = RelicRecoveryVuMark.from(relicTemplate);
                if (vuMark != RelicRecoveryVuMark.UNKNOWN) {
                    return vuMark;
                }
            }
        }
        return vuMark;
    }

    //******************************GYRO + TURNING FUNCTIONS****************************************

    double currentAngle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        return AngleUnit.DEGREES.fromUnit(angles.angleUnit, angles.firstAngle);
    }

    public void setBaseAngles(String team) {
        veryStartAngle = currentAngle();
        setDefaultAngle(team);
    }

    public void setDefaultAngle(String team) {
        if (team == "red1")
            facingCryptoAngle = currentAngle() - 90; //IF CODE BREAKS try making this (currentAngle()+90)%360
        else if (team == "blue1") facingCryptoAngle = currentAngle() - 90;
        else if (team == "blue2")
            facingCryptoAngle = currentAngle() + 180; //IF CODE BREAKS try making this (currentAngle()+180)%360
        else facingCryptoAngle = currentAngle();
    }

    public void turnAngle(double rawAngle) throws InterruptedException {
        telemetry.addData("I have a turn now", "");
        telemetry.update();
        double angle = rawAngle % 360;
        if (angle == 0) return;
        else if (angle > 0 && angle < 180) turnAngleCW(angle);
        else if (angle < -180) turnAngleCW(angle + 360);
        else if (angle < 0 && angle > -180) turnAngleCCW(-angle);
        else turnAngleCCW(360 - angle);
    }

    public void specialTurnAngleCCW(double angle) throws InterruptedException {
        if (angle < 90) turnAngleCCW(-angle);
        else {
            double startingAngle = currentAngle();
            double goal = (currentAngle() + angle);
            while ((getAngleDiff(startingAngle, currentAngle()) < angle - 4) && opModeIsActive() && !isStopRequested()) {
                if (getAngleDiff(currentAngle(), goal) > 90) turn(-.5);
                else {
                    turnAngleCCW(90);
                    return;
                }
            }
            StopDriving();
        }
    }

    public void turnAngleCW(double angle) {
        double startingAngle = currentAngle();
        while ((getAngleDiff(startingAngle, currentAngle()) < angle - 4) && opModeIsActive() && !isStopRequested()) {
            double difference = ((angle - getAngleDiff(startingAngle, currentAngle())) / (angle * 2));
            telemetry.addData("difference", difference);
            telemetry.update();
            if (difference > .15) turn(difference);
            else turn(.22);
            telemetry.addData("I BROKE", opModeIsActive());
            telemetry.update();
        }
        StopDriving();
    }

    public void turnAngleCCW(double angle) {
        while (opModeIsActive() && !isStopRequested()) {
            double startingAngle = currentAngle();
            while (getAngleDiff(startingAngle, currentAngle()) < angle - 4 && opModeIsActive() && !isStopRequested()) {
                double difference = ((angle - getAngleDiff(startingAngle, currentAngle())) / (angle * 2));
                telemetry.addData("difference", difference);
                telemetry.update();
                if (difference > .15) turn(-difference);
                else turn(-.22);
                if (!opModeIsActive()) {
                    telemetry.addData("I BROKE", opModeIsActive());
                    telemetry.update();
                    break;
                }
            }
            StopDriving();
            break;
        }
        return;
    }

    public double getAngleDiff(double angle1, double angle2) {
        if (Math.abs(angle1 - angle2) < 180.0)
            return Math.abs(angle1 - angle2);
        else if (angle1 > angle2) {
            angle1 -= 360;
            return Math.abs(angle2 - angle1);
        } else {
            angle2 -= 360;
            return Math.abs(angle1 - angle2);
        }
    }

    //*******************************SEQUENCE MOTION FUNCTIONS******************************************
    public void jewelSequence(String team) throws InterruptedException {
        while (opModeIsActive() && !isStopRequested()) {
            String direction = "ERROR";
            jewelHolder.setPosition(.8);
            sleep(500);
            VerticalColorServo.setPosition(VERTICAL_JEWELSERVO_DOWN);
            Boolean jewelBlue = null;
            Boolean jewel_has_been_spotted;
            //read color
            int red = 0;
            int blue = 0;
            clock.reset();
            while (clock.milliseconds() < 1500 && opModeIsActive() && !isStopRequested()) {
                for (int i = 0; i < 40; i++) {
                    if (colorSensor.red() > colorSensor.blue() && colorSensor.red() > .15) red++;
                    if (colorSensor.red() < colorSensor.blue() && colorSensor.blue() > .15) blue++;
                }
                break;
            }
            telemetry.addLine("read color");
            telemetry.update();

            //decide which color we see
            if (blue > red) {
                jewelBlue = true;
                telemetry.addData("blueWins!", blue);
                jewel_has_been_spotted = true;
            } else if (red > blue) {
                jewelBlue = false;
                telemetry.addData("redWins!", red);
                jewel_has_been_spotted = true;
            } else { //if the color sensor doesn't see either red or blue
                jewel_has_been_spotted = false;
                telemetry.addLine("DIDN'T SEE A JEWEL");
            }
            telemetry.addData("blue: ", blue);
            telemetry.addData("red: ", red);
            telemetry.update();

            //figure out which way to turn to knock off correct jewel
            if (jewel_has_been_spotted) { //so we dont knock off a rando one if we see nothing
                if (team == "red") {
                    if (jewelBlue) direction = "CW"; //turn clockwise
                    else direction = "CCW"; //turn counterclockwise
                } else if (team == "blue") {
                    if (!jewelBlue) direction = "CW"; //turn clockwise
                    else direction = "CCW"; //turn counterclockwise
                }
                //knock off the correct jewel
                if (direction == "CW") HorizontalColorServo.setPosition(HORIZONTAL_JEWELSERVO_CW);
                else if (direction == "CCW")
                    HorizontalColorServo.setPosition(HORIZONTAL_JEWELSERVO_CCW);
                sleep(400);
            }
            VerticalColorServo.setPosition(VERTICAL_JEWELSERVO_MID);
            sleep(100);
            HorizontalColorServo.setPosition(HORIZONTAL_JEWELSERVO_MID);
            sleep(100);
            VerticalColorServo.setPosition(VERTICAL_JEWELSERVO_UP);
            sleep(200);
            break;
        }
        return;
    }

    public void turnToColumnSequence(RelicRecoveryVuMark column, boolean red1) throws InterruptedException {
        while (opModeIsActive() && !isStopRequested()) {
            turnAngle(currentAngle() - facingCryptoAngle);
            telemetry.addData("turnAngle", currentAngle() - facingCryptoAngle);
            //TURN TO THE CORRECT COLUMN
            if (column == RelicRecoveryVuMark.CENTER) {
            } else if (column == RelicRecoveryVuMark.LEFT || column == RelicRecoveryVuMark.UNKNOWN) {
                turnAngle(-COLUMN_TURN_ANGLE);//fill w left value
            } else {
                if (red1) turnAngle(COLUMN_TURN_ANGLE+2);
                else turnAngle(COLUMN_TURN_ANGLE);//fill w right value
            }
            break;
        }
        return;
    }

    public void returntoCenterSequence(boolean end) throws InterruptedException {
        while (opModeIsActive() && !isStopRequested()) {
            moveTicksForward(.4, 250);
            sleep(100);
            turnAngle(currentAngle() - facingCryptoAngle);
            //if (end) moveTicksBack(.4, 150);
            break;
        }
        return;
    }

    public void placeGlyphSequence(RelicRecoveryVuMark column, boolean red1) throws InterruptedException {
        while (opModeIsActive() && !isStopRequested()) {
            turnToColumnSequence(column, red1);
            backPlate.setPosition(.9);
            sleep(100);
            moveTicksBack(.4, 100);
            Servo1.setPosition(FLIP_OUT);
            moveTicksBack(.4, 150);
            sleep(500);
            moveBackward(.4, 200);
            sleep(200);
            turnAngle(-10);
            moveTicksForward(.4, 50);
            turnAngle(10);
            moveTicksForward(.4, 275);
            Servo1.setPosition(FLIP_IN);
            backPlate.setPosition(.3);
            sleep(100);
            returntoCenterSequence(false);
            break;
        }
        return;
    }

    public void placeSecondGlyphSequence(RelicRecoveryVuMark column, boolean red1) throws InterruptedException {
        while (opModeIsActive() && !isStopRequested()) {
            RelicRecoveryVuMark newcolumn;
            //Figure out what column is empty
            if (column == RelicRecoveryVuMark.CENTER) {
                newcolumn = RelicRecoveryVuMark.LEFT;  // if the first glyph was in the center, put the second in the left
            } else if (column == RelicRecoveryVuMark.LEFT || column == RelicRecoveryVuMark.UNKNOWN) {
                newcolumn = RelicRecoveryVuMark.RIGHT; // if the first glyph was in the left, put the second in the right
            } else { // if the first glyph was in the right, put the second in the left
                newcolumn = RelicRecoveryVuMark.LEFT;
            }
            telemetry.addLine("placing extra glyphs");
            telemetry.update();
            turnToColumnSequence(newcolumn, false);
            elbowServo.setPosition(.5);
            sleep(100);
            Servo1.setPosition(.4);
            backPlate.setPosition(.9);
            moveTicksBack(.4, 50);
            Pulley.setPower(.6);
            sleep(100);
            Pulley.setPower(0);
            sleep(100);
            moveTicksBack(.5, 325);
            sleep(350);
            Servo1.setPosition(FLIP_OUT);
            moveBackward(.5, 400);
            backPlate.setPosition(.9);
            sleep(350);
            moveTicksForward(.5, 250);
            moveTicksBack(.5, 250);
            moveTicksForward(.5, 250);
            if(red1) moveTicksForward(.5, 250);
            Servo1.setPosition(FLIP_IN);
            backPlate.setPosition(.3);
            Pulley.setPower(-.5);
            sleep(100);
            Pulley.setPower(0);
            sleep(100);
            telemetry.addLine("glyphs placed, backing out");
            telemetry.update();
            returntoCenterSequence(true);
            break;
        }
        return;
    }

    public void getMoreGlyphsStone1() throws InterruptedException {
        while (opModeIsActive() && !isStopRequested()) {
            nom(.9);
            telemetry.addLine("the nom is on");
            telemetry.update();
            turnAngle(currentAngle() - facingCryptoAngle); //correct angle to face cryptobox
            sleep(100);
            moveTicksForward(.4, 1900); //drive straight into the glyph pile
            telemetry.addLine("eating glyphs!");
            telemetry.update();
            sleep(200); //give it time to eat
//        turnAngle(currentAngle()-(facingCryptoAngle+40)); //turn 40 degrees to eat from a diff angle in case the first had no glyphs
//        sleep(500);
            turnAngle(currentAngle() - facingCryptoAngle); //correct angle to face cryptobox
            moveTicksBack(.5, 300); //drive straight into  the glyph pile
            turnAngle(currentAngle() - facingCryptoAngle); //correct angle to face cryptobox
            sleep(200);
            moveTicksForward(.5, 300); //drive straight out of the glyph pile
            turnAngle(currentAngle() - facingCryptoAngle); //correct angle to face cryptobox
//        sleep(100);
            telemetry.addLine("I'm done eating!");
            telemetry.update();
            sleep(100);
            turnAngle(currentAngle() - facingCryptoAngle); //correct angle to face cryptobox
            telemetry.addLine("I'm facing the cryptobox!");
            telemetry.update();
            //nom(-.65);
            sleep(50);
            moveTicksBack(.4, 2000); //drive back to the cryptobox
            turnAngle(currentAngle() - facingCryptoAngle); //correct angle to face cryptobox
            telemetry.addLine("I'm back at the cryptobox!");
            telemetry.update();
            break;
        }
        return;
    }

    public void getMoreGlyphsStone2(String team) throws InterruptedException {
        while (opModeIsActive() && !isStopRequested()) {
            nom(.95);
            telemetry.addLine("my nom is on!");
            telemetry.update();
            turnAngle(currentAngle() - facingCryptoAngle); //correct angle to face cryptobox
            sleep(50);
            if (team == "blue")
                moveTicksLeft(.7, 800); //strafes toward center (a bit over halfway to it)
            else if (team == "red")
                moveTicksRight(.7, 800); //strafes toward center (a bit over halfway to it)
            telemetry.addLine("I'm close to the middle!");
            telemetry.update();
            sleep(50);
            turnAngle((currentAngle() - (facingCryptoAngle))); //correct angle to face cryptobox
            sleep(50);
            moveTicksForward(.7, 1100); //drive to the pile
            telemetry.addLine("I'm at the pile!");
            telemetry.update();
            sleep(50);
            if (team == "red")
                turnAngle((currentAngle() - (facingCryptoAngle - 45))); //turns 45 degrees toward the pile
            else if (team == "blue")
                turnAngle((currentAngle() - (facingCryptoAngle + 45))); //turns 45 degrees toward the pile
            telemetry.addLine("I'm facing the pile!");
            telemetry.update();
            sleep(50);
            moveTicksForward(.8, 500); //drive into the pile
            telemetry.addLine("I'm eating in the pile!");
            telemetry.update();
            sleep(600); //give it time to eat
            telemetry.addLine("I'm done eating!");
            telemetry.update();
            moveTicksBack(.6, 500); //drive out of the pile
            sleep(50);
            turnAngle((currentAngle() - facingCryptoAngle)); //correct angle to face cryptobox wall
            sleep(50);
            moveTicksBack(.65, 1100); //drive away from the pile
            turnAngle((currentAngle() - facingCryptoAngle)); //correct angle to face cryptobox wall
            telemetry.addLine("ready to strafe to the box");
            telemetry.update();
            sleep(50);
            if (team == "blue")
                moveTicksRight(.6, 800); //strafes back to the center of the cryptobox
            else if (team == "red")
                moveTicksLeft(.6, 800); //strafes back to the center of the cryptobox
            telemetry.addLine("I'm lined up at the cryptobox!");
            telemetry.update();
            break;
        }
        return;
    }
}