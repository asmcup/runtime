[![Build Status](https://travis-ci.org/asmcup/runtime.svg)](https://travis-ci.org/asmcup/runtime)
[![Join the chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/asmcup/Lobby)

# asmcup

`asmcup` is a game where players create small and limited programs
to power robots in a virtual environment to compete for prizes.

It is currently in active alpha development.

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
you can either import the project into Eclipse or build using `gradle jar`

## Compete

Made a robot you think can hold it's own on our servers?

*Note we aren't ready for uploading until come November*

 * [Upload your robot](https://asmcup.github.io)


## Specifications

For some nitty gritty details see the `SPEC.md` file for opcodes and IO
commands.

## Game Basics

The game world is randomly generated based on a seed value. This allows anyone
to easily generate a new world and test their robot within it. When running the
code on our servers the seed value will be randomized and all bots will have to
discover the world they have been placed into and compete for resources.

The world consists of tiles of size 32x32. Aside from normal ground tiles,
there are tiles with items, hazards, obstacles and walls/rooms.

It is planned that multiple robots will be able to compete in the same world,
including the ability to fight each other.

### Hazards

There are currently 4 levels of hazards:

* Mud pit (low battery damage)
* Water pit (medium battery damage)
* Fire pit (severe battery damage)
* Deep pit  (instant death)

### Obstacles

There are four types of obstacles, with the first two just being basic doodads
in the game world like stumps and bushes. The last two are rocks which can be
destroyed with a lazer.

### Rooms

Some areas of the game world spawn "rooms" which are areas walled off, sometimes
with rocks blocking their entrances. Some rooms can have hazards as walls with
more loot in them.

### Items

Robots can collect battery power which recharge them essentially giving them
more life as well as collect gold which determines prize payout.

