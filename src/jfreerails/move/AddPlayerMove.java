package jfreerails.move;

import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.accounts.StockTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;

/**
 * Adds a player to the world.
 * 
 * @author Luke
 */
public class AddPlayerMove implements Move, ServerMove {
	private static final long serialVersionUID = 3977580277537322804L;

	private final Player player2add;

	private AddPlayerMove(Player p) {
		player2add = p;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AddPlayerMove))
			return false;

		final AddPlayerMove addPlayerMove = (AddPlayerMove) o;

		if (!player2add.equals(addPlayerMove.player2add))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return player2add.hashCode();
	}

	public static AddPlayerMove generateMove(ReadOnlyWorld w, Player player) {
		/**
		 * create a new player with a corresponding Principal
		 */
		Player player2add = new Player(player.getName(), w.getNumberOfPlayers());

		return new AddPlayerMove(player2add);
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		if (isAlreadyASimilarPlayer(w))
			return MoveStatus
					.moveFailed("There is already a player with the same name.");

		return MoveStatus.MOVE_OK;
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		int numPlayers = w.getNumberOfPlayers();
		Player pp = w.getPlayer(numPlayers - 1);
		if (pp.equals(player2add)) {
			return MoveStatus.MOVE_OK;
		}
		return MoveStatus.moveFailed("The last player is " + pp.getName()
				+ "not " + player2add.getName());
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryDoMove(w, p);
		if (!ms.ok)
			return ms;
		int playerId = w.addPlayer(this.player2add);		
		// Sell the player 2 $500,000 bonds at 5% interest.
		FreerailsPrincipal principal = player2add
				.getPrincipal();
		w.addTransaction(principal, BondTransaction.issueBond(5));
		//Issue stock
		Money initialStockPrice = new Money(5);
		Transaction t = StockTransaction.issueStock(playerId, 100000,
				initialStockPrice);
		w.addTransaction(principal, t);
		return ms;
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryUndoMove(w, p);
		if (!ms.ok)
			return ms;

		w.removeLastTransaction(player2add.getPrincipal());
		w.removeLastTransaction(player2add.getPrincipal());
		w.removeLastPlayer();

		return ms;
	}

	private boolean isAlreadyASimilarPlayer(World w) {
		for (int i = 0; i < w.getNumberOfPlayers(); i++) {
			Player pp = w.getPlayer(i);
			if (pp.getName().equalsIgnoreCase(this.player2add.getName())) {
				return true;
			}
		}
		return false;
	}
}