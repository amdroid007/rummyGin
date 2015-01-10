package gameEngine;

import java.util.HashSet;
import java.util.Set;

import ca.mcgill.cs.comp303.rummy.model.Card;

public class OptPlayer extends AbstractPlayer {

	public OptPlayer(IGameEngineSetter gameSetter) {
		super("Wobbuffet", gameSetter);
		// TODO Auto-generated constructor stub
	}
	
	private Set<Card> copyHand(){
		Set<Card> copy = new HashSet<Card>();
		for (Card c: aHand){
			copy.add(c);
		}
		return copy;
	}
	
	/*
	 * return the card to discard
	 */
	private Card maximize(Card toAdd, int minScore){
		Card optDiscard = null;
		Card preRemoved = null;
		Set<Card> s = copyHand();
		for (Card c: s){
			aHand.remove(c);
			if (preRemoved == null){
				aHand.add(toAdd);
			}else{
				aHand.add(preRemoved);
			}
			preRemoved = c;
			aHand.autoMatch();
			int score = getHandScore();
			if (minScore > score){
				minScore = score;
				optDiscard = c;
			}
		}
		if (optDiscard != null){
			if (!optDiscard.equals(preRemoved)){
				aHand.remove(optDiscard);
				aHand.add(preRemoved);
			}
			return optDiscard;
		}
		aHand.remove(toAdd);
		aHand.add(preRemoved);
		return null;
	}
	
	@Override
	public void pickCard() {
		aHand.autoMatch();
		int score = getHandScore();
		Card toDiscard = maximize(gameEngine.peekDiscard(), score);
		if (toDiscard != null){
			gameEngine.takeDiscard();
			newCard = toDiscard;
		}else{
			newCard = gameEngine.drawFromDeck();
			toDiscard = maximize(newCard, score);
			if (toDiscard != null) newCard = toDiscard;
		}	
	}

	@Override
	public void discard() {
		gameEngine.discard(newCard);
	}

	@Override
	public void knock() {
		if (getHandScore() < 5){
			gameEngine.knock();
		}
	}

	@Override
	public boolean isRobot() {
		return true;
	}

}