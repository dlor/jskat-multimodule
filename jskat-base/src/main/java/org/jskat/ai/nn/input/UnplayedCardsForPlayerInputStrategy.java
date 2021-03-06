/**
 * JSkat - A skat program written in Java
 * by Jan Schäfer, Markus J. Luzius and Daniel Loreck
 *
 * Version 0.13.0-SNAPSHOT
 * Copyright (C) 2013-05-10
 *
 * Licensed under the Apache License, Version 2.0. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jskat.ai.nn.input;

import org.jskat.player.PlayerKnowledge;
import org.jskat.util.Card;

/**
 * Gets network inputs for unplayed cards by the player
 */
public class UnplayedCardsForPlayerInputStrategy extends
		AbstractCardInputStrategy {

	@Override
	public double[] getNetworkInput(PlayerKnowledge knowledge, Card cardToPlay) {

		double[] result = getEmptyInputs();

		for (Card card : Card.values()) {
			if (knowledge.isOwnCard(card)) {
				result[getNetworkInputIndex(card)] = 1.0;
			}
		}

		return result;
	}
}
