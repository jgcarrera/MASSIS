---
title : "Perception"
---

This is the continuation of the [fourth tutorial]({{site.baseurl}}/tutorials/04-bigger-environment-and-multiple-agents). In this tutorial, we will learn how agents can query some of the properties of the environment, and we will make them to play the tag game.

# How agents can perceive the environment.

The `LowLevelAgent` can obtain information about its environment, such as the room where it is, its current location, and other agents near him.

Let's add a simple print method for checking this out. The method [`getAgentsInRange`](https://github.com/rpax/MASSIS/blob/master/massisframework/massis/massis-core/src/main/java/com/massisframework/massis/model/agents/LowLevelAgent.java#L52) returns the LowLevelAgents within a range of the agent on which the method was performed.

So, in `MyHelloHighLevelControllerjava` we can write the following method:

	private void printAgentsIDsInRange(double range) {
		StringBuilder sb = new StringBuilder();

		for (LowLevelAgent otherAgent : this.agent.getAgentsInRange(range)) {
			final int otherId = otherAgent.getID();
			final Location agentLoc = this.agent.getLocation();
			final Location otherLoc = otherAgent.getLocation();
			final double distance = agentLoc.distance2D(otherLoc);

			sb.append("\tAgent #").append(otherId).append(". distance: ")
					.append(distance).append("\n");
		}
		if (sb.length() > 0) {
			System.out.println("Agent #" + this.agent.getID()+" has in the range of "+range+" cm:");
			System.out.println(sb.toString());
		}
	}

and add it at the beginning of the `step()` method

	@Override
	public void step() {

		printAgentsIDsInRange(200);
		if (this.currentTarget == null) {
			/* 1 */
    //etc

This should print in the console output something like this:

    Agent #12 has in the range of 200.0 cm:
        Agent #42. distance: 233.04966693463686

    Agent #12 has in the range of 200.0 cm:
        Agent #42. distance: 213.96123582412258

	...etc

So, agent's can move, and see each other. Let's make them to play a game.

# The Game

## Game Rules

[Tag Game rules from Wikipedia][tag_game]:

>A group of players (two or more) decide who is going to be "it", often using a counting-out game such as eeny, meeny, miny, moe.
>
>The player selected to be "it" then chases the others, attempting to get close enough to "tag" one of them (touching them with a hand) while the others try to escape.
>
>A tag makes the tagged player "it" - in some variations[...]

## Implementation

We can model the behavior of the agents following the flowchart below:

![](http://i.imgur.com/UNrajuM.png)

### Conditionals
For modeling the conditionals of the flowchart, we need to:

- Check if the agent is tagged. This can be done in two ways.

	1. Adding a new property to `MyHelloHighLevelController`.

            private boolean tagged;

            public boolean isTagged() {
                return tagged;
            }

            public void setTagged(boolean tagged) {
                this.tagged = tagged;
            }

	2. Using the LowLevelAgent's `getProperty()` and `setProperty()` methods. These methods provide a simple way for storing information into the low-level agents. However, it becomes quickly unmaintanable. The way for doing this should be the following:

            public boolean isTagged() {
            	return "true".equals(this.agent.getProperty("TAGGED"));
            }

            public void setTagged(boolean tagged) {
            	this.agent.setProperty("TAGGED", String.valueOf(tagged));
            }



		The initial value of `"TAGGED"` should be introduced first in any agent of the environment.
        ![](http://i.imgur.com/ITGP0JP.gif)
        And then, in the constructor of the behavior, we need to recover the metadata of provided from the environment and setting it into the agent's attributes:

            public MyHelloHighLevelController(LowLevelAgent agent,
                    Map<String, String> metadata, String resourcesFolder) {
                super(agent, metadata, resourcesFolder);
                this.agent.setHighLevelData(this);
                String taggedStr = metadata.get("TAGGED");
                if (taggedStr == null || !"true".equals(metadata.get("TAGGED"))) {
                    this.setTagged(false);
                } else {
                    this.setTagged(true);
                }
            }


-  Check for seeing a tagged / untagged agent

        private MyHelloHighLevelController getNearestAgent(double range,
                boolean tagStatus) {
            /*
             * We set a high limit
             */
            double minDist = Float.MAX_VALUE;
            /*
             * Location of this agent
             */
            final Location agentLoc = this.agent.getLocation();
            /*
             * Nearest agent found
             */
            MyHelloHighLevelController nearest = null;
            for (LowLevelAgent otherAgent : this.agent.getAgentsInRange(range)) {
                /*
                 * Retrieve the high-level data of the other agent. It should be of
                 * the type of agent playing this game, MyHelloHighLevelController.
                 */
                final Object highLevelData = otherAgent.getHighLevelData();
                if (highLevelData instanceof MyHelloHighLevelController) {
                    MyHelloHighLevelController otherCtrl = (MyHelloHighLevelController) highLevelData;
                    /*
                     * Satisfies the search condition?
                     */
                    if (otherCtrl.isTagged() == tagStatus) {
                        final Location otherLoc = otherAgent.getLocation();
                        final double distance = agentLoc.distance2D(otherLoc);
                        /*
                         * Store if nearest.
                         */
                        if (distance < minDist) {
                            nearest = otherCtrl;
                            minDist=distance;
                        }
                    }
                }
            }
            return nearest;
        }

- Check for detecting if the distance to the tagged agent is less than the maximum allowed: (_sees tagged agent_, in the flowchart)

        private boolean seesTaggedAgent() {
                // true, because is tagged
                return getNearestAgent(search_range, true) != null;
        }

- Check for the distance being less than 0.5 m (For the sake of completeness):

        private boolean isDistanceLessThan50cm(LowLevelAgent a1,LowLevelAgent a2) {
            return a1.getLocation().distance2D(a2.getLocation())<50;
        }

### Coding the flow

We have written the conditional checks of the flowchart. Let's put the pieces together.
The `START` section of the flowchart corresponds to:

    @Override
    public void step() {
        // START
        if (this.isTagged()) {
            runAsTagged();
        } else {
            runAsNotTagged();
        }
    }

Now, going to the _not tagged_ branch,

![](http://i.imgur.com/nidqPY6.png)

    private void runAsNotTagged() {
        // sees tagged agent?
        if (seesTaggedAgent()) {
            /*
             * Retrieve the polygon associated with the current room
             */
            KPolygon polygon = this.agent.getRoom().getPolygon();
            /*
             * If the agent's target is in the same room of the agent (and in
             * the same room as the "tagged" one), select a new target in a
             * different room.
             */
            while (this.randomTarget == null
                    || polygon.contains(this.randomTarget.getXY())) {
                this.randomTarget = this.agent.getRandomRoom().getRandomLoc();
            }
        }
        this.moveRandomly();
    }
The new part here is the `getPolygon()` method. In MASSIS, every simulation object has a polygonal representation. Here, we take advantage of that possibility to detect if a point is contained in a the room's shape.

For the  _tagged_ branch,

![](http://i.imgur.com/AyOQSGJ.png)

This is a little bit more complex than the other branch.

- First, we need to check if there is any _not tagged_ agent in range. Remember that the `false` parameter was because we want to obtain the agents that are not tagged.

		MyHelloHighLevelController nearest = getNearestAgent(search_range, false);

- After that, if there is any agent in range (that is, `nearest!=null`), we check the distance to it
	- If the distance < 0.5 meters (50 cm),
        - The agent _tags_ it
        - The agent _untags_ itself

                final Location nearestLoc = nearest.agent.getLocation();
                // Yes, and is the closest one.
                // if distance < 0.5 m, (50 cm), tag it
                final double distance = agentLoc.distance2D(nearestLoc);
                if (distance < tag_max_distance) {
                    // tag it
                    nearest.setTagged(true);
                    // un-tag itself
                    this.setTagged(false);
                    // end
                }

	- If not, the tagged agent just chase its nearest target.

            this.agent.approachTo(nearestLoc, new ApproachCallback() {

                @Override
                public void onTargetReached(LowLevelAgent agent) {
                    // Nothing this time. We are handling the logic
                    // elsewhere.
                }

                @Override
                public void onSucess(LowLevelAgent agent) {}

                @Override
                public void onPathFinderError(
                        PathFinderErrorReason reason) {
                    // Error!
                    Logger.getLogger(
                            MyHelloHighLevelController.class.getName())
                            .log(Level.SEVERE,
                                    "Error when approaching to {0} Reason: {1}",
                                    new Object[] { nearestLoc, reason });
                }
            });
- If there wasn't any agent in range, just move randomly

		this.moveRandomly();

Putting all together,

    private void runAsTagged() {
        final Location agentLoc = this.agent.getLocation();
        // Sees un-tagged agent?
        MyHelloHighLevelController nearest = getNearestAgent(search_range,
                false);
        if (nearest != null) {
            final Location nearestLoc = nearest.agent.getLocation();
            // Yes, and is the closest one.
            // if distance < 0.5 m, (50 cm), tag it
            final double distance = agentLoc.distance2D(nearestLoc);
            if (distance < tag_max_distance) {
                // tag it
                nearest.setTagged(true);
                // un-tag itself
                this.setTagged(false);
                // end
            } else {
                // chase him
                this.agent.approachTo(nearestLoc, new ApproachCallback() {

                    @Override
                    public void onTargetReached(LowLevelAgent agent) {
                        // Nothing this time. We are handling the logic
                        // elsewhere.
                    }

                    @Override
                    public void onSucess(LowLevelAgent agent) {
                    }

                    @Override
                    public void onPathFinderError(
                            PathFinderErrorReason reason) {
                        // Error!
                        Logger.getLogger(
                                MyHelloHighLevelController.class.getName())
                                .log(Level.SEVERE,
                                        "Error when approaching to {0} Reason: {1}",
                                        new Object[] { nearestLoc, reason });
                    }
                });
            }
        } else {
            // no target found:
            this.moveRandomly();
        }

    }

The `moveRandomly()` method is an adaptation of the previous code that made the agent move to random targets.

    private Location randomTarget = null;

    private void moveRandomly() {
        if (this.randomTarget == null) {
            Location randomLocation = this.agent.getRandomRoom().getRandomLoc();
            this.randomTarget = randomLocation;
        }
        this.agent.approachTo(this.randomTarget, new ApproachCallback() {

            @Override
            public void onTargetReached(LowLevelAgent agent) {
                randomTarget = null;
            }

            @Override
            public void onSucess(LowLevelAgent agent) {
            }

            @Override
            public void onPathFinderError(PathFinderErrorReason reason) {
                // Error!
                Logger.getLogger(MyHelloHighLevelController.class.getName())
                        .log(Level.SEVERE,
                                "Error when finding path Reason: {0}", reason);
            }
        });
    }

If everything went ok, the result should be something like this:

![](http://i.imgur.com/Jva4HPG.gif)

>Note: The code for the complete behavior is [in this gist](https://gist.github.com/rpax/b457fa14d2a9d14779ab)


The problem is... **_Who is the tagged one_ ? All the agents are identical!**.

The [Next Tutorial]({{site.baseurl}}/tutorials/06-extending-the-gui) solves that problem, explaining how to develop visualization utilities.






[tag_game]: https://en.wikipedia.org/wiki/Tag_(game)