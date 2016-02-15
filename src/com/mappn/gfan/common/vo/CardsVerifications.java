package com.mappn.gfan.common.vo;

import java.util.ArrayList;
import java.util.List;

public class CardsVerifications {
	public int version;
	public List<CardsVerification> cards;

	public CardsVerifications() {
		cards = new ArrayList<CardsVerification>();
	}

	public String[] getCardNames() {
		String cardNames[] = null;
		if (cards != null && cards.size() > 0) {
			int len = cards.size();
			cardNames = new String[len];
			for (int i = 0; i < len; i++) {
				cardNames[i] = cards.get(i).name;
			}
		}
		return cardNames;
	}
}
