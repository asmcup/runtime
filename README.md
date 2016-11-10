[![Build Status](https://travis-ci.org/asmcup/runtime.svg)](https://travis-ci.org/asmcup/runtime)
[![Join the chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/asmcup/Lobby)

# asmcup

`asmcup` is a game where players create small and limited programs
to power robots in a virtual environment to compete for prizes.

It is currently in active beta development.

## Screenshot

![](http://i.imgur.com/Snvjuon.gif)

## Getting Started


The quickest way to get started is to download
[asmcup.jar](https://github.com/asmcup/runtime/releases)
and run it. This will launch the Sandbox which allows you to write, compile,
and debug your robot. **You need to have Java 8 installed to run the Jar file.**

You can find sample bots to try out over [here](https://github.com/asmcup/bots).

asmcup.jar also has command line tools:

 * `asmcup.compiler.Main` compiles assembly source into binaries
 * `asmcup.decompiler.Main` decompiles binary files into source
 * `asmcup.runtime.Main` simulates a game world via the command line

If you want to improve the Sandbox or make changes to the game code itself
you can either import the project into Eclipse or build using `gradle jar`.

## Compete

Made a robot you think can hold its own on our servers?

*Note we aren't ready for uploading until come November*

 * [Upload your robot](https://asmcup.github.io)


## Specifications

If you want to code a robot, you should take a look at the 
[SPEC.md](https://github.com/asmcup/runtime/blob/master/SPEC.md) file for details
on the available instructions and operations.

## Game Basics

The game world is randomly generated based on a seed value. This allows anyone
to easily generate a new world and test their robot within it. When running the
code on our servers, the seed value will be randomized and all bots will have to
discover the world they have been placed into and compete for resources.

The world consists of tiles of size 32x32. Aside from normal ground tiles,
there are tiles with items, hazards, obstacles and walls/rooms. Robots have
multiple ways of interacting with the world, ranging from controls for motor
and steering over a beam sensor, a laser, a compass and more to being able to
(*WIP*) communicating with each other.

It is planned that multiple robots will be able to compete in the same world,
including the ability to fight each other.

### Items

The main goal of each robot is to collect gold, which occurs as item in the world.
Players will receive prizes based on the amount of gold their robots collected.
Robots can also pick up battery items to recharge their internal battery, which is
consumed when executing instructions and taking damage.

### Hazards

There are currently 4 different hazards that penalize robots for standing in them:

* Mud pit (low damage)
* Water pit (medium damage)
* Fire pit (severe damage)
* Deep pit  (instant death)

### Obstacles

There are four types of obstacles, with the first two just being basic doodads
in the game world like stumps and bushes. The last two are rocks which can be
destroyed with a laser.

### Rooms

Some areas of the game world spawn "rooms", which are walled off areas, sometimes
with rocks blocking their entrances. Some rooms can have hazards as walls, these
have more loot in them.

