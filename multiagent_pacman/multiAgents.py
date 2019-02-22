# multiAgents.py
# --------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
# 
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent

class ReflexAgent(Agent):
    """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
    """


    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {NORTH, SOUTH, WEST, EAST, STOP}
        """
        # Collect legal moves and successor states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed successor
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        successorGameState = currentGameState.generatePacmanSuccessor(action)
        newPos = successorGameState.getPacmanPosition()
        newFood = successorGameState.getFood()
        newGhostStates = successorGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]


        "*** YOUR CODE HERE ***"

        score = successorGameState.getScore()

        # find the closest ghost
        for ghosts in newGhostStates:
            if manhattanDistance(newPos, ghosts.getPosition()) != 0:
                score -= 2/manhattanDistance(newPos, ghosts.getPosition())

        # find how many food left
        food_distance = [manhattanDistance(food, newPos) for food in newFood.asList()]
        if food_distance != []:
            score += 1/min(food_distance)
        #
        # scared time
        if max(newScaredTimes) >= 20:
            score += 1

        return score

def scoreEvaluationFunction(currentGameState):
    """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
    """
    Your minimax agent (question 2)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action from the current gameState using self.depth
        and self.evaluationFunction.

        Here are some method calls that might be useful when implementing minimax.

        gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

        gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

        gameState.getNumAgents():
        Returns the total number of agents in the game

        gameState.isWin():
        Returns whether or not the game state is a winning state

        gameState.isLose():
        Returns whether or not the game state is a losing state
        """
        "*** YOUR CODE HERE ***"


        def get_value(gameState, agent, depth):
            if depth == self.depth:
                return self.evaluationFunction(gameState), 0
            if agent == 0:
                return max_agent(gameState, agent, depth)
            else:
                return min_agent(gameState, agent, depth)


        def max_agent(gameState, agent, depth):
            if gameState.isLose() or gameState.isWin():
                return self.evaluationFunction(gameState), 0

            v = float("-inf")
            saved_action = 0

            for action in gameState.getLegalActions(agent):
                next = get_value(gameState.generateSuccessor(agent, action), agent+1, depth)
                if next[0] > v:
                    v, saved_action = next[0], action
            return v, saved_action



        def min_agent(gameState, agent, depth):
            if gameState.isLose() or gameState.isWin():
                return self.evaluationFunction(gameState), 0

            v = float("inf")
            saved_action = 0

            for action in gameState.getLegalActions(agent):
                if agent == gameState.getNumAgents() - 1:
                    next = get_value(gameState.generateSuccessor(agent, action), 0, depth + 1)
                    if next[0] < v:
                        v, saved_action = next[0], action
                else:
                    next = get_value(gameState.generateSuccessor(agent, action), agent+1, depth)
                    if next[0] < v:
                        v, saved_action = next[0], action
            return v, saved_action

        return get_value(gameState, 0, 0)[1]

class AlphaBetaAgent(MultiAgentSearchAgent):
    """
    Your minimax agent with alpha-beta pruning (question 3)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action using self.depth and self.evaluationFunction
        """

        def get_value(gameState, agent, depth, alpha, beta):
            if depth == self.depth:
                return self.evaluationFunction(gameState), 0
            if agent == 0:
                return max_agent(gameState, agent, depth, alpha, beta)
            else:
                return min_agent(gameState, agent, depth, alpha, beta)



        def max_agent(gameState, agent, depth, alpha, beta):
            if gameState.isLose() or gameState.isWin():
                return self.evaluationFunction(gameState), 0

            v = float("-inf")
            saved_action = 0

            for action in gameState.getLegalActions(agent):
                next = get_value(gameState.generateSuccessor(agent, action), agent+1, depth, alpha, beta)
                if next[0] > v:
                    v, saved_action = next[0], action
                if v > beta:
                    return v, action
                alpha = max(alpha, v)
            return v, saved_action



        def min_agent(gameState, agent, depth, alpha, beta):
            if gameState.isLose() or gameState.isWin():
                return self.evaluationFunction(gameState), 0

            v = float("inf")
            saved_action = 0

            for action in gameState.getLegalActions(agent):
                if agent == gameState.getNumAgents() - 1:
                    next = get_value(gameState.generateSuccessor(agent, action), 0, depth+1, alpha, beta)
                    if next[0] < v:
                        v, saved_action = next[0], action
                    if v < alpha:
                        return v, action
                    beta = min(beta, v)

                else:
                    next = get_value(gameState.generateSuccessor(agent, action), agent+1, depth, alpha, beta)
                    if next[0] < v:
                        v, saved_action = next[0], action
                    if v < alpha:
                        return v, action
                    beta = min(beta, v)
            return v, saved_action

        return get_value(gameState, 0, 0, float("-inf"), float("inf"))[1]

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """

    def getAction(self, gameState):
        """
        Returns the expectimax action using self.depth and self.evaluationFunction

        All ghosts should be modeled as choosing uniformly at random from their
        legal moves.
        """
        "*** YOUR CODE HERE ***"

        def get_value(gameState, agent, depth):
            if depth == self.depth:
                return self.evaluationFunction(gameState), 0
            if agent == 0:
                return max_agent(gameState, agent, depth)
            else:
                return min_agent(gameState, agent, depth)


        def max_agent(gameState, agent, depth):
            if gameState.isLose() or gameState.isWin():
                return self.evaluationFunction(gameState), 0

            v = float("-inf")
            saved_action = 0

            for action in gameState.getLegalActions(agent):
                next = get_value(gameState.generateSuccessor(agent, action), agent+1, depth)
                if next[0] > v:
                    v, saved_action = next[0], action
            return v, saved_action


        def min_agent(gameState, agent, depth):
            if gameState.isLose() or gameState.isWin():
                return self.evaluationFunction(gameState), 0

            v = 0
            saved_action = 0
            action_count = 0

            for action in gameState.getLegalActions(agent):
                action_count += 1
                if agent == gameState.getNumAgents() - 1:
                    next = get_value(gameState.generateSuccessor(agent, action), 0, depth + 1)
                    v += next[0]
                else:
                    next = get_value(gameState.generateSuccessor(agent, action), agent+1, depth)
                    v += next[0]

            return v/action_count, saved_action
        return get_value(gameState, 0, 0)[1]




def betterEvaluationFunction(currentGameState):
    """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: <write something here so we know what you did>
    """
    "*** YOUR CODE HERE ***"
    newPos = currentGameState.getPacmanPosition()
    newFood = currentGameState.getFood()
    newGhostStates = currentGameState.getGhostStates()
    newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

    score = currentGameState.getScore()

    # find the closest ghost
    for ghosts in newGhostStates:
        if manhattanDistance(newPos, ghosts.getPosition()) != 0:
            score -= currentGameState.getNumAgents() / manhattanDistance(newPos, ghosts.getPosition())

    # find how many food left
    food_distance = [manhattanDistance(food, newPos) for food in newFood.asList()]
    if food_distance != []:
        score += 1 / min(food_distance)
    #
    # scared time
    if max(newScaredTimes) >= 20:
        score += 1

    return score

# Abbreviation
better = betterEvaluationFunction
