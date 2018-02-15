/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Provides classes to record a players assets (for example, real estate, miles-of-track,
 * cash, and shares) and liabilities (for example, outstanding shares and bonds,
 * and any overdraft). Each player has a BankAccount that stores a growing list of
 * Transactions. Transactions are used represent bills and receipts for a player
 * as well as record changes in a players physical assets such as miles-of-track.
 * Totals can be calculated by adding up the relevant transactions in the players
 * account.
 */
package freerails.model.finances;