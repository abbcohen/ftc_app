package org.firstinspires.ftc.teamcode.ignoreThis;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;

@Disabled
@Autonomous(name = "RedCorner")
public class RedNotCorner extends AutoBase {
    RelicRecoveryVuMark column = RelicRecoveryVuMark.UNKNOWN;
    public Alliance getAlliance() {
        return Alliance.RED;
    }
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");

        // Initialize the hardware variables.
        FR = hardwareMap.get(DcMotor.class, "FR");
        FL = hardwareMap.get(DcMotor.class, "FL");
        BR = hardwareMap.get(DcMotor.class, "BR");
        BL = hardwareMap.get(DcMotor.class, "BL");
        CBL = hardwareMap.get(ColorSensor.class, "CBL");
        CBOT = hardwareMap.get(ColorSensor.class, "CBOT");
        JS = hardwareMap.get(Servo.class, "JS");
        lift = hardwareMap.get(DcMotor.class, "lift");
        liftServo = hardwareMap.get(Servo.class, "liftServo");
        rightBottom = hardwareMap.get(CRServo.class, "rightBottom");
        leftBottom = hardwareMap.get(CRServo.class, "leftBottom");
        rightTop = hardwareMap.get(CRServo.class, "rightTop");
        leftTop = hardwareMap.get(CRServo.class, "leftTop");

        FR.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
        FL.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
        BR.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
        BL.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
        lift.setZeroPowerBehavior(ZERO_POWER_BEHAVIOR);
        initGyro();
        initVuforia();
        //  initVuforia();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                imu.initialize(parameters);
//                initialized = true;
//            }
//        }).start();
//        startingAngle = imu.getAngularOrientation().firstAngle; //grabbers facing away from wall
//        telemetry.addData("start", startingAngle);
        telemetry.update();
        strafe(false);
        JS.setPosition(JEWEL_SERVO_UP);
        waitForStart();
        if (isStopRequested()) return;
        if (!isStopRequested() && opModeIsActive()) intake(.81);
        if (!isStopRequested() && opModeIsActive()) column = getPicto();
        if (!isStopRequested() && opModeIsActive()) lift(.7, 400);
        if (!isStopRequested() && opModeIsActive()) stopIntake();
        if (!isStopRequested() && opModeIsActive()) pushJewel();
        if (!isStopRequested() && opModeIsActive()) delay(500);
        if (!isStopRequested() && opModeIsActive()) drive(0,.3,0,1750);
        if (!isStopRequested() && opModeIsActive()) delay(500);
        if (!isStopRequested() && opModeIsActive()) driveUntilColorRedForward(.3);
        if (!isStopRequested() && opModeIsActive()) delay(500);
        if (!isStopRequested() && opModeIsActive()) {
            if (column == RelicRecoveryVuMark.CENTER || column == RelicRecoveryVuMark.UNKNOWN) {
                if (!isStopRequested() && opModeIsActive()) turn(.3, 70);
            } else if (column == RelicRecoveryVuMark.LEFT) {
                if (!isStopRequested() && opModeIsActive()) turn(.3, 40);
            } else if (column == RelicRecoveryVuMark.RIGHT) {
                if (!isStopRequested() && opModeIsActive()) turn(.3, 100);
            }
        }
        if (!isStopRequested() && opModeIsActive()) delay(500);
        if (!isStopRequested() && opModeIsActive()) drive(0, 0, .3, 300);
        if (!isStopRequested() && opModeIsActive()) lift(-.7,400);
        if (!isStopRequested() && opModeIsActive()) outtake(.81);
        if (!isStopRequested() && opModeIsActive()) delay(1000);
        if (!isStopRequested() && opModeIsActive()) stopIntake();
        if (!isStopRequested() && opModeIsActive()) delay(500);
        if (!isStopRequested() && opModeIsActive()) drive(0, 0, .3, 1800);
        if (!isStopRequested() && opModeIsActive()) delay(500);
        if (!isStopRequested() && opModeIsActive()) drive(0, 0, -.3, 350);

    }

//    public void run(int state) {
//        if (state == 0) {
//            closeGrabber();
//            delay(1000);
//            lift.setPower(.4);
//            delay(800);
//            lift.setPower(0);
//        } else if (state == 1) {
//            column = getPicto();
//        } else if (state == 10) {
//            pushJewel();
//        } else if (state == 2) {
//            delay(500);
//            drive(0, .2, 0, 2500);
//        } else if (state == 3) {
//            delay(500);
//            turn(.2, 86);
//                delay(500);
//        } else if (state == 4) {
//                delay(500);
//                if (column == RelicRecoveryVuMark.CENTER || column == RelicRecoveryVuMark.UNKNOWN) {
//                    drive(0, -.3, 0, 2000);
//                    turn(.2, 13);
//                } else if (column == RelicRecoveryVuMark.LEFT) {
//                    drive(0, -.3, 0, 2500);
//                    turn(.2, 13);
//                } else if (column == RelicRecoveryVuMark.RIGHT) {
//                    drive(0, -.3, 0, 1500);
//                    turn(.2, 13);
//                }
//            delay(500);
//        } else if (state == 5) {
//            lift.setPower(-.4);
//            delay(600);
//            lift.setPower(0);
//            delay(500);
//            openGrabberFlat();
//            delay(1000);
//        } else if (state == 6){
//            drive(0, 0, .3, 1800);
//        } else {
//            delay(500);
//            delay(500);
//            drive(0, 0, -.3, 200);
//        }
//    }
}