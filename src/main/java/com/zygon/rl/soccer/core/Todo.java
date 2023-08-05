package com.zygon.rl.soccer.core;

/**
 *
 * @author zygon
 */
public class Todo {

    // grocery list:
    /*
    Grid
    Players
        - have possession
        - pass
        - gain possession
        - shoot
        - move ?? (more desireable, but not first priority)

        Advanced:
            - Formations?

    Ball

    Actions:
        - with posession can: pass (can miss), shoot (can miss), move
        - after action, team with posession has advantage (tbd)
    Pass/shoot ball and resolution
        Shoot
            - Angle to goal plus strength of goalie
            - A shot that is intercepted by an opponent is a pass or block
            - Real fancy: a block creates rebound and goes anywhere
        Pass
            - Goes to teammate, keep advantage
            - Goes to opponent, loose advantage
            - Calculation is shooter stats and distance against target moving, nearby opponents (with respective "zones").
            
     */
}
