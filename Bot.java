package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command DO_NOTHING = new DoNothingCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {

        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        //Basic fix logic
        List<Object> frontblocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> rightblocks = getBlocksOnRight(myCar.position.lane, myCar.position.block, gameState);
        List<Object> leftblocks = getBlocksOnLeft(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlocks = frontblocks.subList(0,1);

        //fix first to move
        if (myCar.damage >=2){
            return FIX;
        }
        else {
            //cek posisi musuh
            if (opponent.position.block > myCar.position.block - 5 && opponent.position.block < myCar.position.block + 20) {
                if (opponent.position.block > myCar.position.block) {
                    if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                        return EMP;
                    } else {
                        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                            return BOOST;
                        } else {
                            if (frontblocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL) || nextBlocks.contains(Terrain.OIL_SPILL)) {
                                if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                                    return LIZARD;
                                } else {
                                    if (rightblocks.contains(Terrain.MUD) || rightblocks.contains(Terrain.WALL) || rightblocks.contains(Terrain.OIL_SPILL)) {
                                        return TURN_LEFT;
                                    } else {
                                        return TURN_RIGHT;
                                    }
                                }
                            } else {
                                //kalo kurang speed accelerate
                                if (myCar.speed < Bot.maxSpeed) {
                                    return ACCELERATE;
                                }
                            }
                        }
                    }
                } else {
                    if (opponent.position.block < myCar.position.block && hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                        return OIL;
                    } else
                        return DO_NOTHING;
                }
            }
        }

        if (myCar.speed == 0){
            return FIX;
        }

        //Basic avoidance logic
        if (frontblocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL) || nextBlocks.contains(Terrain.OIL_SPILL)) {
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
            else{
                if (rightblocks.contains(Terrain.MUD) || rightblocks.contains(Terrain.WALL) || rightblocks.contains(Terrain.OIL_SPILL)){
                    return TURN_LEFT;
                }
                else {
                    return TURN_RIGHT;
                }
            }
        }
        //kalo kurang speed accelerate
        if (myCar.speed < Bot.maxSpeed ){
            return ACCELERATE;
        }



        //Basic aggression logic
        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }
        return ACCELERATE;
    }

    private Boolean hasPowerUp(PowerUps powerCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerCheck)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    private List<Object> getBlocksOnLeft(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock2 = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 2);
        for (int i = block - startBlock2; i <= block - startBlock2 + 15; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }

    private List<Object> getBlocksOnRight(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock3 = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane);
        for (int i = block - startBlock3; i <= block - startBlock3 + 15; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }

}