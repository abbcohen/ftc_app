package org.firstinspires.ftc.teamcode.GR_Worlds.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;

@Autonomous(name = "Red 1", group = "Sensor")
public class Red1 extends WorldsMasterAuto {

    @Override
    public void runOpMode() throws InterruptedException {
        declare();
        initGyro();
        waitForStart();
        initVuforia();
        setBaseAngles("red1");

        waitForStart();

        while (opModeIsActive()) {
            RelicRecoveryVuMark column = getPicto();
            jewelSequence("red");
            sleep(200);
            //// TODO from abby: 4/14/18  test these numbers
            moveTicksBack(.4, 2000);
            sleep(200);
            turnToColumnSequence(column);
            placeGlyphSequence(column);
            break;
        }
    }
}