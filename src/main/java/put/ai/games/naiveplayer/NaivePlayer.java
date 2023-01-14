/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.naiveplayer;

import java.util.List;
import java.util.Random;
import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;

public class NaivePlayer extends Player {

    private Random random = new Random(0xdeadbeef);


    @Override
    public String getName() {
        return "Jędrzej Górski 148128 Aleksander Szlachta xxxxxx";
    }
    
    // Returns the B value of a piece at x, y
    protected double isPieceBlocked(int x, int y, int boardSize, Color player, Board b) {
    	if (player == Color.PLAYER1) {
    		// If the piece in front of the one being tested belongs to the player, is blocked by an enemy piece? If so, return 0, if not, return 1/3
    		if (b.getState(x + 1, y) == player) {
    			// Min is used here to prevent chaining pieces from lowering the value below 1/4
    			return Math.min(1/4, 1/3 * isPieceBlocked(x + 1, y, boardSize, player, b));
    		}
    		// If the piece can't move forward, return 0
    		else if (x + 1 == boardSize || b.getState(x + 1, y) == Color.PLAYER2){
    			return 0;
    		}
    		// If the piece can move forward, return 1
    		else {
    			return 1;
    		}
    	}
    	else if (player == Color.PLAYER2) {
    		// If the piece in front of the one being tested belongs to the player, is blocked by an enemy piece? If so, return 0, if not, return 1/3
    		if (b.getState(x, y - 1) == player) {
    			// Min is used here to prevent chaining pieces from lowering the value below 1/4
    			return Math.min(1/4, 1/3 * isPieceBlocked(x, y - 1, boardSize, player, b));
    		}
    		// If the piece can't move forward, return 0
    		else if (y - 1 == -1 || b.getState(x, y - 1) == Color.PLAYER2){
    			return 0;
    		}
    		// If the piece can move forward, return 1
    		else {
    			return 1;
    		}
    	}
    	else {
    		return 0;
    	}
    }
    
    // Returns the value of W / P for a piece at a given x, y
    protected double calculatePathFreedom(int x, int y, int boardSize, Color player, Board b) {
    	int maximumDistance;
    	// Assuming that the piece is not actually blocked, since the function should never be called otherwise
    	int blockadeDistance = 1;
    	if (player == Color.PLAYER1) {
    		maximumDistance = boardSize - 1 - x;
    		while (true) {
    			// If the piece moved forward, would it still be capable of movement? Break if it would be blocked, if not, increment W by 1.
    			if (isPieceBlocked(x + blockadeDistance, y, boardSize, player, b) == 0.0) {
    				break;
    			}
    			blockadeDistance += 1;
    		}
    		
    	}
    	else if (player == Color.PLAYER2){
    		maximumDistance = y;
    		while (true) {
    			// If the piece moved upward, would it still be capable of movement? Break if it would be blocked, if not, increment W by 1.
    			if (isPieceBlocked(x, y - blockadeDistance, boardSize, player, b) == 0.0) {
    				break;
    			}
    			blockadeDistance += 1;
    		}
    	}
    	else {
    		return 0.0;
    	}
    	// W / P
    	return blockadeDistance / maximumDistance;
    }
    
    // Calculate max(1/2, min(L/3, 1)), addressing how easy it is for an opponent to block the piece at x, y. This function should never be called on a blocked piece
    protected double calculateEaseOfBlockade(int x, int y, int boardSize, Color player, Board b) {
    	int blockadeSteps = boardSize;
    	if (player == Color.PLAYER1) {
    		//For every column ahead of the piece...
    		for (int i = (x + 1); i < boardSize; i++) {
    			//...check how many moves the opponent would have to make minimum to block the piece at x, y
    			for (int j = (y + 1); j >= 0; j++) {
    				if (b.getState(i, j) == Color.PLAYER2) {
    					if (blockadeSteps > j - y - 1) {
    						blockadeSteps = j - y - 1;
    						break;
    					}
    				}
    			}
    		}
    	}
    	else if (player == Color.PLAYER2) {
    		//For every row ahead of the piece...
    		for (int j = y - 1; j >= 0; j--) {
    			//...check how many moves the opponent would have to make minimum to block the piece at x, y
    			for (int i = x - 1; i < boardSize; i--) {
    				if (b.getState(i, j) == Color.PLAYER1) {
    					if (blockadeSteps > x - 1 - i) {
    						blockadeSteps = x - 1 - i;
    						break;
    					}
    				}
    			}
    		}
    	}
    	// If player == Color.EMPTY, should never occur
    	else {
    		return 0.0;
    	}
    	return Math.max(1/2, Math.min(blockadeSteps / 3, 1));
    }
    
    // Return the board value for a given board state and turn. The function will return a value multiplied by -1 for the enemy's turn in order to be compatible with the Negamax algorithm.
    protected double evaluateBoard(Board b, Color turn) {
    	Color playerNo = getColor();
    	Color opponentNo = Color.EMPTY;
    	double playerOneScore = 0.0;
    	double playerTwoScore = 0.0;
    	
    	if (playerNo == Color.PLAYER1) {
    		opponentNo = Color.PLAYER2;
    	}
    	else {
    		opponentNo = Color.PLAYER1;
    	}
    	
    	// Does the game end? If so, return an arbitrarily high value
    	if (b.getMovesFor(playerNo).isEmpty() && turn == playerNo) {
    		return -10000.0;
    	}
    	else if (b.getMovesFor(opponentNo).isEmpty() && turn == opponentNo) {
    		return 10000.0;
    	}
    	
    	if (playerNo == Color.PLAYER1) {
    		
    	}
    	
    	int boardDimensions = b.getSize();
    	for (int i = 0; i < boardDimensions; i++) {
    		for (int j = 0; j < boardDimensions; j++) {
    			// Calculate a piece's contribution to PLAYER1's score
    			if (b.getState(i, j) == Color.PLAYER1) {
    				double isBlockedCoeff = isPieceBlocked(i, j, boardDimensions, Color.PLAYER1, b);
    				// Float comparison using a fixed epsilon value. If isBlockedCoeff isn't equal to 1.0, return the value of the piece.
    				if (Math.abs(isBlockedCoeff - 1.0) > 0.0001) {
    					playerOneScore += isBlockedCoeff * 10;
    					continue;
    				}
    				else {
    					// freePathCoeff is equivalent to W / P in the formula
    					double freePathCoeff = calculatePathFreedom(i, j, boardDimensions, Color.PLAYER1, b);
    					// blockadeEaseCoeff is equivalent to max(1/2, min(1, L/3)) in the formula
    					double blockadeEaseCoeff = calculateEaseOfBlockade(i, j, boardDimensions, Color.PLAYER1, b);
    					// Formula for unblocked pieces
    					playerOneScore += isBlockedCoeff * 10 * Math.max(1/3, freePathCoeff * blockadeEaseCoeff);
    				}
    			}
    			// Calculate a piece's contribution to PLAYER2's score
    			else if (b.getState(i, j) == Color.PLAYER2) {
    				double isBlockedCoeff = isPieceBlocked(i, j, boardDimensions, Color.PLAYER2, b);
    				// Float comparison using a fixed epsilon value. If isBlockedCoeff isn't equal to 1.0, return the value of the piece.
    				if (Math.abs(isBlockedCoeff - 1.0) > 0.0001) {
    					playerTwoScore += isBlockedCoeff * 10;
    					continue;
    				}
    				else {
    					// freePathCoeff is equivalent to W / P in the formula
    					double freePathCoeff = calculatePathFreedom(i, j, boardDimensions, Color.PLAYER2, b);
    					// blockadeEaseCoeff is equivalent to max(1/2, min(1, L/3)) in the formula
    					double blockadeEaseCoeff = calculateEaseOfBlockade(i, j, boardDimensions, Color.PLAYER2, b);
    					// Formula for unblocked pieces
    					playerTwoScore += isBlockedCoeff * 10 * Math.max(1/3, freePathCoeff * blockadeEaseCoeff);
    				}
    			}
    		}
    	}
    	// Change the value of the opponent's score to be negative and add both scores to each other to get the final state score.
    	if (playerNo == Color.PLAYER1) {
    		playerTwoScore *= -1;
    	}
    	else {
    		playerOneScore *= -1;
    	}
    	
    	if (turn == playerNo) {
    		return playerTwoScore + playerOneScore;
    	}
    	else {
    		return -(playerTwoScore + playerOneScore);
    	}
    	
    }
    
    protected double negamax(Board b, int remainingDepth) {
    	if (remainingDepth == 0) {
    		return 0.0;
    	}
    	return 0.0;
    }


    @Override
    public Move nextMove(Board b) {
        List<Move> moves = b.getMovesFor(getColor());
        return moves.get(random.nextInt(moves.size()));
    }
}
