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
package org.jskat.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all player positions in a trick
 */
public enum Player {
	/**
	 * First player
	 */
	FOREHAND {
		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public Player getRightNeighbor() {
			return REARHAND;
		}

		@Override
		public Player getLeftNeighbor() {
			return MIDDLEHAND;
		}
	},
	/**
	 * Second player
	 */
	MIDDLEHAND {
		@Override
		public int getOrder() {
			return 1;
		}

		@Override
		public Player getRightNeighbor() {
			return FOREHAND;
		}

		@Override
		public Player getLeftNeighbor() {
			return REARHAND;
		}
	},
	/**
	 * Third player
	 */
	REARHAND {
		@Override
		public int getOrder() {
			return 2;
		}

		@Override
		public Player getRightNeighbor() {
			return MIDDLEHAND;
		}

		@Override
		public Player getLeftNeighbor() {
			return FOREHAND;
		}
	};

	public static List<Player> getOrderedList() {
		List<Player> result = new ArrayList<Player>();
		result.add(FOREHAND);
		result.add(MIDDLEHAND);
		result.add(REARHAND);
		return result;
	}

	/**
	 * Gets order of a player
	 * 
	 * @return Order of the player
	 */
	public abstract int getOrder();

	/**
	 * Gets the player right from the player
	 * 
	 * @return Player right from the player
	 */
	public abstract Player getRightNeighbor();

	/**
	 * Gets the player left from the player
	 * 
	 * @return Player left from the player
	 */
	public abstract Player getLeftNeighbor();
}
