package gameEngine;

import java.util.Set;

import ca.mcgill.cs.comp303.rummy.model.Card;
import ca.mcgill.cs.comp303.rummy.model.ICardSet;

public interface Player extends Iterable<Card>
{

	/**
	 * Add a card to the hand.
	 * @param pCard
	 */
	void addCard(Card pCard);
	
	/**
	 * Take a card.
	 */
	void pickCard();
	
	/**
	 * Discard a card and put onto the discard pile.
	 */
	void discard();
	
	/**
	 * Kocking. 
	 */
	void knock();
	
	/**
	 * Get matched sets. 
	 * @return matched sets
	 */
	Set<ICardSet> getMatchedSets();
	
	/**
	 * Get deadwook.
	 * @return set of deadwook
	 */
	Set<Card> getDeadwook();
	
	/**
	 * Extend deadwook to opponent's knock. 
	 * @param pSets
	 * @return updated matched sets
	 */
	Set<ICardSet> addDeadwook(Set<ICardSet> pSets);
	
	/**
	 * Update score
	 * @param points
	 */
	void updateScore(int points);
	
	/**
	 * Get score.
	 * @return the score as an int
	 */
	int getScore();
	
	/**
	 * Get score for a matched set.
	 * Assumes that the hand is already matched.
	 * @return the score
	 */
	int getHandScore();
	
	/**
	 * Check if the player has already layout its cards.
	 * @return whether it has or not
	 */
	boolean doneLayout();
}