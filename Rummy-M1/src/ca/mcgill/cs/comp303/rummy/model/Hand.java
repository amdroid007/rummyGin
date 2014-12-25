package ca.mcgill.cs.comp303.rummy.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Models a hand of 10 cards. The hand is not sorted. Not threadsafe.
 * The hand is a set: adding the same card twice will not add duplicates
 * of the card.
 * @inv size() > 0
 * @inv size() <= HAND_SIZE
 */
public class Hand implements Iterable<Card>
{
	public static final int HANDSIZE = 10;
	private static final int MAX_POINT = 10;
	
	private HashMap<Card, Boolean> aCards;
	private HashSet<ICardSet> aMatchSet;
	private boolean autoMatchCalled;
	
	/**
	 * Creates a new, empty hand.
	 */
	public Hand()
	{
		aCards = new HashMap<Card, Boolean>();
		aMatchSet = null;
		autoMatchCalled = false;
	}
	
	/**
	 * Adds pCard to the list of unmatched cards.
	 * If the card is already in the hand, it is not added.
	 * @param pCard The card to add.
	 * @throws HandException if the hand is complete.
	 * @throws HandException if the card is already in the hand.
	 * @pre pCard != null
	 */
	public void add( Card pCard )
	{
		assert (pCard != null);
		if (isComplete()) throw new HandException("Complete hand.");
		if (contains(pCard)) throw new HandException("Already in hand.");
		aCards.put(pCard, false);
		autoMatchCalled = false;
	}
	
	/**
	 * Remove pCard from the hand and break any matched set
	 * that the card is part of. Does nothing if
	 * pCard is not in the hand.
	 * @param pCard The card to remove.
	 * @pre pCard != null
	 */
	public void remove( Card pCard )
	{
		assert (pCard != null);
		aCards.remove(pCard);
		autoMatchCalled = false;
	}
	
	/**
	 * @return True if the hand is complete.
	 */
	public boolean isComplete()
	{
		return aCards.size() == HANDSIZE;
	}
	
	/**
	 * Removes all the cards from the hand.
	 */
	public void clear()
	{
		aCards.clear();
	}
	
	/**
	 * Reset automatch
	 */
	public void reset(){
		for (Card c : this){
			aCards.put(c, false);
		}
		aMatchSet = null;
	}
	
	/**
	 * @return A copy of the set of matched sets
	 */
	public Set<ICardSet> getMatchedSets()
	{
		return aMatchSet;
	}
	
	/**
	 * @return A copy of the set of unmatched cards.
	 */
	public Set<Card> getUnmatchedCards()
	{
		HashSet<Card> cards = new HashSet<Card>(aCards.keySet());
		for (Card c : this){
			if (aCards.get(c)){
				cards.remove(c);
			}
		}
		return cards;
	}
	
	/**
	 * @return The number of cards in the hand.
	 */
	public int size()
	{
		return aCards.size();
	}
	
	/**
	 * Determines if pCard is already in the hand, either as an
	 * unmatched card or as part of a set.
	 * @param pCard The card to check.
	 * @return true if the card is already in the hand.
	 * @pre pCard != null
	 */
	public boolean contains( Card pCard )
	{
		assert (pCard != null);
		return aCards.containsKey(pCard);
	}
	
	/**
	 * @return The total point value of the unmatched cards in this hand.
	 */
	public int score()
	{
		int score = 0;
		for (Map.Entry pair : aCards.entrySet()){
			if (! (Boolean) pair.getValue()){
				if (((Card) pair.getKey()).getRank().ordinal() >= MAX_POINT){
					score += MAX_POINT;
				}else{
					score += ((Card) pair.getKey()).getRank().ordinal()+1;
				}
			}
		}
		return score;
	}
	
	/**
	 * Create groups
	 * @return a set of groups
	 */
	public Set<ICardSet> createGroups()
	{
		Comparator<Card> groupComparator = new Comparator<Card>(){
			@Override
			public int compare(Card arg0, Card arg1) {
				return arg0.getRank().compareTo(arg1.getRank());
			}
		};
		ArrayList<Card> sorted = new ArrayList<Card>(aCards.keySet());
		sorted.sort(groupComparator);
		HashSet<ICardSet> sets = new HashSet<ICardSet>();
		for (int i=0; i<sorted.size(); i++){
			for (int j=i+1; j<sorted.size(); j++){
				if (groupComparator.compare(sorted.get(i), sorted.get(j)) == 0){
					if (j-i>1){
						HashSet<Card> temp = new HashSet<Card>();
						for (int k = i; k<j+1; k++){
							temp.add(sorted.get(k));
						}
						sets.add(new GroupSet(temp));
						if (temp.size() == 4){
							sets.add(new RunSet(new HashSet<Card>(
									Arrays.asList(sorted.get(i), sorted.get(i+1), sorted.get(j)))));
							sets.add(new RunSet(new HashSet<Card>(
									Arrays.asList(sorted.get(i), sorted.get(i+2), sorted.get(j)))));
						}
					}
				}else{
					break;
				}
				
			}
		}
		return sets;
	}
	
	/**
	 * Creates runs
	 * @return a set of runs
	 */
	public Set<ICardSet> createRun()
	{
		Comparator<Card> runComparator = new Comparator<Card>(){
			@Override
			public int compare(Card arg0, Card arg1) {
				return arg0.compareTo(arg1);
			}
		};
		ArrayList<Card> sorted = new ArrayList<Card>(aCards.keySet());
		sorted.sort(runComparator);
		HashSet<ICardSet> sets = new HashSet<ICardSet>();
		for (int i=0; i<sorted.size(); i++){
			for (int j=i+1; j<sorted.size(); j++){
				if (runComparator.compare(sorted.get(j), sorted.get(j-1)) == 1){
					if (j-i > 1){
						HashSet<Card> temp = new HashSet<Card>();
						for (int k = i; k<j+1; k++){
							temp.add(sorted.get(k));
						}
						sets.add(new RunSet(temp));
					}
				}else{
					break;
				}
			}
		}
		return sets;
	}
	
	private HashMap<HashSet<ICardSet>, Integer> recursiveAutoMatch(Stack<ICardSet> sets){
		// base case, if empty stack
		if (sets.isEmpty()) return new HashMap<HashSet<ICardSet>, Integer>();
		
		ICardSet first = sets.pop();
		// get the points
		int score = 0;
		for (Card c : first){
			if (c.getRank().ordinal() >= MAX_POINT){
				score += MAX_POINT;
			}else{
				score += c.getRank().ordinal()+1;
			}
		}
		
		// recursive call
		HashMap<HashSet<ICardSet>, Integer> withFirst = recursiveAutoMatch(sets);
		
		if (!withFirst.isEmpty()){ // check for each combination if we can put "first"
			HashMap<HashSet<ICardSet>, Integer> temp = new HashMap<HashSet<ICardSet>, Integer>();
			for (Map.Entry pair : withFirst.entrySet()){
				boolean contains = false;
				if (!((HashSet<ICardSet>) pair.getKey()).isEmpty()){
					for (ICardSet s : (HashSet<ICardSet>) pair.getKey()){
						for (Card c : s){
							if (first.contains(c)){
								contains = true;
								break;
							}
						}
						if (contains) break;
					}
				}
				if (!contains){
					int val = (Integer) pair.getValue() + score;
					HashSet<ICardSet> key = new HashSet<ICardSet>();
					key.add(first);
					if (!((HashSet<ICardSet>) pair.getKey()).isEmpty())
						key.addAll((HashSet<ICardSet>)pair.getKey());
					temp.put(key, val);
				}
			}
			withFirst.putAll(temp);
		}
		withFirst.put(new HashSet<ICardSet>(Arrays.asList(first)), score);
		return withFirst;
	}
	
	/**
	 * Calculates the matching of cards into groups and runs that
	 * results in the lowest amount of points for unmatched cards.
	 */
	public void autoMatch()
	{
		if (autoMatchCalled) return; // automatch already called 
		if (aMatchSet != null) reset();
		Stack<ICardSet> sets = new Stack<ICardSet>();
		sets.addAll(createRun());
		sets.addAll(createGroups());
		HashMap<HashSet<ICardSet>, Integer> allCombos = recursiveAutoMatch(sets);
		
		// maximize points in matches = minimize points in deadwook
		int maxPoint = 0;
		HashSet<ICardSet> optimal = null; // TODO: ties
		for (Map.Entry pair : allCombos.entrySet()){
			if (maxPoint < (Integer) pair.getValue()){
				maxPoint = (Integer) pair.getValue();
				optimal = (HashSet<ICardSet>) pair.getKey();
			}
		}
		if (optimal == null) return;
		for (ICardSet s : optimal){
			for (Card c : s){
				aCards.put(c, true);
			}
		}
		aMatchSet = optimal;
	}

	@Override
	public Iterator<Card> iterator() {
		return aCards.keySet().iterator();
	}
}
