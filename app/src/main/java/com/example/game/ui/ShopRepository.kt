package com.example.game.ui

import android.content.Context
import android.content.SharedPreferences

// What the player owns: spendable toppings, bought items and claimed milestones
data class ShopState(
    val balance: Int = 0,
    val inventory: Map<String, Int> = emptyMap(),
    val claimed: Set<String> = emptySet()
) {
    fun count(item: PowerUp): Int = inventory[item.id] ?: 0
}

class ShopRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pizza_escape_prefs", Context.MODE_PRIVATE)

    init {
        // Players who already collected toppings start with them as their balance
        if (!prefs.contains("shop_balance")) {
            prefs.edit().putInt("shop_balance", prefs.getInt("total_toppings", 0)).apply()
        }
    }

    fun load(): ShopState {
        val inventory = HashMap<String, Int>()
        for (item in PowerUp.values()) {
            inventory[item.id] = prefs.getInt("inv_" + item.id, 0)
        }
        val claimed = HashSet<String>(prefs.getStringSet("claimed_milestones", emptySet()) ?: emptySet())
        return ShopState(prefs.getInt("shop_balance", 0), inventory, claimed)
    }

    fun addToppings(amount: Int) {
        if (amount <= 0) return
        prefs.edit().putInt("shop_balance", prefs.getInt("shop_balance", 0) + amount).apply()
    }

    fun buy(item: PowerUp): Boolean {
        val balance = prefs.getInt("shop_balance", 0)
        if (balance < item.cost) return false
        prefs.edit()
            .putInt("shop_balance", balance - item.cost)
            .putInt("inv_" + item.id, prefs.getInt("inv_" + item.id, 0) + 1)
            .apply()
        return true
    }

    fun consume(item: PowerUp): Boolean {
        val owned = prefs.getInt("inv_" + item.id, 0)
        if (owned <= 0) return false
        prefs.edit().putInt("inv_" + item.id, owned - 1).apply()
        return true
    }

    fun claim(milestone: Milestone): Boolean {
        val claimed = HashSet<String>(prefs.getStringSet("claimed_milestones", emptySet()) ?: emptySet())
        if (milestone.id in claimed) return false
        claimed.add(milestone.id)
        prefs.edit()
            .putStringSet("claimed_milestones", claimed)
            .putInt("shop_balance", prefs.getInt("shop_balance", 0) + milestone.reward)
            .apply()
        return true
    }
}
